package be.unamur.snail.tool;

import be.unamur.snail.core.Config;
import be.unamur.snail.core.Context;
import be.unamur.snail.utils.ReleaseDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fetches a JoularJX release JAR directly from GitHub
 * Example release url: <a href="https://github.com/joular/joularjx/releases/tag/3.0.1">...</a>
 */
public class JoularJXFetcher implements ToolReleaseFetcher {
    private static final Logger log = LoggerFactory.getLogger(JoularJXFetcher.class);
    private final ReleaseDownloader downloader;

    public JoularJXFetcher(ReleaseDownloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public String fetchRelease(Context context) throws Exception {
        Config config = Config.getInstance();

        String version = config.getExecutionPlan().getEnergyMeasurements().getToolVersion();
        String releaseUrl = config.getExecutionPlan().getEnergyMeasurements().getReleaseUrl();
        String targetDir = config.getExecutionPlan().getEnergyMeasurements().getToolPath();
        Path toolPath = Path.of(targetDir);
        log.info("Tool path: {}", toolPath);

        if (Files.exists(toolPath.resolve("joularjx-" + version + ".jar"))) {
            log.info("JoularJX v{} already downloaded at {}", version, toolPath);
            context.setEnergyToolPath(targetDir);
            return targetDir;
        }

        log.info("Fetching JoularJX v{} from GitHub release...", version);
        String baseDownloadUrl = String.format("%s/releases/download/%s", releaseUrl, version);

        URI jarUri = URI.create(baseDownloadUrl + "/joularjx-" + version + ".jar");
        URI configUri = URI.create(baseDownloadUrl + "/config.properties");

        downloader.downloadFile(jarUri, toolPath);
        downloader.downloadFile(configUri, toolPath);

        log.info("Downloaded JoularJX release to {}", toolPath);
        context.setEnergyToolPath(targetDir);
        context.setEnergyToolVersion(version);

        return targetDir;
    }
}
