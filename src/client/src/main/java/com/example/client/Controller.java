package com.example.client;

import com.example.client.structures.Message;
import com.example.client.structures.Room;
import com.example.client.structures.User;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.scene.control.*;

public class Controller implements Initializable {

    private Client client = new Client();
    private Stage stage;
    private ResponseFromServer responseFromServer;
    private Room activeRoom;
    private User activeUser;
    private String chosenRoom;

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
    private ListView<Room> roomsList;

    @FXML
    private ListView<User> usersList;

    @FXML
    private TextField messageInput;

    @FXML
    private TextArea allMessages;

    @FXML
    private TextField newRoomName;

    @FXML
    private Button addRoom;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (allMessages != null) {
            allMessages.textProperty().addListener((observable, oldValue, newValue) -> {
                allMessages.setText(newValue);
                displayUsersList(activeRoom);
            });
        }

        roomsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Room>() {
            @Override
            public void changed(ObservableValue<? extends Room> observableValue, Room oldRoom, Room newRoom) {
                if (newRoom != null) {
                    allMessages.clear();
                    activeRoom = newRoom;
                    displayUsersList(activeRoom);

                    activeRoom.getMessages().forEach(message -> {
                        if (message.getRoomName().equals(activeRoom.getRoomName())) {
                            updateMessage(message.textFormat());
                        }
                    });
                }
                else{
                    displayUsersList(activeRoom);
                }
            }
        });

        usersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends User> observableValue, User oldUser, User newUser) {
                if (newUser != null) {
                    activeUser = newUser;
                }
            }
        });

        choiceRoom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String newRoom) {
                chosenRoom = newRoom;
            }
        });
    }

    @FXML
    public void onJoinRoomButtonClick() {
        if (chosenRoom != null) {
            Room result = null;
            for (Room c : client.getAllRooms()) {
                if (chosenRoom.equals(c.getRoomName())) {
                    result = c;
                    break;
                }
            }
            client.getWriter().println("#2%" + result.getRoomName() + "$");
        }
    }

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
                    this.responseFromServer = new ResponseFromServer(this.client);
                    responseFromServer.setStopped(false);
                    Thread t = new Thread(this.responseFromServer);
                    t.start();

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
            client.getUser().getRooms().forEach(x -> {
                    client.getWriter().println("#3%" + x.getRoomName()+"$");
                }
            );

            try {
                System.out.println("Disconnected: " + client.getSocket());
                responseFromServer.setStopped(true);
                client.getSocket().close();
                stage.close();
            } catch (IOException ex) {
                System.out.println("Cant' disconnect!");
            } catch (Exception e){
            }

            client.getUser().setUsername(null);
            client.getUser().setConnected(false);
        }
        else{
            System.out.println("Already disconnected from the server!");
            stage.close();
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
        if (client.getUser().isConnected() && !newRoomName.getText().isEmpty()) {
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

    public void displayRoomsList(Boolean empty) {
//        System.out.println("Rooms list: " + client.getUser().getRooms());
        if(empty){
            choiceRoom.getItems().clear();
            activeRoom = null;
            activeUser = null;
        }
        else{
            roomsList.setItems(client.getUser().getRooms());

            roomsList.setCellFactory(param -> new ListCell<Room>() {
                @Override
                protected void updateItem(Room item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.getRoomName() == null) {
                        setText(null);
                    } else {
                        setText(item.getRoomName());
                    }
                }
            });

            client.allRooms.forEach(x -> {
                if (!choiceRoom.getItems().contains(x.getRoomName())) {
                    choiceRoom.getItems().add(x.getRoomName());
                }
            });
        }
    }

    public void displayUsersList(Room activeRoom) {
//        System.out.println("Users list: " + client.getUser().getRooms());
        if (this.activeRoom != null && this.activeRoom.getRoomName().equals(activeRoom.getRoomName())){
            usersList.setItems(activeRoom.getUsers());

            usersList.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null || item.getUsername() == null) {
                        setText(null);
                    } else {
                        setText(item.getUsername());
                    }
                }
            });
        }
        else if(this.roomsList.getItems().size() == 0){
            usersList.getItems().clear();
        }
    }

    public void onDeleteUserButtonClick() {
        if (client.getUser().isConnected() && activeUser != null) {
            if (client.getSocket() != null) {
                System.out.println(activeUser.getUsername());
                client.getWriter().println("#5%" + activeRoom.getRoomName() + "%" + activeUser.getUsername() +"$");
            }
        }
        else{
            System.out.println("Can't delete user!");
        }
    }

    public void updateMessage(String message) {
//        System.out.println("Updating message" + message);
        if (this.allMessages == null) {
            this.allMessages.setText(message);
        }  else {
            this.allMessages.appendText(message);
        }
    }

    @FXML
    private void onSendButtonClick() {
        if (client.getUser().isConnected() && activeRoom != null) {
            String time = DateTimeFormatter.ofPattern("hh:mm:ss").format(ZonedDateTime.now());

            String text = messageInput.getText();
            text = text.replaceAll(";", "+;+");

            Message message = new Message(client.getUser().getUsername(), activeRoom.getRoomName(), time, text);
            client.getWriter().println("#4%" + message.getRoomName() + "%" + message.getTime() + ";" + message.getText() + "$");

//            System.out.println("Message to send: " + message.getText());
            this.messageInput.clear();
        }
        else {
            System.out.println("Can't send message!");
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Room getActiveRoom() {
        return activeRoom;
    }

    public void setActiveRoom(Room activeRoom) {
        this.activeRoom = activeRoom;
    }

    public ListView<Room> getRoomsList() {
        return roomsList;
    }

    public void setRoomsList(ListView<Room> roomsList) {
        this.roomsList = roomsList;
    }

    public TextArea getAllMessages() {
        return allMessages;
    }

    public void setAllMessages(TextArea allMessages) {
        this.allMessages = allMessages;
    }

    public ChoiceBox getChoiceRoom() {
        return choiceRoom;
    }

    public void setChoiceRoom(ChoiceBox choiceRoom) {
        this.choiceRoom = choiceRoom;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ListView<User> getUsersList() {
        return usersList;
    }

    public void setUsersList(ListView<User> usersList) {
        this.usersList = usersList;
    }
}
