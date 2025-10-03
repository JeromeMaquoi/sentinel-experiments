package be.unamur.snail.processors;

import be.unamur.snail.exceptions.ParameterIsNullOrEmptyException;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;

public class InstrumentationUtils {
    private final Factory factory;

    public InstrumentationUtils(final Factory factory) {
        this.factory = factory;
    }

    /**
     * Creates a local variable declaration for a given utility class
     * Example of generated code: SomeUtils utils = new SomeUtils();
     *
     * @param fqcn fully qualified class name of the utility type to instantiate
     * @param varName the name of the local variable
     * @return a CtLocalVariable representing the local variable declaration with initialization
     *
     * @throws ParameterIsNullOrEmptyException if fqcn or varName is null/empty
     */
    public CtLocalVariable<?> createConstructorInstantiationVariable(String fqcn, String varName) {
        if (fqcn == null || fqcn.isEmpty()) {
            throw new ParameterIsNullOrEmptyException("fqcn");
        }
        if (varName == null || varName.isEmpty()) {
            throw new ParameterIsNullOrEmptyException("varName");
        }
        CtTypeReference<?> utilsType = factory.Type().createReference(fqcn);
        CtConstructorCall constructorCall = factory.Code().createConstructorCall(utilsType);
        return factory.Code().createLocalVariable(utilsType, varName, constructorCall);
    }

    /**
     * Extracts the fully-qualified type names of a list of parameters.
     *
     * @param params the list of CtParameter elements
     * @return a list of type names (e.g., ["java.lang.String", "int"])
     *
     * @throws ParameterIsNullOrEmptyException if params is null
     */
    public List<String> getParameterTypes(List<? extends CtParameter<?>> params) {
        if (params == null) {
            throw new ParameterIsNullOrEmptyException("params");
        }
        List<String> result = new ArrayList<>();
        for (CtParameter<?> param : params) {
            result.add(param.getType().getQualifiedName());
        }
        return result;
    }

    /**
     * Creates a method invocation expression
     * Example generated code: target.methodName(arg1, arg2, ...);
     *
     * @param target the expression on chich the method should be invoked (can be a variable read, type access, etc.)
     * @param fqcn fully qualified class name where the method is declared
     * @param method the name of the method to invoke
     * @param args arguments to pass to te method invocation
     * @return a CtInvocation representing the method call
     *
     * @throws ParameterIsNullOrEmptyException if fqcn or method is null/empty
     */
    public CtInvocation<?> createInvocation(CtExpression<?> target, String fqcn, String method, CtExpression<?>... args) {
        if (fqcn == null || fqcn.isEmpty()) {
            throw new ParameterIsNullOrEmptyException("fqcn");
        }
        if (method == null || method.isEmpty()) {
            throw new ParameterIsNullOrEmptyException("method");
        }
        CtTypeReference<?> type = factory.Type().createReference(fqcn);
        CtExecutableReference<?> methodRef = factory.Executable().createReference(
                type,
                factory.Type().voidPrimitiveType(),
                method
        );
        return factory.Code().createInvocation(target, methodRef, args);
    }

    /**
     * Creates an expression representing a new ArrayList<String> initialized with the given values.
     * Example of generated code: new ArrayList(Arrays.asList("a", "b", "c"))
      *
     * @param values the list of string literals to insert into the list
     * @return a CtExpression representing the constructor call that creates the list
     *
     * @throws ParameterIsNullOrEmptyException if values is null
     */
    public CtExpression<ArrayList> createStringListLiteral(List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new ParameterIsNullOrEmptyException("values");
        }
        CtTypeReference<?> arraysType = factory.Type().createReference(ArrayList.class);
        CtExecutableReference<?> asListMethod = factory.Executable().createReference(
                arraysType,
                factory.Type().createReference(List.class),
                "asList",
                factory.Type().createReference(Object[].class)
        );
        List<CtExpression<?>> parameterExpressions = new ArrayList<>();
        for (String value : values) {
            parameterExpressions.add(factory.Code().createLiteral(value));
        }

        CtInvocation<?> asListInvocation = factory.Code().createInvocation(
                factory.Code().createTypeAccess(arraysType),
                asListMethod,
                parameterExpressions
        );

        CtTypeReference<ArrayList> listStringTypeRef = factory.Type().createReference(ArrayList.class);
        return factory.Code().createConstructorCall(listStringTypeRef, asListInvocation);
    }
}
