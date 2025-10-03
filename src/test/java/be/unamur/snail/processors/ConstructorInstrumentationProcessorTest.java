package be.unamur.snail.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
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

    @BeforeEach
    void setUp() {
        inputPath = Paths.get("src/test/resources/test-code-instrumentation/");
        outputPath = tempDir.resolve("output");

        launcher = new Launcher();
        launcher.addInputResource(inputPath.toString());
        launcher.setSourceOutputDirectory(outputPath.toString());
        launcher.addProcessor(new ConstructorInstrumentationProcessor());
        launcher.run();
        launcher.prettyprint();
    }

    @Test
    void constructorWithAssignmentsIsInstrumentedTest() {
        String className = "TestConstructorClassWithAssignments";
        Path outputFile = outputPath.resolve("test/" + className + ".java");
        assertTrue(outputFile.toFile().exists(), "Output file should be generated");
        System.out.println("Instrumented file: " + outputFile.toAbsolutePath());

        CtModel model = launcher.getModel();
        // Retrieve the processed constructor
        CtClass<?> clazz = model.getElements(new TypeFilter<>(CtClass.class))
                .stream()
                .filter(c -> c.getSimpleName().equals(className))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Class "+className+" not found in the model"));

        CtConstructor<?> constructor = clazz.getConstructors().iterator().next();
        CtBlock<?> body = constructor.getBody();
        List<CtStatement> statements = body.getStatements();
        System.out.println(statements);

        assertThat(statements.get(0).toString()).contains("SendConstructorsUtils utils = new be.unamur.snail.spoon.constructor_instrumentation.SendConstructorsUtils()");
        assertThat(statements.get(1).toString()).contains("utils.initConstructorContext(");
        assertThat(statements.get(2).toString()).contains("this.name = name");
        assertThat(statements.get(3).toString()).contains("utils.addAttribute(");
        assertThat(statements.get(4).toString()).contains("utils.getStackTrace()");
        assertThat(statements.get(5).toString()).contains("utils.send()");
    }
}