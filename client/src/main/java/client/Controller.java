package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextField textField;
    @FXML
    public TextArea textArea;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private static final int PORT = 8189;
    private static final String ADDRESS = "localhost";

    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickName;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickName = "";
        }
        textArea.clear();
        setTitle(nickName);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);

    }

    @FXML
    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());
        passwordField.clear();
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connect() {
        try {
            socket = new Socket(ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/auth_ok")) {
                                nickName = str.split(" ")[1];
                                setAuthenticated(true);
                                break;
                            }

                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }


                    // цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/clientlist")) {
                                String[] token = str.split(" ");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                        } else {
                            textArea.appendText(str + "\n");
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        String title;
        if (nickname.equals("")) {
            title = "Chatty";
        } else {
            title = String.format("Chatty_%s", nickname);
        }
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    @FXML
    public void clientListMouseAction(MouseEvent mouseEvent) {
        System.out.println(clientList.getSelectionModel().getSelectedItem());

        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText(String.format("/w %s ", receiver));
    }

    private void createRegStage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();


            regStage = new Stage();
            regStage.setTitle("Chatty_Reg");
            regStage.setScene(new Scene(root, 400, 300));

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegStage();
        }
        regStage.show();
    }

    public void registration(String login, String password, String nickname) {
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
