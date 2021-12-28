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

    private Socket socket;

    private PrintWriter writer;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("IRC-Communicator");
        stage.setScene(scene);
        stage.show();
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static ObservableList<Room> getAllRooms() {
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

    public static void main(String[] args) {
        launch();
    }
}
