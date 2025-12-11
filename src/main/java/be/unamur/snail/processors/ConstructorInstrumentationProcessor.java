package be.unamur.snail.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

public class ConstructorInstrumentationProcessor extends AbstractProcessor<CtConstructor<?>> implements InstrumentProcessor<CtConstructor<?>> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String FQCN = "be.unamur.snail.spoon.constructor_instrumentation.SendConstructorsUtils";

    @Override
    public void process(CtConstructor<?> constructor) {
        if (constructor.getBody() == null) return;

        if (constructor.getDeclaringType() instanceof spoon.reflect.declaration.CtRecord) {
            log.debug("Skipping record constructor {}", constructor.getDeclaringType().getQualifiedName());
            return;
        }

        if (isUtilityConstructor(constructor)) {
            log.debug("Skipping utility constructor {}", constructor.getDeclaringType().getQualifiedName());
            return;
        }

        instrument(constructor, new InstrumentationUtils(getFactory()));
    }

    private boolean isUtilityConstructor(CtConstructor<?> constructor) {
        CtBlock<?> body = constructor.getBody();
        if (body == null) return false;

        List<CtStatement> statements = body.getStatements();
        return statements.size() == 1 && statements.get(0) instanceof CtThrow;
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

        // Add attributes
        for (CtAssignment<?, ?> assignment : constructor.getBody().getElements(new TypeFilter<>(CtAssignment.class))) {
            if (assignment.getAssigned() instanceof CtFieldAccess<?> fieldAccess) {
                String fieldName = fieldAccess.getVariable().getSimpleName();
                String fieldType = fieldAccess.getVariable().getType().getQualifiedName();
                String rhsType = getRightHandSideExpression(assignment.getAssignment(), constructor);

                assignment.insertAfter(
                        utils.createInvocation(
                                utilsAccess,
                                FQCN,
                                "addAttribute",
                                factory.Code().createLiteral(fieldName),
                                factory.Code().createLiteral(fieldType),
                                fieldAccess,
                                factory.Code().createLiteral(rhsType)
                        )
                );
            }
        }

        // Stack trace
        constructor.getBody().insertEnd(utils.createInvocation(utilsAccess, FQCN, "getStackTrace"));
        constructor.getBody().insertEnd(utils.createInvocation(utilsAccess, FQCN, "send"));
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
