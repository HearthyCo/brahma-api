package utils;

import gl.glue.brahma.plugins.StoragePlugin;

import java.io.File;
import java.util.Map;

public class FakeStoragePlugin implements StoragePlugin {

    private String prefix = "http://example.com/";

    @Override
    public String key2url(String key) {
        return prefix + key;
    }

    @Override
    public String url2key(String url) {
        return url.substring(prefix.length());
    }

    @Override
    public String putFile(String key, File file, Map<String, String> userMetadata) {
        return "application/octet-stream";
    }

    @Override
    public void removeFile(String key) {
        // Do nothing...
    }
}
