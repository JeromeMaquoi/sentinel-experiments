package be.unamur.snail.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public interface ReleaseDownloader {
    /**
     * Downloads a file from a given URL into a destination directory
     * @param uri the url from which the file needs to be downloaded
     * @param destinationDir the destination directory of the download
     * @return the path of the downloaded file
     * @throws IOException if something occurs during the download
     */
    Path downloadFile(URI uri, Path destinationDir) throws IOException, InterruptedException;
}
