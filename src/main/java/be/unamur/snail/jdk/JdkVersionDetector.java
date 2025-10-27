package be.unamur.snail.jdk;

import be.unamur.snail.exceptions.CouldNotParseBuildGradleException;
import be.unamur.snail.exceptions.CouldNotParsePomException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects the JDK version required by a Maven or Gradle project.
 */
public class JdkVersionDetector {
    public Optional<String> detectVersion(File repoPath) {
        File pomFile = new File(repoPath, "pom.xml");
        File gradleFile = new File(repoPath, "build.gradle");
        if (pomFile.exists()) {
            return detectFromPom(pomFile);
        } else if (gradleFile.exists()) {
            return detectFromGradle(gradleFile);
        }
        return Optional.empty();
    }

    private Optional<String> detectFromPom(File pomFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(pomFile);
            NodeList props = doc.getElementsByTagName("maven.compiler.source");
            if (props.getLength() == 0) {
                props = doc.getElementsByTagName("java.version");
            }
            if (props.getLength() > 0) {
                return Optional.of(props.item(0).getTextContent());
            }
        } catch (Exception e) {
            throw new CouldNotParsePomException();
        }
        return Optional.empty();
    }

    private Optional<String> detectFromGradle(File gradleFile) {
        try {
            String content = Files.readString(gradleFile.toPath());
            Matcher matcher = Pattern.compile("JavaVersion\\.VERSION_(\\d+)|sourceCompatibility\\s*=\\s*['\"](\\d+)['\"]").matcher(content);
            if (matcher.find()) {
                return matcher.group(1) != null
                        ? Optional.of(matcher.group(1))
                        : Optional.of(matcher.group(2));
            }
        } catch (IOException e) {
            throw new CouldNotParseBuildGradleException();
        }
        return Optional.empty();
    }
}
