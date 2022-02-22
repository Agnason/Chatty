package server;

import org.omg.CORBA.StringHolder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private String nickName;


    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/auth")) {
                                String[] token = str.split(" ", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server.getAuthService()
                                        .getNicknameByLoginAndPassword(token[1], token[2]);
                                if ((newNick != null)) {
                                    nickName = newNick;
                                    sendMsg("/auth_ok " + nickName);
                                    authenticated = true;
                                    server.subscribe(this);
                                    break;
                                } else {
                                    sendMsg("Логин/пароль не совпали");
                                }
                            }
                        }
                    }
                    // цикл работы
                    while (authenticated) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/w ")) {
                                String[] token = str.split(" ", 3);

                                if (token.length < 3) {
                                    continue;
                                }
                                server.broadcastMsgToNickname(this, token[1], token[2]);
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return nickName;
    }
}
