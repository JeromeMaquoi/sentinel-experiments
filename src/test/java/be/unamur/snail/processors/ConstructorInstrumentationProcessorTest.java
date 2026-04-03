package be.unamur.snail.processors;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void setUp() throws Exception {
        inputPath = Paths.get("src/test/resources/test-code-instrumentation/");
        outputPath = tempDir.resolve("output");
        processor = new ConstructorInstrumentationProcessor();

        launcher = new Launcher();
        launcher.addInputResource(inputPath.toString());
        launcher.setSourceOutputDirectory(outputPath.toString());
        launcher.addProcessor(processor);

        launcher.getEnvironment().setNoClasspath(true);

        factory = launcher.getFactory();
        constructor = factory.Core().createConstructor();
        CtParameter<String> param = factory.createParameter(
                constructor,
                factory.Type().stringType(),
                "param"
        );
        constructor.addParameter(param);

        Config.reset();
        Path yaml = tempDir.resolve("config.yaml");
        Files.writeString(yaml, """
            project:
              sub-project: ""
              name: "test"
            repo:
              url: "https://example.com/repo.git"
              commit: "123abc"
              target-dir: "/tmp/repo"
            log:
              level: "DEBUG"
            backend:
              server-host: localhost
              server-port: 8080
              server-timeout-seconds: 120
              nb-check-server-start: 5
              server-log-path: "/tmp/sentinel-backend.log"
              server-ready-path: "/tmp/backend-ready"
        """);
        Config.load(yaml.toString());
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

        assertThat(statements.get(1).toString()).contains("SendConstructorsUtils utils = be.unamur.snail.spoon.constructor_instrumentation.SendConstructorsUtils.getInstance()");
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

    @Test
    void getFilePathWorkingTest() {
        CtConstructor<?> mockConstructor = mock(CtConstructor.class);
        SourcePosition position = mock(SourcePosition.class);
        when(mockConstructor.getPosition()).thenReturn(position);
        File file = mock(File.class);
        when(position.getFile()).thenReturn(file);
        when(file.getPath()).thenReturn("/path/to/testProject_instrumentation_abc123/constructor/file.java");

        String filePath = processor.getFilePath(mockConstructor);
        assertEquals("/path/to/testProject_instrumentation_abc123/constructor/file.java", filePath);
    }

    @Test
    void getFilePathUnknownFileTest() {
        CtConstructor<?> mockConstructor = mock(CtConstructor.class);
        when(mockConstructor.getPosition()).thenReturn(null);
        assertEquals("Unknown File",  processor.getFilePath(mockConstructor));

        SourcePosition position = mock(SourcePosition.class);
        when(mockConstructor.getPosition()).thenReturn(position);
        when(position.getFile()).thenReturn(null);
        assertEquals("Unknown File",  processor.getFilePath(mockConstructor));
    }
}