package be.unamur.snail.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

public class ConstructorInstrumentationProcessor extends AbstractProcessor<CtConstructor<?>> implements InstrumentProcessor<CtConstructor<?>> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String FQCN = "be.unamur.snail.spoon.constructor_instrumentation.SendConstructorsUtils";
    private static final String UTILS_VAR_NAME = "utils";

    @Override
    public void process(CtConstructor<?> constructor) {
        if (constructor.getBody() == null) return;
        instrument(constructor, new InstrumentationUtils(getFactory()));
    }

    @Override
    public void instrument(CtConstructor<?> constructor, InstrumentationUtils utils) {
        Factory factory = getFactory();

        String fileName = getFileName(constructor);
        String className = constructor.getDeclaringType().getQualifiedName();
        String constructorName = constructor.getDeclaringType().getSimpleName();
        List<String> params = utils.getParameterTypes(constructor.getParameters());
        log.debug("Found constructor {} with parameters {}", constructorName, params);

        CtLocalVariable<?> utilsVariable = utils.createConstructorInstantiationVariable(FQCN, "utils");
        CtExpression<?> utilsAccess = factory.Code().createVariableRead(utilsVariable.getReference(), false);

        // Init constructor context
        constructor.getBody().insertBegin(
                utils.createInvocation(
                        utilsAccess,
                        FQCN,
                        "initConstructorContext",
                        factory.Code().createLiteral(fileName),
                        factory.Code().createLiteral(className),
                        factory.Code().createLiteral(constructorName),
                        utils.createStringListLiteral(params)
                )
        );

        // Insert utils variable
        constructor.getBody().insertBegin(utilsVariable);

        List<CtAssignment<?, ?>> assignments = constructor.getBody().getElements(new TypeFilter<>(CtAssignment.class));


        // Add attributes
        for (CtAssignment<?, ?> assignment : assignments) {
            try {
                instrumentAssignment(assignment, utilsAccess, utils, constructor);
            } catch (Exception e) {
                log.warn("Failed to instrument assignment {} in constructor {}", assignment, constructor, e);
            }
        }

        // Stack trace
        constructor.getBody().insertEnd(utils.createInvocation(utilsAccess, FQCN, "getStackTrace"));
        constructor.getBody().insertEnd(utils.createInvocation(utilsAccess, FQCN, "send"));
    }

    protected void instrumentAssignment(CtAssignment<?, ?> assignment, CtExpression<?> utilsAccess, InstrumentationUtils utils, CtConstructor<?> constructor) {
        Factory factory = getFactory();

        // Only instrument assignments that target a field (CtFieldAccess)
        if (!(assignment.getAssigned() instanceof CtFieldAccess<?> fieldAccess)) {
            // optionally: handle CtFieldWrite / CtFieldReference / other forms - for now skip non-field writes
            return;
        }

        CtFieldReference<?> fieldRef = fieldAccess.getVariable();
        String fieldName = fieldRef.getSimpleName();
        String fieldType = safeGetQualifiedName(fieldRef.getType());
        String rhsType = getRightHandSideExpression(assignment.getAssignment(), constructor);

        // Create the invocation expression to call utils.addAttribute(...)
        CtInvocation<?> addAttributeInvocation = utils.createInvocation(
                utilsAccess,
                FQCN,
                "addAttribute",
                factory.Code().createLiteral(fieldName),
                factory.Code().createLiteral(fieldType),
                // pass the field access as the actual object parameter (we clone it to be safe)
                fieldAccess.clone(),
                factory.Code().createLiteral(rhsType)
        );

        // CASE 1: If assignment is a standalone statement inside a block, use insertAfter (safe & simple)
        if (assignment.getParent() instanceof CtBlock<?>) {
            assignment.insertAfter(addAttributeInvocation);
            return;
        }

        // CASE 2: Assignment is inside an expression => must replace the expression itself
        // We'll generate a source-level snippet that:
        //   1. performs the original assignment
        //   2. calls utils.addAttribute(...)
        //   3. returns the assigned value
        //
        // Example snippet produced:
        //   ( ( () -> { this.x = compute(); utils.addAttribute("x","pkg.Type", this.x, "invocation"); return this.x; } ) )()
        //
        // We produce a Supplier-like lambda and immediately call .get() then cast to original type if necessary.
        // For simplicity and portability across Spoon versions we use a CodeSnippetExpression.

        // Build snippet pieces
        String originalAssignmentSrc = assignment.toString(); // e.g., "this.x = foo(bar)"
        String fieldAccessSrc = fieldAccess.toString();      // e.g., "this.x"

        CtTypeReference<?> assignedExpressionType = assignment.getType(); // the type of the whole assignment expression
        String assignedTypeSrc = assignedExpressionType != null ? assignedExpressionType.toString() : "Object";

        // NOTE: if assignedTypeSrc is a primitive, we'll rely on auto-unboxing when expression is used.
        // If Java compiler complains, adapt to use boxed types explicitly (e.g., Integer.valueOf(...)).

        // Build snippet: use Supplier to return the assigned value and .get(), then cast to original type
        // Example snippet for reference type:
        //   ( (java.util.function.Supplier<assignedType>) () -> { originalAssignment; utils.addAttribute(...); return fieldAccess; } ).get()
        //
        // We must escape string literals for fieldName/fieldType/rhsType inside snippet.

        String addAttrLiteralArgs = quote(fieldName) + ", " + quote(fieldType) + ", " + fieldAccessSrc + ", " + quote(rhsType);

        String supplierType = assignedTypeSrc;

        // For primitives, Supplier<primitive> is invalid: fall back to Supplier<Object> and cast
        boolean primitiveAssigned = assignedExpressionType != null && assignedExpressionType.isPrimitive();
        if (primitiveAssigned) {
            supplierType = "Object";
        }

        // Compose the snippet
        StringBuilder snippet = new StringBuilder();

        // cast wrapper ensures the resulting expression has the original type
        snippet.append("((").append(assignedTypeSrc).append(") (");

        // supplier lambda creation and immediate get
        snippet.append("((").append("java.util.function.Supplier<").append(supplierType).append(">) () -> { ");
        // original assignment
        snippet.append(originalAssignmentSrc).append("; ");
        // addAttribute call - uses 'utils' variable which we inserted at begin of constructor body
        snippet.append(UTILS_VAR_NAME).append(".addAttribute(").append(addAttrLiteralArgs).append("); ");
        // return assigned value (field access)
        snippet.append("return ").append(fieldAccessSrc).append("; ");
        snippet.append("}").append(").get()");
        snippet.append("))");

        String snippetCode = snippet.toString();

        // Create a code snippet expression and replace the original assignment
        CtCodeSnippetExpression<?> snippetExpression = factory.Code().createCodeSnippetExpression(snippetCode);

        // Replace the assignment expression with the snippet expression
        assignment.replace(snippetExpression);
    }

    private static String safeGetQualifiedName(CtTypeReference<?> typeRef) {
        if (typeRef == null) return "java.lang.Object";
        String q = typeRef.getQualifiedName();
        return q != null ? q : typeRef.toString();
    }

    private static String quote(String s) {
        if (s == null) return "\"\"";
        // escape backslashes and double quotes for safe snippet
        String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    public String getRightHandSideExpression(CtExpression<?> expression, CtConstructor<?> constructor) {
        String sourceType;
        if (expression instanceof CtConstructorCall<?>) {
            sourceType = "constructor call";
        } else if (expression instanceof CtVariableRead<?> variableRead) {
            CtVariableReference<?> variable = variableRead.getVariable();
            boolean isConstructorParam = constructor.getParameters().stream()
                    .anyMatch(p -> p.getReference().equals(variable));
            sourceType = isConstructorParam ? "constructor parameter" : "variable reference";
        } else if (expression instanceof CtLiteral<?>) {
            sourceType = "literal";
        } else if (expression instanceof CtInvocation<?>) {
            // TODO add method names handling to see if really just an invocation or if it's a method that hides a constructor (for example with the name "builder" or "newInstance")
            sourceType = "invocation";
        } else {
            sourceType = "other";
        }
        return sourceType;
    }

    public String getFileName(CtConstructor<?> constructor) {
        String fileName = "Unknown File";
        if (constructor.getPosition() != null && constructor.getPosition().getFile() != null) {
            fileName = constructor.getPosition().getFile().getPath();
        }
        return fileName;
    }
}
