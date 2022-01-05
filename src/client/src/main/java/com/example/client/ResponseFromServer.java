package com.example.client;

import com.example.client.structures.Message;
import com.example.client.structures.Room;
import com.example.client.structures.User;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ResponseFromServer implements Runnable {
    private Client client;
    private BufferedReader reader;
    private static boolean stopped = false;

    public ResponseFromServer(Client client) {
        this.client = client;
    }

    public static boolean isStopped() {
        return stopped;
    }

    public static void setStopped(boolean stopped) {
        ResponseFromServer.stopped = stopped;
    }

    @Override
    public void run() {
        try {
            this.reader = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
            while (!stopped) {

                String serverResponse = "";
                try{
                    serverResponse = this.reader.readLine();
                } catch (Exception e){
//                    System.out.println(e.toString());
                }

                if(serverResponse.contains("#")){
                    serverResponse = serverResponse.substring(serverResponse.indexOf("#"));
                }
                else{
                    final String response_copy = serverResponse;
                    if(response_copy.equals("At this moment there are to many users connected to the server. Please try again later!")){
                        Platform.runLater(() -> {
                            this.client.getUser().setConnected(false);
                            this.client.getUser().setUsername(null);
                            try {
                                System.out.println("Disconnected: " + this.client.getSocket());
                                this.client.getController().getLabel().setText(response_copy);
                                this.client.getController().getResponseFromServer().setStopped(true);
                                this.client.getSocket().close();
                            } catch (Exception e){
                                System.out.println(e.toString());
                            }
                        });
                    }
                    else if (!response_copy.equals("")) {
                        Platform.runLater(() -> {
                            this.client.getController().getLabel().setText(response_copy);
                        });
                    }
                    continue;
                }

                Platform.runLater(
                        () -> {
                            this.client.getUser().getRooms().clear();
                            this.client.getAllRooms().clear();
                            this.client.getController().getChoiceRoom().getItems().clear();
                            this.client.getController().getRoomsList().getItems().clear();
                        }
                );

                if (serverResponse.startsWith("#") && serverResponse.endsWith("$") && serverResponse.length() > 2) {
                    String[] rooms = serverResponse.substring(2, serverResponse.length() - 1).split("@");

                    for (int i = 0; i < rooms.length; i++) {
                        String[] roomAndUsers = rooms[i].split("%");
                        String roomName = roomAndUsers[0];
                        String[] users = roomAndUsers[1].split(";");

                        boolean userInRoom = false; // logged in user in this room
                        Room room = new Room(roomName);

                        for (int j = 0; j < users.length; j++) {
                            String user = users[j];
                            room.getUsers().add(new User(user));

                            if (!userInRoom && user.equals(this.client.getUser().getUsername())) {
                                userInRoom = true;
                            }
                        }

                        if (roomAndUsers.length > 2 && userInRoom) {
                            String[] messages = roomAndUsers[2].split(";");

                            for (int j = 0; j < messages.length; j++) {
                                if ((j + 1) %  3 == 0) {
                                    Message newMessage = new Message(messages[j - 2], room.getRoomName(), messages[j - 1], messages[j]);
                                    room.getMessages().add(newMessage);
                                }
                            }
                        }

                        if (client.getController().getActiveRoom() == null && userInRoom) {
                            client.getController().setActiveRoom(room);
                            client.getController().getRoomsList().getSelectionModel().selectFirst();
                        }
                        if (client.getController().getActiveRoom() != null &&
                                client.getController().getActiveRoom().getRoomName().equals(room.getRoomName())) {
                            if (client.getController().getAllMessages() != null) {
                                client.getController().getAllMessages().clear();
                            }
                            room.getMessages().forEach(message -> client.getController().updateMessage(message.textFormat()));
                        }

                        if (userInRoom) {
                            Platform.runLater(
                                    () -> {
                                        this.client.getUser().getRooms().add(room);
                                        this.client.getAllRooms().add(room);
                                        this.client.getController().displayRoomsList(false);
                                    }
                            );
                        } else {
                            Platform.runLater(
                                    () -> {
                                        this.client.getAllRooms().add(room);
                                        this.client.allRooms.forEach(x -> {
                                            if (!client.getController().getChoiceRoom().getItems().contains(x.getRoomName())) {
                                                client.getController().getChoiceRoom().getItems().add(x.getRoomName());
                                            }
                                        });
                                    }
                            );
                        }

                    }
                }
                else{
                    Platform.runLater(
                            () -> {
                                this.client.getController().displayRoomsList(true);
                                this.client.getController().getAllMessages().clear();
                            }
                    );
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
