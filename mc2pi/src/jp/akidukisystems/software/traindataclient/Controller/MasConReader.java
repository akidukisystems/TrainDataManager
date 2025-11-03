package jp.akidukisystems.software.traindataclient.Controller;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;


public class MasConReader implements Runnable {

    private Controller target;
    private Component[] comps;
    private Map<String, Float> values = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private Thread thread;

    public MasConReader() {
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            String n = c.getName().toLowerCase();
            if (n.contains("zuiki") || n.contains("mascon") ||
                c.getType() == Controller.Type.STICK ||
                c.getType() == Controller.Type.GAMEPAD) {
                target = c;
                break;
            }
        }

        if (target == null) {
            System.err.println("MasCon not found");
        } else {
            System.out.println(" MasCon detected: " + target.getName());
            comps = target.getComponents();
            for (Component cp : comps) {
                values.put(cp.getIdentifier().getName(), 0f);
            }
        }
    }

    public void start() {
        if (target == null) return;
        if (running) return;

        running = true;
        thread = new Thread(this, "MasConReaderThread");
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public float getValue(String id) {
        return values.getOrDefault(id, 0f);
    }

    public float getValue(int index) {
        if (comps == null || index < 0 || index >= comps.length) return 0f;
        String id = comps[index].getIdentifier().getName();
        return getValue(id);
    }

    private static final float[] Y_MARKS = new float[] {
        -1.000000f,   // EB (-8)
        -0.960937f,   // B8 (-7)
        -0.851560f,   // B7 (-7)
        -0.749996f,   // B6 (-6)
        -0.640620f,   // B5 (-5)
        -0.531243f,   // B4 (-4)
        -0.429709f,   // B3 (-3)
        -0.320333f,   // B2 (-2)
        -0.210956f,   // B1 (-1)
        -0.000015f,   // N  ( 0)
        0.236117f,   // P1 ( 1)
        0.428580f,   // P2 ( 2)
        0.611109f,   // P3 ( 3)
        0.801602f,   // P4 ( 4)
        1.000000f    // P5 ( 5)
    };

    private static final int[] NOTCHES = new int[] {
        -8,  // EB
        -7,  // B8
        -7,  // B7
        -6,  // B6
        -5,  // B5
        -4,  // B4
        -3,  // B3
        -2,  // B2
        -1,  // B1
        0,  // N
        1,  // P1
        2,  // P2
        3,  // P3
        4,  // P4
        5   // P5
    };

    public static int mapYtoNotch(float y) {
        int idx = 0;
        float best = Float.MAX_VALUE;
        for (int i = 0; i < Y_MARKS.length; i++) {
            float d = Math.abs(y - Y_MARKS[i]);
            if (d < best) {
                best = d;
                idx = i;
            }
        }
        int notch = NOTCHES[idx];
        return notch;
    }

    @Override
    public void run() {
        System.out.println("MasCon polling started");
        try {
            while (running) {
                target.poll();
                for (Component cp : comps) {
                    float v = cp.getPollData();
                    values.put(cp.getIdentifier().getName(), v);
                }
                
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("MasCon polling stopped");
    }
}
