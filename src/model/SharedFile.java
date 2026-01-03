package model;

import java.io.Serializable;

public class SharedFile implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private long size;
    // For local files, we might need the full path. For remote, we just know the name/size.
    private transient String localPath; 

    public SharedFile(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public SharedFile(String name, long size, String localPath) {
        this.name = name;
        this.size = size;
        this.localPath = localPath;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getLocalPath() {
        return localPath;
    }

    @Override
    public String toString() {
        return name + " (" + size + " bytes)";
    }
}
