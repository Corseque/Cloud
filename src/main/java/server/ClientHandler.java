package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static constants.Constants.*;

public class ClientHandler implements Runnable{

    private byte[] buf;
    private final int BUFFER_SIZE = 256;

    private Path clientHomeDir;
    private DataInputStream is;
    private DataOutputStream os;

    public ClientHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        clientHomeDir = Paths.get("src/main/java/server/test_client_dir");
        buf = new byte[BUFFER_SIZE];
        sendServerFiles();
    }

    public void sendServerFiles() throws IOException {
        List<String> files = Files.list(clientHomeDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        os.writeUTF(FILE_LIST);
        os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
        os.flush();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Received: " + command);
                if (command.equals(UPLOAD_FILE)) {
                    String fileName = is.readUTF();
                    long fileSize = is.readLong();
                    try (OutputStream fos = new FileOutputStream(clientHomeDir.resolve(fileName).toFile())) {
                        for (int i = 0; i < (fileSize + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int readBytes = is.read(buf);
                            fos.write(buf, 0 , readBytes);
                        }
                    }
                    sendServerFiles();
                } else if (command.equals(DOWNLOAD_FILE)) {
                    String fileName = is.readUTF();
                    os.writeUTF(UPLOAD_FILE);
                    os.writeUTF(fileName);
                    Path file = clientHomeDir.resolve(fileName);
                    long fileSize = Files.size(file);
                    byte[] fileBytes = Files.readAllBytes(file);
                    os.writeLong(fileSize);
                    os.write(fileBytes);
                    os.flush();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
