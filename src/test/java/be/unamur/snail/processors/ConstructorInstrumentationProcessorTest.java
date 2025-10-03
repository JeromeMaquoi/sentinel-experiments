package be.unamur.snail.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.CodeFactory;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class ConstructorInstrumentationProcessorTest {
    private Launcher launcher;

    Path inputPath;

    ConstructorInstrumentationProcessor processor;

    Factory factory;

    TypeFactory typeFactory;

    CodeFactory codeFactory;

    String PKG;

    CtTypeReference mockTypeRef;

    CtConstructorCall mockCall;

    CtLocalVariable mockLocalVariable;

    @BeforeEach
    void setUp() {
        inputPath = Paths.get("src/test/resources/test-inputs/");
        launcher = new Launcher();
        factory = mock(Factory.class);
        typeFactory = mock(TypeFactory.class);
        codeFactory = mock(CodeFactory.class);
        processor = new ConstructorInstrumentationProcessor(){
            @Override
            public Factory getFactory() {
                return factory;
            }
        };
        PKG = "be.unamur.snail.spoon.constructor_instrumentation.SendConstructorsUtils";

        when(factory.Type()).thenReturn(typeFactory);
        when(factory.Code()).thenReturn(codeFactory);

        mockTypeRef = mock(CtTypeReference.class);
        mockCall = mock(CtConstructorCall.class);
        mockLocalVariable = mock(CtLocalVariable.class);
        when(typeFactory.createReference(PKG)).thenReturn(mockTypeRef);
        when(codeFactory.createConstructorCall(mockTypeRef)).thenReturn(mockCall);
        when(codeFactory.createLocalVariable(mockTypeRef, "utils", mockCall)).thenReturn(mockLocalVariable);
    }

//    @Test
//    void createConstructorParameterListTest() {
//        CtConstructor<?> mockConstructor = mock(CtConstructor.class);
//        CtParameter<?> mockParameter = mock(CtParameter.class);
//        CtTypeReference mockTypeRef  = mock(CtTypeReference.class);
//
//        when(mockTypeRef.getQualifiedName()).thenReturn("java.lang.String");
//        when(mockParameter.getType()).thenReturn(mockTypeRef);
//        when(mockConstructor.getParameters()).thenReturn(List.of(mockParameter));
//
//        List<String> actualResult = processor.createConstructorParameterList(mockConstructor);
//        assertEquals(List.of("java.lang.String"), actualResult);
//    }
//
//    @Test
//    void createSendUtilsInitializationInvocationTest() {
//        CtLocalVariable<?> actualResult = processor.createSendUtilsInitializationInvocation();
//        assertEquals(mockLocalVariable, actualResult);
//
//        verify(factory.Type()).createReference(PKG);
//        verify(codeFactory).createConstructorCall(mockTypeRef);
//        verify(codeFactory).createLocalVariable(mockTypeRef, "utils", mockCall);
//    }
}