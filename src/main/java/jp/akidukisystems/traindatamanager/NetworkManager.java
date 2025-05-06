package jp.akidukisystems.traindatamanager;

import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkManager {
    ServerSocket server;
    BufferedReader reader = null;
    PrintWriter writer = null;
    Socket s2c = null;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicReference<String> sharedData = new AtomicReference<>(null);

    public void serverInit(int port) {
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverWaitingClient() {
        try {
            server.setSoTimeout(1000);
            s2c = server.accept();
            System.out.println("Connected s2c");
            reader = new BufferedReader(new InputStreamReader(s2c.getInputStream()));
            writer = new PrintWriter(s2c.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverStartRead() {
        executor.submit(() -> {
            while (true) {
                try {
                    String data = reader.readLine();
                    if (data != null) {
                        sharedData.set(data);
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    public String getLatestReceivedString()  {
        String data = sharedData.get();
        sharedData.set(null);
        return data;
    }

    public void serverSendString(String data) {
        writer.println(data);
    }

    public void serverClose() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (s2c != null) s2c.close();
            if (server != null) server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sharedData.set(null);
        executor.shutdown();
    }
}