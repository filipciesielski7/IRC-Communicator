package com.example.client;

import com.example.client.structures.Room;
import com.example.client.structures.User;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Application {

    private User user = new User(false);
    public static ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private Controller controller;
    private Socket socket;
    private PrintWriter writer;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("client.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 907, 522);
        stage.setTitle("IRC-Communicator");
        stage.setScene(scene);
        stage.show();
        controller = fxmlLoader.getController();
        controller.setClient(this);
        controller.setStage(stage);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ObservableList<Room> getAllRooms() {
        return allRooms;
    }

    public static void setAllRooms(ObservableList<Room> allRooms) {
        Client.allRooms = allRooms;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
