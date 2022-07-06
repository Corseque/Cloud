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

    private byte[] buffer;
    private final int BUFFER_SIZE = 256;

    private Path clientFilesDir;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            buffer = new byte[BUFFER_SIZE];
            clientFilesDir = Paths.get("D:\\");
                    //(System.getProperty("user.home")); //—сылка на домашнюю директорию
            updateClientView();
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Network created...");
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> readLoop());
//            readThread.setDaemon(true);
//            readThread.start();
            //Thread writeThread = new Thread(() -> uploadFile(filePathTest));
            //writeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Recieved: " + command);
                if (command.equals(FILE_LIST)) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    int filesCount = is.readInt();
                    for (int i = 0; i < filesCount; i++) {
                        String fileName = is.readUTF(); //можно собрать в лист и отсортировать по имени и типу (папка и файл)
                        Platform.runLater(() -> serverView.getItems().add(fileName));
                    }
                } else if (command.equals(UPLOAD_FILE)) {
                    String fileName = is.readUTF();
                    long fileSize = is.readLong();
                    try (OutputStream fos = new FileOutputStream(clientFilesDir.resolve(fileName).toFile())) {
                        for (int i = 0; i < (fileSize + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int readBytes = is.read(buffer);
                            fos.write(buffer, 0 , readBytes);
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
            Files.list(clientFilesDir)
                    .map(path -> path.getFileName().toString())
                    .forEach(file -> clientView.getItems().add(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        os.writeUTF(UPLOAD_FILE);
        os.writeUTF(fileName);
        Path file = clientFilesDir.resolve(fileName);
        long fileSize = Files.size(file);
        byte[] fileBytes = Files.readAllBytes(file);
        os.writeLong(fileSize);
        os.write(fileBytes);
        os.flush();
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF(DOWNLOAD_FILE);
        os.writeUTF(fileName);
        os.flush();
    }
}
