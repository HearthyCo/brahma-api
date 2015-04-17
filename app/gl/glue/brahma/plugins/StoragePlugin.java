package gl.glue.brahma.plugins;

import java.io.File;
import java.util.Map;

public interface StoragePlugin {

    /**
     * Gets the corresponding URL for the specified key/path.
     * @param key A stored file key/path
     * @return The specified file's URL
     */
    public String key2url(String key);

    /**
     * Gets the corresponding key for the specified URL. Opposite of key2url.
     * @param url A stored file URL
     * @return The specified file's key
     */
    public String url2key(String url);

    /**
     * Upload a file to the storage service.
     * @param key Target key/path for the file
     * @param file File to upload
     * @param userMetadata Other metadata to add to the upload
     * @return The mime type of the file
     */
    public String putFile(String key, File file, Map<String, String> userMetadata);

    /**
     * Remove a file from the storage service.
     * @param key Current file key/path
     */
    public void removeFile(String key);

}

