package jp.akidukisystems.software.traindataclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONObject;

public class NetworkManager {

    public static Socket clientSocket;
    Socket client;
    BufferedReader reader = null;
    PrintWriter writer = null;
    Socket c2s = null;

    public void clientInit(int port) {
        while (true) {
            try {
                this.client = new Socket("localhost", port);
            } catch (UnknownHostException e) {
                // e.printStackTrace();
            } catch (IOException e) {
                // e.printStackTrace();
            }
            if(this.client != null) break;

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            this.writer = new PrintWriter(this.client.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String clientReciveString() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clientSendString(String data) {
        this.writer.println(data);
    }

    public void clientClose() {
        System.out.println("exit");
        try {
            if (this.writer != null) this.writer.close();
            if (this.reader != null) this.reader.close();
            if (this.c2s != null) this.c2s.close();
            if (this.client != null) this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String type, String doAny, Object value) {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("doAny", doAny);
        obj.put(doAny, value);
        clientSendString(obj.toString());
    }
    
}
