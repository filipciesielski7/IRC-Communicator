package com.example.client.structures;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

public class Room {
    private String roomName;
    private ObservableList<User> users = FXCollections.observableArrayList();
    private ObservableList<Message> messages = FXCollections.observableArrayList();

    public Room(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public void setUsers(ObservableList<User> users) {
        this.users = users;
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ObservableList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Room other = (Room) obj;
        if (!Objects.equals(this.roomName, other.roomName)) {
            return false;
        }
        return true;
    }
}
