package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static constants.Constants.*;

public class Client implements Initializable {

    public ListView<String> serverView;
    public ListView<String> clientView;
    public TextField serverPath;
    public TextField clientPath;

    private byte[] buf;

    private Path clientDir;
    private DataInputStream is;
    private DataOutputStream os;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            buf = new byte[BUFFER_SIZE];
            clientDir = Paths.get("D:\\");
            clientPath.setText(clientDir.toString());
                    //(System.getProperty("user.home")); //—сылка на домашнюю директорию
            updateClientView();
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Network created...");
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Recieved: " + command);
                if (command.equals(FILES_LIST)) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    serverPath.setText(is.readUTF());
                    int filesCount = is.readInt();
                    for (int i = 0; i < filesCount; i++) {
                        String fileName = is.readUTF(); //можно собрать в лист и отсортировать по имени и типу (папка и файл)
                        Platform.runLater(() -> serverView.getItems().add(fileName));
                    }
                } else if (command.equals(UPLOAD_FILE)) {
                    String fileName = is.readUTF();
                    System.out.println("Recieved: " + fileName);
                    long fileSize = is.readLong();
                    try (OutputStream fos = new FileOutputStream(clientDir.resolve(fileName).toFile())) {
                        for (int i = 0; i < (fileSize + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int readBytes = is.read(buf);
                            fos.write(buf, 0 , readBytes);
                        }
                    }
                    Platform.runLater(this::updateClientView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        os.writeUTF(UPLOAD_FILE);
        os.writeUTF(fileName);
        Path file = clientDir.resolve(fileName);
        long fileSize = Files.size(file);
        os.writeLong(fileSize);
        byte[] bytes;
        if (fileSize > MAX_ARRAY_SIZE) {
            bytes = new byte[MAX_ARRAY_SIZE];
            try  (InputStream fis = new FileInputStream(file.toFile())) {
                for (int i = 0; i < (fileSize + MAX_ARRAY_SIZE - 1) / MAX_ARRAY_SIZE; i++) {
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

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF(DOWNLOAD_FILE);
        os.writeUTF(fileName);
        os.flush();
    }

    public void clientFolderUp(ActionEvent actionEvent) {
    }

    public void serverFolderUp(ActionEvent actionEvent) {
    }
}
