package data;

import model.SharedFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {
    private final File sharedDirectory;
    // Map of filename -> SharedFile (for local files)
    private final Map<String, SharedFile> localFiles = new ConcurrentHashMap<>();

    // Map of Peer -> List of their files (for remote view)
    // Using simple Object key or String key for now, but ideally Peer object.
    // However, Peer object equality depends on host/port.
    private final Map<String, List<SharedFile>> remoteFiles = new ConcurrentHashMap<>();

    public FileManager(String sharedFolderPath) {
        this.sharedDirectory = new File(sharedFolderPath);
        if (!sharedDirectory.exists()) {
            sharedDirectory.mkdirs();
        }
        refreshLocalFiles();
    }

    public void refreshLocalFiles() {
        localFiles.clear();
        File[] files = sharedDirectory.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    localFiles.put(f.getName(), new SharedFile(f.getName(), f.length(), f.getAbsolutePath()));
                }
            }
        }
    }

    public List<SharedFile> getLocalFiles() {
        return new ArrayList<>(localFiles.values());
    }

    public SharedFile getLocalFile(String name) {
        return localFiles.get(name);
    }

    public File getSharedDirectory() {
        return sharedDirectory;
    }

    public void updateRemoteFiles(String peerAddress, List<SharedFile> files) {
        remoteFiles.put(peerAddress, files);
    }

    public List<SharedFile> getRemoteFiles(String peerAddress) {
        return remoteFiles.getOrDefault(peerAddress, new ArrayList<>());
    }

    // Get all remote files flattened or organized? Let's just expose the map for
    // now via specific methods.
    public Map<String, List<SharedFile>> getAllRemoteFiles() {
        return remoteFiles;
    }

    public void addFile(File sourceFile) throws java.io.IOException {
        if (sourceFile.exists()) {
            File destFile = new File(sharedDirectory, sourceFile.getName());
            java.nio.file.Files.copy(sourceFile.toPath(), destFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            refreshLocalFiles();
        }
    }

}
