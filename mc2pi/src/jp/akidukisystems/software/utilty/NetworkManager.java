package jp.akidukisystems.software.utilty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;

public class NetworkManager 
{
    public static Socket clientSocket;
    Socket client;
    public BufferedReader reader = null;
    PrintWriter writer = null;
    Socket c2s = null;

    ServerSocket server;
    Socket s2c = null;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicReference<String> sharedData = new AtomicReference<>(null);

    public void clientInit(String adrs, int port) 
    {
        while (true) 
        {
            try 
            {
                this.client = new Socket(adrs, port);
                this.client.setTcpNoDelay(true);
            } 
            catch (UnknownHostException e) 
            {
                // e.printStackTrace();
            } 
            catch (IOException e) 
            {
                // e.printStackTrace();
            }
            if(this.client != null) break;

            try 
            {
                Thread.sleep(10);
            } 
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
        try 
        {
            this.writer = new PrintWriter(this.client.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public String clientReciveString() 
    {
        try 
        {
            if(this.reader == null)
            {
                return null;
            }

            return this.reader.readLine();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return null;
        }
    }

    public void clientClose() 
    {
        System.out.println("exit");
        try 
        {
            if (this.writer != null) this.writer.close();
            if (this.reader != null) this.reader.close();
            if (this.c2s != null) this.c2s.close();
            if (this.client != null) this.client.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }



    public void serverInit(int port) {
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverWaitingClient() {
        try {
            server.setSoTimeout(60000);
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
        if(this.reader == null)
        {
            return null;
        }
        
        String data = sharedData.get();
        sharedData.set(null);
        return data;
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



    public void sendString(String data) 
    {
        this.writer.println(data);
    }

    public void sendCommand(String type, String message, Object value) 
    {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("message", message);
        obj.put(message, value);
        sendString(obj.toString());
    }
    
}
