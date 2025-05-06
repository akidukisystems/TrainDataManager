package jp.akidukisystems.traindatamanager;

import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.*;

public class NetworkManager {
    ServerSocket server;
    BufferedReader reader = null;
    PrintWriter writer = null;
    Socket s2c = null;

    private static final Queue<String> receivedDataQueue = new ConcurrentLinkedQueue<>();

    public void serverInit(int port) {
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverWaitingClient() {
        try {
            s2c = server.accept();
            System.out.println("Connected s2c");
            reader = new BufferedReader(new InputStreamReader(s2c.getInputStream()));
            writer = new PrintWriter(s2c.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String serverReciveString() throws IOException {
        new Thread(() -> {
            while (true) {
                try {
                    String data = reader.readLine();
                    if (data != null) {
                        receivedDataQueue.add(data);
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();

        return receivedDataQueue.poll();
    }

    public void serverSendString(String data) throws IOException {
        try {
            writer.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverClose() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (s2c != null) s2c.close();
            if (server != null) server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}