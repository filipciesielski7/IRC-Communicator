package com.example.client;

import com.example.client.structures.Room;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Controller {

    private Client client = new Client();

    @FXML
    private ChoiceBox choiceRoom;

    @FXML
    private Button joinRoom;

    @FXML
    private TextField username;

    @FXML
    private Button updateName;

    @FXML
    private Button logout;

    @FXML
    private Button connect;

    @FXML
    private ListView roomsList;

    @FXML
    private TextField messageInput;

    @FXML
    private TextField newRoomName;

    @FXML
    private Button addRoom;

    @FXML
    protected void onConnectButtonClick() {
        if (client.getUser().getUsername() == null && !username.getText().isEmpty()) {
            try {
                Socket socket = new Socket("localhost", 1234);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                client.setSocket(socket);
                client.setWriter(writer);

                System.out.println("Connected: " + socket);

                client.getUser().setUsername(username.getText());
                client.getUser().setConnected(true);

                if (client.getSocket() != null) {
                    client.getWriter().println("#0%" + client.getUser().getUsername()+"$");
                }

            } catch (IOException ex) {
                System.out.println("Can't connect to server!");
            }
        }
        else if (client.getUser().getUsername() == null && username.getText().isEmpty()){
            System.out.println("You have to type in your name before connecting to the server!");
        }
        else if (client.getUser().getUsername() != null){
            System.out.println("Already connected to the server!");
        }
    }

    @FXML
    private void onLogoutButtonClick() {
        if (client.getUser().isConnected()) {
            client.getUser().getRooms().forEach(x ->
                    client.getWriter().println("#3%;" + x.getRoomName()+"$")
            );

            try {
                System.out.println("Disconnected: " + client.getSocket());
                client.getSocket().close();
            } catch (IOException ex) {
                System.out.println("Cant' disconnect!");
            }

            client.getUser().setUsername(null);
            client.getUser().setConnected(false);
        }
        else{
            System.out.println("Already disconnected from the server!");
        }
    }

    @FXML
    private void onUpdateNameButtonClick() {
        if (client.getUser().isConnected() && !username.getText().isEmpty() && !username.getText().equals(client.getUser().getUsername())) {
            client.getUser().setUsername(username.getText());
            if (client.getSocket() != null) {
                client.getWriter().println("#0%" + client.getUser().getUsername()+"$");
            }
        }
        else{
            System.out.println("Can't change username!");
        }
    }

    @FXML
    private void onAddRoomButtonClick() {
        if (client.getUser().isConnected() && !newRoomName.getText().isEmpty()) { // sprawdzenie czy takie istnieje
            if (client.getSocket() != null) {
                client.getWriter().println("#1%" + newRoomName.getText()+"$");
                newRoomName.setText("");
            }
        }
        else if (client.getUser().isConnected() && newRoomName.getText().isEmpty()){
            System.out.println("You have to type in new room name before adding!");
        }
        else{
            System.out.println("Can't add room because you are not connected to the server!");
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
