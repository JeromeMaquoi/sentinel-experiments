package be.unamur.snail.utils;

import be.unamur.snail.exceptions.SurefirePluginNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Utility class for modifying Maven pom.xml files to inject a javaagent in the
 * test maven phase
 */
public class MavenPomModifier {
    /**
     * Injects the javaagent line into the pom.xml's surefire plugin
     * @param pomFile the pom file of the analyzed project
     * @param energyToolPath the tool path to inject as a java agent
     * @return a backup file of the original POM so it can be restored later
     */
    public static File injectJavaAgent(File pomFile, String energyToolPath) throws Exception {
        File backup = new File(pomFile.getAbsolutePath() + ".bak");
        copyFile(pomFile, backup);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(pomFile);

        Element surefirePlugin = findSurefirePlugin(doc);
        if (surefirePlugin == null) {
            throw new SurefirePluginNotFoundException();
        }

        Element config = getOrCreateChild(doc, surefirePlugin, "configuration");
        Element argLine = getOrCreateChild(doc, config, "argLine");

        String newArgLine = "-javaagent:" + energyToolPath;
        if (!argLine.getTextContent().contains("-javaagent")) {
            argLine.setTextContent(argLine.getTextContent().trim() + " " + newArgLine);
        }

        // Write the modified POM
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(pomFile));

        return backup;
    }

    private static Element getOrCreateChild(Document doc, Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) return (Element) nodes.item(0);
        Element child = doc.createElement(tagName);
        parent.appendChild(child);
        return child;
    }

    private static Element findSurefirePlugin(Document doc) {
        NodeList plugins = doc.getElementsByTagName("plugin");
        for (int i = 0; i < plugins.getLength(); i++) {
            Element plugin = (Element) plugins.item(i);
            Node artifactIdNode = plugin.getElementsByTagName("artifactId").item(0);
            if (artifactIdNode != null && "maven-surefire-plugin".equals(artifactIdNode.getTextContent().trim())) {
                return plugin;
            }
        }
        return null;
    }

    private static void copyFile(File from, File to) throws IOException {
        try(InputStream in = new FileInputStream(from);
        OutputStream out = new FileOutputStream(to)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
