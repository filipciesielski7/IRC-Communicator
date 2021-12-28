package com.example.client.structures;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class User {
    private String username;
    BooleanProperty connected;
    private ObservableList<Room> rooms = FXCollections.observableArrayList();

    public User(String username, BooleanProperty connected) {
        this.username = username;
        this.connected = connected;
    }

    public User(String username) {
        this.username = username;
    }

    public User(boolean connected) {
        this.connected = new SimpleBooleanProperty(connected);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public ObservableList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ObservableList<Room> rooms) {
        this.rooms = rooms;
    }
}
