package constants;

public class Constants {
    /**
     * Server address
     */
    public static final String SERVER_ADDRESS = "localhost";

    /**
     * Server port
     */
    public static final int SERVER_PORT = 8189;

    /**
     *
     */
    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     *
     */
    public static final int BUFFER_SIZE = 256;

    /**
     * Command: update file list
     */
    public static final String FILES_LIST = "#list#";

    /**
     * Command: send file from client to server
     */
    public static final String UPLOAD_FILE = "#upload_file#";

    /**
     * Command: send files from client to server
     */
    public static final String UPLOAD_FILES = "#upload_files#";

    /**
     * Command: send file from server to client
     */
    public static final String DOWNLOAD_FILE = "#download_file#";

    /**
     * Command: send files from server to client
     */
    public static final String DOWNLOAD_FILES = "#download_files#";

    /**
     * Command: send path of the current directory
     */
    public static final String DIR_PATH = "#path#";

}
