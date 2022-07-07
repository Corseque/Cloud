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

    private final Path rootClientDir;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final byte[] buf;

    public ClientHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        rootClientDir = Paths.get("C:/Users/Corse/IdeaProjects/CloudStorage/data");
        buf = new byte[BUFFER_SIZE];
        sendServerFiles();
    }

    public void sendServerFiles() throws IOException {
        List<String> files = Files.list(rootClientDir)
                .map(p -> p.getFileName().toString()).toList();
        os.writeUTF(FILES_LIST);
        os.writeUTF(rootClientDir.toString());
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
                    try (OutputStream fos = new FileOutputStream(rootClientDir.resolve(fileName).toFile())) {
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
                    Path file = rootClientDir.resolve(fileName);
                    long size = Files.size(file);
                    os.writeLong(size);
                    byte[] bytes;
                    if (size > MAX_ARRAY_SIZE) {
                        bytes = new byte[MAX_ARRAY_SIZE];
                        try  (InputStream fis = new FileInputStream(file.toFile())) {
                            for (int i = 0; i < (size + MAX_ARRAY_SIZE - 1) / MAX_ARRAY_SIZE; i++) {
                                int readBytes = fis.read(bytes);
                                os.write(bytes, 0, readBytes);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bytes = Files.readAllBytes(file);
                        os.write(bytes);
                    }
                    os.flush();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
