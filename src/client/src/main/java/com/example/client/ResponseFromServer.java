package com.example.client;

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
                String serverResponse = this.reader.readLine();

                if(serverResponse.contains("#")){
                    serverResponse = serverResponse.substring(serverResponse.indexOf("#"));
                    System.out.println(serverResponse);
                }
                else{
                    System.out.println(serverResponse);
                }
            }
        } catch (Exception e) {
        }

    }
}
