package jp.akidukisystems.software.MasconBridge;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import jp.akidukisystems.software.utilty.MasConReader;
import jp.akidukisystems.software.utilty.NetworkManager;

public class BridgeCore {

    private static final int GAME_PORT = 34565;
    private static final int PI_PORT = 34575;
    private static NetworkManager gameNetworkManager = null; 
    private static NetworkManager piNetworkManager = null;   
    private static MasConReader reader;

    private static final int RESET_NOTCH = -32768;

    private static final AtomicInteger lastUserNotch    = new AtomicInteger(RESET_NOTCH);
    private static final AtomicInteger lastPiNotch      = new AtomicInteger(RESET_NOTCH);

    public static void main(String[] args)
    {
        BridgeCore bridge = new BridgeCore();
        reader = new MasConReader();
        
        // Minecraft <- Bridge ( <- Pi )
        gameNetworkManager = new NetworkManager();
        gameNetworkManager.clientInit("localhost", GAME_PORT, 60000, 32768);

        // Pi -> Bridge ( -> Minecraft )
        piNetworkManager = new NetworkManager();
        try {
            piNetworkManager.serverInit(PI_PORT);
            piNetworkManager.serverWaitingClient();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 終了時クリーンアップ
        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            if (gameNetworkManager != null)
            {
                gameNetworkManager.sendString("{\"type\":\"kill\"}");
            }
            if (piNetworkManager != null)
            {
                piNetworkManager.sendString("{\"type\":\"kill\"}");
            }
        }));

        if (gameNetworkManager != null && piNetworkManager != null)
            bridge.running(); 
    }

    private void reconnectGame()
    {
        try { gameNetworkManager.clientClose(); } catch (Exception ignored) {}
        // 短い待ち → 再接続ループ
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        System.out.println("[info] reconnecting to GAME...");
        while (!gameNetworkManager.clientInit("localhost", GAME_PORT, 5000, 12)) {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            System.out.println("[info] retry...");
        }
        System.out.println("[info] reconnected to GAME");
    }
    
    private void sendNotch()
    {
        int piNotch     = lastPiNotch.get();
        int userNotch   = lastUserNotch.get();

        System.out.println("user:"+ userNotch +" pi:"+ piNotch);

        if((piNotch == RESET_NOTCH) && (userNotch != RESET_NOTCH))
        {
            gameNetworkManager.sendCommand("send", "notch", userNotch);
            lastUserNotch.set(RESET_NOTCH);
            return;
        }

        if((piNotch != RESET_NOTCH) && (userNotch == RESET_NOTCH))
        {
            gameNetworkManager.sendCommand("send", "notch", piNotch);
            return;
        }

        if((piNotch != RESET_NOTCH) && (userNotch != RESET_NOTCH))
        {
            if(userNotch > piNotch)
            {
                gameNetworkManager.sendCommand("send", "notch", piNotch);
            }
            else
            {
                gameNetworkManager.sendCommand("send", "notch", userNotch);
            }

            lastUserNotch.set(RESET_NOTCH);
            return;
        }
    }

    private void resetPiNotch()
    {
        lastPiNotch.set(RESET_NOTCH);
    }

    public void running()
    {
        if(!reader.isRunning()) reader.start();

        piNetworkManager.serverStartRead();
        piNetworkManager.sendString("{\"type\":\"start\",\"version\":null}");

        piNetworkManager.startHeartbeat(
            3000,  // intervalMs
            7000,  // timeoutMs
            () -> {
                System.err.println("[fatal] Peer dead! exiting...");
                try { piNetworkManager.clientClose(); } catch (Exception ignored) {}
                try { piNetworkManager.serverClose(); } catch (Exception ignored) {}
                System.exit(0);
            }
        );

        // Game2Pi
        new Thread(() -> {
            while (true)
            {
                try {
                    String data = gameNetworkManager.clientReceiveString();
                    if (data == null)
                    {
                        // 5回くらい再接続するのでここで対応する
                        reconnectGame();
                        continue;
                    }

                    piNetworkManager.sendString(data);
                    System.out.println("[G2P] "+ data);

                    JSONObject json = new JSONObject(data);
                    if ("kill".equals(json.optString("type")))
                    {
                        gameNetworkManager.clientClose();
                        System.exit(0);
                    }

                } catch (IOException e) {
                    reconnectGame();
                }
            }
        }, "Game2Pi").start();


        // Pi2Game
        new Thread(() ->
        {
            while (true)
            {
                String data = piNetworkManager.getLatestReceivedString();
                if (data != null)
                {
                    JSONObject json = new JSONObject(data);
                    System.out.println("[P2G] "+ data);

                    switch (json.optString("type"))
                    {
                        case "kill":
                            gameNetworkManager.sendString(data);
                            piNetworkManager.serverClose();
                            System.exit(0);
                            break;

                        case "send":
                            switch (json.optString("message"))
                            {
                                case "notch":
                                    int piNotch = json.optInt("notch");

                                    if(piNotch == RESET_NOTCH)
                                    {
                                        resetPiNotch();
                                    }
                                    else
                                    {
                                        lastPiNotch.set(json.optInt("notch"));
                                        sendNotch();
                                    }

                                    break;
                            
                                default:
                                    gameNetworkManager.sendString(data);
                                    break;
                            }
                            break;
                    
                        default:
                            gameNetworkManager.sendString(data);
                            break;
                    }

                    
                }
                try { Thread.sleep(5); } catch (InterruptedException ignored) {}
            }
        }, "Pi2Game").start();

        // マスコン
        new Thread(() ->
        {
            while (true)
            {
                if (reader.isRunning() && reader.isMasConConnected())
                {
                    int notch = MasConReader.mapYtoNotch(reader.getValue("y"));
                    gameNetworkManager.sendCommand("send", "notch", notch);
                    lastUserNotch.set(notch);
                    sendNotch();
                }
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }, "MasconLoop").start();

        // コンソールからの入力
        new Thread(() -> {
            try (Scanner sc = new Scanner(System.in)) {
                while (true)
                {
                    String line = sc.nextLine();
                    if (line.equalsIgnoreCase("exit")) break;
                    lastUserNotch.set(Integer.parseInt(line));
                    sendNotch();

                    System.out.println("[KEYBOARD] Sended notch:"+ Integer.parseInt(line));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "ConsoleInput").start();
    }
}