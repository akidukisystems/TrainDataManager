package jp.akidukisystems.software.utilty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;
import com.fazecast.jSerialComm.SerialPort;

public class Serial {

    private SerialPort port;

    private BufferedReader in;
    private BufferedWriter out;

    private final ConcurrentLinkedQueue<String> rxQueue = new ConcurrentLinkedQueue<>();
    private final BlockingQueue<String> txQueue = new LinkedBlockingQueue<>();

    private volatile boolean running = false;

    public void initialize(String portName)
    {
        port = SerialPort.getCommPort(portName);
        port.setComPortParameters(9600, 8,
                SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (!port.openPort()) {
            throw new RuntimeException("Cannot open port: " + portName);
        }

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        in = new BufferedReader(new InputStreamReader(port.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(port.getOutputStream(), StandardCharsets.UTF_8));

        running = true;

        new Thread(() ->
        {
            try {
                String line;
                while (running && (line = in.readLine()) != null)
                {
                    rxQueue.offer(line);
                    System.out.println("RX RAW " + portName + ": " + line);
                }
            } catch (Exception e) {
                if (running) e.printStackTrace();
            } finally {
                running = false;
            }
        }, "SerialRX-" + portName).start();

        new Thread(() ->
        {
            try {
                while (running)
                {
                    String s = txQueue.take(); // running=false でも待つので工夫の余地あり
                    out.write(s);
                    out.write("\n");
                    out.flush();
                }
            } catch (Exception e) {
                if (running) e.printStackTrace();
            } finally {
                running = false;
            }
        }, "SerialTX-" + portName).start();
    }

    public String read()
    {
        return rxQueue.poll();
    }

    // 送信はキューに統一（UIスレッドでも安全）
    public void send(JSONObject obj)
    {
        txQueue.offer(obj.toString());
    }

    public void sendRawJson(String raw)
    {
        txQueue.offer(raw);
    }

    // 終了処理
    public void close()
    {
        running = false;
        try { if (port != null) port.closePort(); } catch (Exception ignored) {}
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
    }
}

