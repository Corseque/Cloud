package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static constants.Constants.SERVER_PORT;

public class Server {

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
            while (true) {
                System.out.println("Waiting for a client...");
                Socket socket = server.accept();
                System.out.println("New client connected...");
                new Thread((new ClientHandler(socket))).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
