package be.unamur.snail.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class ConstructorInstrumentationProcessorTest {
    private Launcher launcher;
    Path inputPath;
    @TempDir
    Path tempDir;
    Path outputPath;
    private ConstructorInstrumentationProcessor processor;
    private CtConstructor<?> constructor;
    private Factory factory;

    @BeforeEach
    void setUp() {
        inputPath = Paths.get("src/test/resources/test-code-instrumentation/");
        outputPath = tempDir.resolve("output");
        processor = new ConstructorInstrumentationProcessor();

        launcher = new Launcher();
        launcher.addInputResource(inputPath.toString());
        launcher.setSourceOutputDirectory(outputPath.toString());
        launcher.addProcessor(processor);

        factory = launcher.getFactory();
        constructor = factory.Core().createConstructor();
        CtParameter<String> param = factory.createParameter(
                constructor,
                factory.Type().stringType(),
                "param"
        );
        constructor.addParameter(param);
    }

    @Test
    void constructorWithAssignmentsIsInstrumentedTest() {
        launcher.run();
        launcher.prettyprint();

        String className = "TestConstructorClassWithAssignments";
        Path outputFile = outputPath.resolve("test/" + className + ".java");
        assertTrue(outputFile.toFile().exists(), "Output file should be generated");

        CtModel model = launcher.getModel();
        // Retrieve the processed constructor
        CtClass<?> clazz = model.getElements(new TypeFilter<>(CtClass.class))
                .stream()
                .filter(c -> c.getSimpleName().equals(className))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Class "+className+" not found in the model"));

        CtConstructor<?> expectedConstructor = clazz.getConstructors().iterator().next();
        CtBlock<?> body = expectedConstructor.getBody();
        List<CtStatement> statements = body.getStatements();
        System.out.println(statements);

        assertThat(statements.get(1).toString()).contains("SendConstructorsUtils utils = new be.unamur.snail.spoon.constructor_instrumentation.SendConstructorsUtils()");
        assertThat(statements.get(2).toString()).contains("utils.initConstructorContext(");
        assertThat(statements.get(3).toString()).contains("this.field1 = field1");
        assertThat(statements.get(4).toString()).contains("utils.addAttribute(\"field1\", \"java.lang.String\", this.field1, \"constructor parameter\"");
        assertThat(statements.get(5).toString()).contains("this.field2 = field2");
        assertThat(statements.get(6).toString()).contains("utils.addAttribute(\"field2\", \"int\", this.field2, \"constructor parameter\"");
        assertThat(statements.get(7).toString()).contains("utils.getStackTrace()");
        assertThat(statements.get(8).toString()).contains("utils.send()");
    }

    @Test
    void getRightHandSideExpressionReturnsConstructorCallTest() {
        CtExpression<?> expr = factory.Code().createConstructorCall(factory.Type().createReference("java.lang.String"));
        String result = processor.getRightHandSideExpression(expr, constructor);
        assertEquals("constructor call", result);
    }
}