package com.example.client;

import com.example.client.structures.Room;
import com.example.client.structures.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Client extends Application {

    private User user = new User(false);
    public static ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private static Controller controller;
    private Socket socket;
    private PrintWriter writer;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Thread.setDefaultUncaughtExceptionHandler(Client::showError);
            FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("client.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 907, 522);
            stage.setTitle("IRC-Communicator");
            stage.setScene(scene);
            stage.show();
            controller = fxmlLoader.getController();
            controller.setClient(this);
            controller.setStage(stage);
        } catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    private static void showError(Thread t, Throwable e) {

        System.err.println("***Default exception handler***");
        // controller.getLabel().setText("Wystapil błąd. Aplikacja może zostać wyłączona. Spróbuj ponownie póżniej.");
        try {
            if (Platform.isFxApplicationThread()) {
                Platform.exit();

                controller.getResponseFromServer().setStopped(true);
                controller.getClient().getSocket().close();

                controller.getThread().interrupt();
                controller.getClient().getUser().setUsername(null);
                controller.getClient().getUser().setConnected(false);
                showErrorDialog(e);

            } else {
                System.err.println("An unexpected error occurred in "+t);

            }
        }catch(IOException ex){
            e.printStackTrace();
        }

    }

    private static void showErrorDialog(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Content Here");
        alert.setTitle("Error. Closing the app and deleting user.");
//        alert.setHeaderText("Unexpected error happened");
//        alert.setContentText("Try again later");
        alert.showAndWait();
        try {
            Thread.sleep(3000);
            System.exit(0);
        } catch (InterruptedException efd) {

        }
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
