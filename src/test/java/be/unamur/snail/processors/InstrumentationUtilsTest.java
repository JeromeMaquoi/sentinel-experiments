package be.unamur.snail.processors;

import be.unamur.snail.exceptions.ParameterIsNullOrEmptyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InstrumentationUtilsTest {
    private Factory factory;
    private InstrumentationUtils utils;

    @BeforeEach
    void setUp() {
        Launcher launcher = new Launcher();
        this.factory = launcher.getFactory();
        this.utils = new InstrumentationUtils(factory);
    }

    @Test
    void createConstructorInstantiationVariableShouldThrowExceptionIfFqcnOrVarNameIsNullTest() {
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createConstructorInstantiationVariable(null, "utils"));
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createConstructorInstantiationVariable("ClassName", null));
    }

    @Test
    void createConstructorInstantiationVariableShouldThrowExceptionIfFqcnOrVarNameIsEmptyTest() {
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createConstructorInstantiationVariable("", "var"));
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createConstructorInstantiationVariable("ClassName", ""));
    }

    @Test
    void createConstructorInstantiationVariableWithSimpleConstructorShouldSucceedTest() {
        CtLocalVariable<?> variable = utils.createConstructorInstantiationVariable("be.unamur.snail.sentinel.SomeClass", "utils");
        assertNotNull(variable);
        assertEquals("be.unamur.snail.sentinel.SomeClass", variable.getType().getQualifiedName());
        assertEquals("utils", variable.getSimpleName());
        assertTrue(variable.getAssignment().toString().contains("be.unamur.snail.sentinel.SomeClass()"));
        assertEquals("be.unamur.snail.sentinel.SomeClass utils = new be.unamur.snail.sentinel.SomeClass()", variable.toString());
    }

    @Test
    void getParameterTypesShouldThrowExceptionIfParamsIsNullTest() {
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.getParameterTypes(null));
    }

    @Test
    void getParameterTypesShouldSucceedTest() {
        CtTypeReference<String> stringType = factory.Type().createReference(String.class);
        CtParameter<String> param = factory.Core().createParameter();
        param.setType(stringType);

        List<String> result = utils.getParameterTypes(List.of(param));
        assertEquals(List.of("java.lang.String"), result);
    }

    @Test
    void getParameterTypeShouldReturnEmptyListWithEmptyParametersTest() {
        List<String> result = utils.getParameterTypes(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void createInvocationShouldThrowExceptionIfFqcnOrVarNameIsNullTest() {
        CtExpression<?> target = factory.Code().createVariableRead(
                factory.Code().createLocalVariable(factory.Type().createReference("java.util.ArrayList"), "list", null).getReference(),
                false
        );
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createInvocation(target, null, "methodName"));
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createInvocation(target, "someClass", null));
    }

    @Test
    void createInvocationShouldThrowExceptionIfFqcnOrVarNameIsEmptyTest() {
        CtExpression<?> target = factory.Code().createVariableRead(
                factory.Code().createLocalVariable(factory.Type().createReference("java.util.ArrayList"), "list", null).getReference(),
                false
        );
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createInvocation(target, "", "methodName"));
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createInvocation(target, "class", ""));
    }

    @Test
    void createInvocationShouldSucceedTest() {
        CtExpression<?> target = factory.Code().createVariableRead(
                factory.Code().createLocalVariable(factory.Type().createReference("java.util.ArrayList"), "list", null).getReference(),
                false
        );

        CtInvocation<?> invocation = utils.createInvocation(target, "someClass", "methodName");
        assertNotNull(invocation);
        assertEquals("methodName", invocation.getExecutable().getSimpleName());
        assertEquals("someClass", invocation.getExecutable().getDeclaringType().getQualifiedName());
    }

    @Test
    void createStringListLiteralShouldThrowExceptionIfValuesIsNullTest() {
        assertThrows(ParameterIsNullOrEmptyException.class, () -> utils.createStringListLiteral(null));
    }

    @Test
    void createStringListLiteralShouldSucceedTest() {
        List<String> values = List.of("a", "b", "c");
        CtExpression<ArrayList> expression = utils.createStringListLiteral(values);

        assertNotNull(expression);
        assertEquals("new java.util.ArrayList(java.util.Arrays.asList(\"a\", \"b\", \"c\"))", expression.toString());
    }
}