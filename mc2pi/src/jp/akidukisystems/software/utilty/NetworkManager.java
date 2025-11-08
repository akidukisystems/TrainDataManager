package jp.akidukisystems.software.utilty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;

public class NetworkManager 
{
    public static Socket clientSocket;
    Socket client;
    public BufferedReader reader = null;
    PrintWriter writer = null;

    ServerSocket server;
    Socket s2c = null;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicReference<String> sharedData = new AtomicReference<>(null);
    private final BlockingQueue<String> receiveQueue = new LinkedBlockingQueue<>();

    private volatile HeartbeatLink hb;

    public PrintWriter getWriter() { return writer; }

    public boolean clientInit(String adrs, int port, int connectTimeoutMs, int maxRetry)
    {
        for (int i = 0; i < maxRetry; i++)
        {
            Socket sock = new Socket();
            try {
                sock.connect(new java.net.InetSocketAddress(adrs, port), connectTimeoutMs);

                this.client = sock;
                this.writer = new PrintWriter(this.client.getOutputStream(), true);
                this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));

                sock.setKeepAlive(true);
                sock.setSoTimeout(0);
                return true; // 接続できた
            } catch (IOException e) {
                try { sock.close(); } catch (IOException ignore) {}
                // 少し待ってリトライ
                try { Thread.sleep(200); } catch (InterruptedException ignore) {}
            }
        }
        // 失敗時は全クリア
        this.client = null;
        this.writer = null;
        this.reader = null;
        return false;
    }

    public boolean isAliveClient()
    {
        Socket s = this.client;
        return s != null
            && s.isConnected()
            && !s.isClosed()
            && !s.isInputShutdown()
            && !s.isOutputShutdown();
    }

    public boolean isAliveServer()
    {
        Socket s = this.s2c;
        return s != null
            && s.isConnected()
            && !s.isClosed()
            && !s.isInputShutdown()
            && !s.isOutputShutdown();
    }

    public Socket getActiveSocket()
    {
        if (isAliveClient()) return client;
        if (isAliveServer()) return s2c;
        return null;
    }

    public String clientReceiveString() throws IOException
    {
        String line;
        while ((line = this.reader.readLine()) != null)
        {
            HeartbeatLink local = hb;
            if (local != null && local.consume(line))
            {
                continue; // HB は飲み込む
            }
            return line;
        }
        return null;
    }

    public void clientClose()
    {
        System.out.println("exit");
        try { if (this.writer != null) this.writer.close(); } catch (Exception ignore) {}
        try { if (this.reader != null) this.reader.close(); } catch (Exception ignore) {}
        try { if (this.client != null) this.client.close(); } catch (Exception ignore) {}

        this.writer = null;
        this.reader = null;
        this.client= null;
    }

    public void serverInit(int port) throws IOException
    {
        server = new ServerSocket(port);
    }

    public void serverWaitingClient() throws IOException
    {
        server.setSoTimeout(60000);
        s2c = server.accept();
        System.out.println("Connected s2c");

        s2c.setKeepAlive(true);
        s2c.setSoTimeout(0);

        reader = new BufferedReader(new InputStreamReader(s2c.getInputStream()));
        writer = new PrintWriter(s2c.getOutputStream(), true);
    }

    public void serverStartRead()
    {
        executor.submit(() ->
        {
            while (true)
            {
                String data = null;
                try {
                    data = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (data == null) break;
                HeartbeatLink local = hb;
                if (local != null && local.consume(data))
                    continue;

                receiveQueue.offer(data);
            }
        });
    }

   public String getLatestReceivedString()
    {
        return receiveQueue.poll(); 
    }

    public void serverClose()
    {
        try { if (writer != null) writer.close(); } catch (Exception ignore) {}
        try { if (reader != null) reader.close(); } catch (Exception ignore) {}
        try { if (s2c != null) s2c.close(); }     catch (Exception ignore) {}
        try { if (server != null) server.close(); } catch (Exception ignore) {}
        writer = null;
        reader = null;
        s2c    = null;
        server = null;
        sharedData.set(null);
        executor.shutdown();
    }

    public synchronized boolean sendString(String data)
    {
        if (!isAliveClient() && !isAliveServer()) return false;
        if (this.writer == null) return false;
        this.writer.println(data);
        this.writer.flush();
        return !this.writer.checkError();
    }


    public void sendCommand(String type, String message, Object value) 
    {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("message", message);
        obj.put(message, value);
        sendString(obj.toString());
    }

    public void startHeartbeat(long intervalMs, long timeoutMs, Runnable onPeerDead)
    {
        try {
            if (hb != null) return; // 既に開始済みならスキップ
            Socket sock = getActiveSocket();
            if (sock == null || writer == null) return;
            hb = HeartbeatLink.attach(sock, writer, intervalMs, timeoutMs, () ->
            {
                try { clientClose(); } catch (Exception ignore) {}
                try { serverClose(); } catch (Exception ignore) {}
                if (onPeerDead != null) onPeerDead.run();
                hb = null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
