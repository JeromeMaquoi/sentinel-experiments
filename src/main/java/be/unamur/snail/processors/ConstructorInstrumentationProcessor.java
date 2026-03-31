package be.unamur.snail.processors;

import be.unamur.snail.core.Config;
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

    protected boolean isUtilityConstructor(CtConstructor<?> constructor) {
        CtBlock<?> body = constructor.getBody();
        if (body == null) return false;

        long meaningfulStatements = body.getStatements().stream()
                .filter(statement -> !isImplicitSuperCall(statement) && !(statement instanceof CtThrow))
                .count();

        long throwCount = body.getStatements().stream()
                .filter(statement -> statement instanceof CtThrow)
                .count();

        // If there are no meaningful statements, and at least one throw statement, consider it a utility constructor
        return meaningfulStatements == 0 && throwCount >= 1;
    }

    protected boolean isImplicitSuperCall(CtStatement statement) {
        if (statement instanceof CtInvocation<?> invocation) {
            return invocation.getExecutable().getSimpleName().equals("<init>") && invocation.getExecutable().getDeclaringType().getQualifiedName().equals("java.lang.Object");
        }
        return false;
    }

    @Override
    public void instrument(CtConstructor<?> constructor, InstrumentationUtils utils) {
        Factory factory = getFactory();

        String className = constructor.getDeclaringType().getQualifiedName();
        String constructorName = constructor.getDeclaringType().getSimpleName();
        List<String> params = utils.getParameterTypes(constructor.getParameters());
        log.debug("Found constructor {} with parameters {}", constructorName, params);

        // Get commit data from Config
        Config config = Config.getInstance();
        String commitSha = config.getRepo().getCommit();
        String projectName = config.getProject().getName();
        String projectOwner = config.getProject().getOwner();

        // Generate code to create CommitSimpleInstrDTO in instrumented code
        String commitCode = generateCommitInstrDTOCode(commitSha, projectName, projectOwner);

        // Get file name with <project-name>_<commit-sha>
        String targetDir = config.getRepo().getTargetDir();
        String fileName = getFilePath(constructor, targetDir, commitSha);

        CtLocalVariable<?> utilsVariable = utils.createThreadLocalUtilsVariable(FQCN, "utils");
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
                        utils.createStringListLiteral(params),
                        factory.Code().createCodeSnippetExpression(commitCode)
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

    /**
     * Returns the file path of the constructor within the copy of the project, inside the 'project-name_commit-sha' folder
     * @param constructor Constructor for which we want to get the file path
     * @param targetDir Target directory of the project
     * @param commitSha SHA of the commit
     * @return The file path of the constructor
     */
    public String getFilePath(CtConstructor<?> constructor, String targetDir, String commitSha) {
        String fileName = "Unknown File";
        if (constructor.getPosition() != null && constructor.getPosition().getFile() != null) {
            String newTargetDir = targetDir + "_" + commitSha;
            String srcConstructorPath = constructor.getPosition().getFile().getPath().replace(targetDir, "");
            fileName = newTargetDir + srcConstructorPath;
        }
        return fileName;
    }

    /**
     * Generates Java code string that creates a CommitSimpleInstrDTO with the given values.
     * @param sha commit SHA hash
     * @param projectName project name
     * @param projectOwner project owner
     * @return Java code string that instantiates the CommitSimpleInstrDTO
     */
    private String generateCommitInstrDTOCode(String sha, String projectName, String projectOwner) {
        return String.format(
            "new be.unamur.snail.spoon.constructor_instrumentation.CommitSimpleInstrDTO(\"%s\", " +
            "new be.unamur.snail.spoon.constructor_instrumentation.RepositorySimpleInstrDTO(\"%s\", \"%s\"))",
            sha, projectName, projectOwner
        );
    }
}
