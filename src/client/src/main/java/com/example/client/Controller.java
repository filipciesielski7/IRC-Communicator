package com.example.client;

import com.example.client.structures.Message;
import com.example.client.structures.Room;
import com.example.client.structures.User;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.*;

public class Controller implements Initializable {

    private Client client = new Client();
    private Stage stage;
    private ResponseFromServer responseFromServer;
    private Room activeRoom;
    private User activeUser;
    private String chosenRoom;
    private Thread thread;

    public static final int maxLengthUsername = 10;
    public static final int maxLengthMessage = 256;
    public static final int maxLengthRoomName = 20;

    @FXML
    private ChoiceBox choiceRoom;

    @FXML
    private Button joinRoom;

    @FXML
    private TextField username;

    @FXML
    private TextField ipAddress;

    @FXML
    private TextField portNumber;

    @FXML
    private Button updateName;

    @FXML
    private Button logout;

    @FXML
    private Button exit;

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

    @FXML
    private Label label;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        if (allMessages != null) {
            allMessages.textProperty().addListener((observable, oldValue, newValue) -> {
                allMessages.setText(newValue);
                displayUsersList(activeRoom);
            });
        }

        messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    onSendButtonClick();
                }
            }
        });

        messageInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                try {
                    if(newValue.length() > maxLengthMessage)
                        messageInput.setText(oldValue);
                } catch (Exception e) {
                    messageInput.setText(oldValue);
                }
            }
        });

        newRoomName.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    onAddRoomButtonClick();
                }
            }
        });

        newRoomName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                try {
                    if(newValue.length() > maxLengthRoomName)
                        newRoomName.setText(oldValue);
                } catch (Exception e) {
                    newRoomName.setText(oldValue);
                }
            }
        });

        username.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    if(client.getUser().isConnected()){
                        onUpdateNameButtonClick();
                    } else{
                        onConnectButtonClick();
                    }
                }
            }
        });

        ipAddress.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                        onConnectButtonClick();
                }
            }
        });

        portNumber.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    onConnectButtonClick();
                }
            }
        });

        username.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                try {
                    if(newValue.length() > maxLengthUsername)
                        username.setText(oldValue);
                } catch (Exception e) {
                    username.setText(oldValue);
                }
            }
        });


        choiceRoom.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    onJoinRoomButtonClick();
                }
            }
        });

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
            final Room resultRoom = result;
            Thread thread = new Thread(() -> {
                client.getWriter().println("#2%" + resultRoom.getRoomName() + "$");
            });
            thread.start();
        }
        else{
            Platform.runLater(() -> {
                if(this.client.getUser().isConnected()){
                    this.client.getController().getLabel().setText("Choose room before joining!");
                }
                else{
                    this.client.getController().getLabel().setText("Can't join room, because you are not connected to the server!");
                }
            });
        }
    }

    @FXML
    protected void onConnectButtonClick() {
        Socket socket;
        if (client.getUser().getUsername() == null && !username.getText().isEmpty()) {
            try {
                if(ipAddress.getText().isEmpty() && portNumber.getText().isEmpty()){
                    socket = new Socket("localhost", 1234);
                }
                else if(ipAddress.getText().isEmpty() && !portNumber.getText().isEmpty()){
                    socket = new Socket("localhost", Integer.parseInt(portNumber.getText()));
                }
                else if(!ipAddress.getText().isEmpty() && portNumber.getText().isEmpty()){
                    socket = new Socket(ipAddress.getText(), 1234);
                }
                else{
                    socket = new Socket(ipAddress.getText(), Integer.parseInt(portNumber.getText()));
                }
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                client.setSocket(socket);
                client.setWriter(writer);

                System.out.println("Connected: " + socket);

                client.getUser().setUsername(username.getText());
                client.getUser().setConnected(true);

                if (client.getSocket() != null) {
                    this.responseFromServer = new ResponseFromServer(this.client);
                    responseFromServer.setStopped(false);
                    thread = new Thread(this.responseFromServer);
                    thread.start();

                    Thread thread = new Thread(() -> {
                        client.getWriter().println("#0%" + client.getUser().getUsername() +"$");

                    });
                    thread.start();
                }

            } catch (IOException ex) {
                Platform.runLater(() -> {
                    this.client.getController().getLabel().setText("Can't connect to server!");
                });
            }
        }
        else if (client.getUser().getUsername() == null && username.getText().isEmpty()){
                this.client.getController().getLabel().setText("Enter username before connecting to the server!");
        }
        else if (client.getUser().getUsername() != null){
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Already connected to the server!");
            });
        }
    }

    @FXML
    private void onLogoutButtonClick(Event event) {
        Button button = (Button) event.getSource();
        if (client.getUser().isConnected()) {
            try {
                System.out.println("Disconnected: " + client.getSocket());
                responseFromServer.setStopped(true);
                client.getSocket().close();

                getUsersList().getItems().clear();
                getRoomsList().getItems().clear();
                getAllMessages().clear();
                choiceRoom.getItems().clear();
                messageInput.clear();
                newRoomName.clear();
                username.clear();
                ipAddress.clear();
                portNumber.clear();
                if(button.getId().equals("exit")){
                     stage.close();
                }
                thread.interrupt();
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    this.client.getController().getLabel().setText("Cant' disconnect!");
                });
            } catch (Exception e){
            }

            Platform.runLater(() -> {
                getLabel().setText("Disconnected from the server!");
            });

            client.getUser().setUsername(null);
            client.getUser().setConnected(false);
        }
        else{
            if(button.getId().equals("exit")){
                stage.close();
            }
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Can't disconnect, because you are not connected to the server!");
            });
        }
    }

    @FXML
    private void onUpdateNameButtonClick() {
        if (client.getUser().isConnected() && !username.getText().isEmpty() && !username.getText().equals(client.getUser().getUsername())) {
            client.getUser().setUsername(username.getText());
            if (client.getSocket() != null) {
                Thread thread = new Thread(() -> {
                    client.getWriter().println("#0%" + client.getUser().getUsername() +"$");
                });
                thread.start();
            }
        }
        else if (username.getText().equals(client.getUser().getUsername())){
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("You entered the same username!");
            });
        }
        else{
            Platform.runLater(() -> {
                if(this.client.getUser().isConnected()){
                    this.client.getController().getLabel().setText("Can't change username to empty string!");
                    username.setText(this.client.getUser().getUsername());
                }
                else{
                    this.client.getController().getLabel().setText("Can't change username, because you are not connected to the server!");
                    username.setText(this.client.getUser().getUsername());
                }
            });
        }
    }

    @FXML
    private void onAddRoomButtonClick() {
        if (client.getUser().isConnected() && !newRoomName.getText().isEmpty()) {
            if (client.getSocket() != null) {
                final String roomName = newRoomName.getText();
                Thread thread = new Thread(() -> {
                    client.getWriter().println("#1%" + roomName +"$");
                });
                thread.start();
                newRoomName.setText("");
            }
        }
        else if (client.getUser().isConnected() && newRoomName.getText().isEmpty()){
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Enter new room name before adding!");
            });
        }
        else{
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Can't add room because you are not connected to the server!");
            });
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
//            if(!usersList.getItems().get(0).getUsername().contains(" ??????")){
//                usersList.getItems().get(0).setUsername(usersList.getItems().get(0).getUsername() + " ??????");
//            }

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
        if (client.getUser().isConnected() && activeUser != null && activeRoom != null) {
            if (client.getSocket() != null) {

                final String roomName = activeRoom.getRoomName();
                final String userName = activeUser.getUsername();
                Thread thread = new Thread(() -> {
                    client.getWriter().println("#5%" + roomName + "%" + userName +"$");
                });
                thread.start();
            }
        }
        else{
            Platform.runLater(() -> {
                if(!this.client.getUser().isConnected()){
                    this.client.getController().getLabel().setText("Can't delete user, because you are not connected to the server!");
                }
                else{
                    this.client.getController().getLabel().setText("Choose user and room before removing!");
                }
            });
        }
    }

    public void onLeaveRoomButtonClick() {
        if (client.getUser().isConnected() && activeRoom != null) {
            if (client.getSocket() != null) {

                final String roomName = activeRoom.getRoomName();
                Thread thread = new Thread(() -> {
                    client.getWriter().println("#3%" + roomName +"$");
                });
                thread.start();
            }
        }
        else{
            Platform.runLater(() -> {
                if(!this.client.getUser().isConnected()){
                    this.client.getController().getLabel().setText("Can't leave room, because you are not connected to the server!");
                }
                else{
                    this.client.getController().getLabel().setText("Choose room before leaving!");
                }
            });
        }
    }

    public void updateMessage(String message) {
//        System.out.println("Updating message: " + message);
        if (this.allMessages == null) {
            this.allMessages.setText(message);
        }  else {
            this.allMessages.appendText(message);
        }
    }

    @FXML
    private void onSendButtonClick() {
        if (client.getUser().isConnected() && activeRoom != null && this.messageInput.getText() != "") {
            String time = DateTimeFormatter.ofPattern("hh:mm:ss").format(ZonedDateTime.now());

            String text = messageInput.getText();
            text = text.replaceAll(";", "+;+");

            Message message = new Message(client.getUser().getUsername(), activeRoom.getRoomName(), time, text);

            final String roomName = message.getRoomName();
            final String messageTime = message.getTime();
            final String messageInfo = message.getText();
            Thread thread = new Thread(() -> {
                client.getWriter().println("#4%" + roomName + "%" + messageTime + ";" + messageInfo + "$");
            });
            thread.start();

//            System.out.println("Message to send: " + message.getText());
            this.messageInput.clear();
        }
        else if(!this.client.getUser().isConnected()){
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Can't send message, because you are not connected to the server!");
            });
        }
        else if(this.messageInput.getText() == ""){
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Can't send empty message!");
            });
        }
        else {
            Platform.runLater(() -> {
                this.client.getController().getLabel().setText("Choose room before sending message!");
            });
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

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public ResponseFromServer getResponseFromServer() {
        return responseFromServer;
    }

    public void setResponseFromServer(ResponseFromServer responseFromServer) {
        this.responseFromServer = responseFromServer;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
