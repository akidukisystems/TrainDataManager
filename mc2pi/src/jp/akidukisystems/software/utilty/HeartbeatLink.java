package jp.akidukisystems.software.utilty;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;



public final class HeartbeatLink implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;
    private final ScheduledExecutorService ses;
    private final long intervalMs;
    private final long timeoutMs;
    private final Runnable onPeerDead;
    private final AtomicLong lastPong = new AtomicLong(System.currentTimeMillis());
    private volatile boolean running = false;

    private HeartbeatLink(Socket socket, PrintWriter out,
                          long intervalMs, long timeoutMs, Runnable onPeerDead) {
        this.socket = Objects.requireNonNull(socket);
        this.out = Objects.requireNonNull(out);
        this.intervalMs = intervalMs;
        this.timeoutMs = timeoutMs;
        this.onPeerDead = Objects.requireNonNull(onPeerDead);
        this.ses = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "hb-link");
            t.setDaemon(true);
            return t;
        });
    }

    public static HeartbeatLink attach(Socket socket, PrintWriter out,
                                       long intervalMs, long timeoutMs, Runnable onPeerDead) throws IOException {

        try { socket.setKeepAlive(true); } catch (Exception ignored) {}
        HeartbeatLink hb = new HeartbeatLink(socket, out, intervalMs, timeoutMs, onPeerDead);
        hb.start();
        return hb;
    }

    private void start() {
        running = true;

        ses.scheduleAtFixedRate(() -> {
            if (!running) return;
            try {
                JSONObject ping = new JSONObject().put("type", "hb").put("op", "ping");
                out.println(ping.toString());
            } catch (Exception ignored) {}
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        ses.scheduleAtFixedRate(() -> {
            if (!running) return;
            long elapsed = System.currentTimeMillis() - lastPong.get();
            if (elapsed > timeoutMs) {
                try { onPeerDead.run(); } finally { closeQuiet(); }
            }
        }, timeoutMs / 3, timeoutMs / 3, TimeUnit.MILLISECONDS);
    }

    public boolean consume(String line) {
        if (line == null) return false;
        try {
            JSONObject obj = new JSONObject(line);
            if (!"hb".equals(obj.optString("type"))) return false;
            String op = obj.optString("op", "");
            if ("ping".equals(op)) {
                JSONObject pong = new JSONObject().put("type", "hb").put("op", "pong");
                out.println(pong.toString());
                return true;
            } else if ("pong".equals(op)) {
                lastPong.set(System.currentTimeMillis());
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private void closeQuiet() {
        running = false;
        ses.shutdownNow();
        try { socket.close(); } catch (Exception ignored) {}
    }

    @Override public void close() { closeQuiet(); }
}
