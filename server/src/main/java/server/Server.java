package server;

import com.sun.security.ntlm.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[%s]: %s", sender.getNickName(), msg);

        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    // метод, для отправки сообщения конкретному человеку
    public void broadcastMsgToNickname(ClientHandler sender, String receiver, String msg) {
        String message = String.format("%s to %s: %s", sender.getNickName(), receiver, msg);

        for (ClientHandler c : clients) {
            if (c.getNickName().equals(receiver)) {
                c.sendMsg(message);
                if (!sender.getNickName().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg("Получатель " + receiver + " не найден");
    }


    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
