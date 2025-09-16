package be.unamur.snail.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodInstrumentationProcessor extends AbstractProcessor<CtMethod<?>> {
    private static final String PKG = "be.unamur.snail.register.SendUtils";

    @Override
    public void process(CtMethod<?> method) {
        if (method.getBody() == null) return;

        String fileName = method.getPosition().getFile().getPath();
        String className = method.getDeclaringType().getQualifiedName();
        String methodName = method.getSimpleName();
        List<String> methodParameters = createMethodParameterList(method);

        CtInvocation<?> setMethodContextInvocation = createSetMethodContextInvocation(fileName, className, methodName, methodParameters);
        method.getBody().insertBegin(setMethodContextInvocation);
    }

    public List<String> createMethodParameterList(CtMethod<?> method) {
        List<String> methodParameters = new ArrayList<>();

        for (CtParameter<?> parameter : method.getParameters()) {
            methodParameters.add(parameter.getType().getQualifiedName());
        }
        return methodParameters;
    }

    public CtInvocation<?> createSetMethodContextInvocation(String fileName, String className, String methodName, List<String> methodParameters) {
        Factory factory = getFactory();
        CtTypeReference<?> registerUtilsType = factory.Type().createReference(PKG);
        CtExecutableReference<?> setMethodContextMethod = factory.Executable().createReference(
                registerUtilsType,
                factory.Type().voidPrimitiveType(),
                "setMethodContext"
        );
        CtExpression<ArrayList> parameterListLiteral = createParameterListLiteral(factory, methodParameters);

        return factory.Code().createInvocation(
                factory.Code().createTypeAccess(registerUtilsType),
                setMethodContextMethod,
                factory.Code().createLiteral(fileName),
                factory.Code().createLiteral(className),
                factory.Code().createLiteral(methodName),
                parameterListLiteral
        );
    }

    public CtExpression<ArrayList> createParameterListLiteral(Factory factory, List<String> methodParameters) {
        /*CtTypeReference<List> listTypeRef = factory.Type().createReference(List.class);
        CtTypeReference<String> stringTypeRef = factory.Type().createReference(String.class);
        listTypeRef.addActualTypeArgument(stringTypeRef);*/

        CtTypeReference<?> arraysType = factory.Type().createReference(Arrays.class);
        CtExecutableReference<?> asListMethod = factory.Executable().createReference(
                arraysType,
                factory.Type().createReference(List.class),
                "asList",
                factory.Type().createReference(Object[].class)
        );

        List<CtExpression<?>> parameterExpressions = new ArrayList<>();
        for (String parameter : methodParameters) {
            parameterExpressions.add(factory.Code().createLiteral(parameter));
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
