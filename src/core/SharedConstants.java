package core;

public class SharedConstants {
    public static final int DEFAULT_PORT = 6000;
    public static final int FILE_BUFFER_SIZE = 4096; // 4KB chunks
    public static final int READ_TIMEOUT = 5000; // 5 seconds
    public static final int CONNECT_TIMEOUT = 3000; // 3 seconds
    
    // Commands
    public static final String CMD_HELLO = "HELLO";
    public static final String CMD_LIST = "LIST_FILES";
    public static final String CMD_GET = "GET_FILE";
    
    // Responses
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
}
