package com.example.client.structures;

public class Message {

    private String username;
    private String roomName;
    private String time;
    private String text;

    public Message(String username, String roomName, String time, String text) {
        this.username = username;
        this.roomName = roomName;
        this.time = time;
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }

    public String textFormat() {
        String format = this.time + padLeft(this.username, 20) + ">" + this.text + "\n";
        return format;
    }
}