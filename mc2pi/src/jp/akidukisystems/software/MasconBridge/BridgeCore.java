package jp.akidukisystems.software.MasconBridge;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONObject;

import jp.akidukisystems.software.utilty.MasConReader;
import jp.akidukisystems.software.utilty.NetworkManager;

public class BridgeCore {

    private static final int GAME_PORT = 34565;
    private static final int PI_PORT = 34575;
    private static NetworkManager gameNetworkManager = null; 
    private static NetworkManager piNetworkManager = null;   
    private static MasConReader reader;

    public static void main(String[] args)
    {
        BridgeCore clientObject = new BridgeCore();
        reader = new MasConReader();
        
        gameNetworkManager = new NetworkManager();

        try {
            gameNetworkManager.clientInit("localhost", GAME_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  

        piNetworkManager = new NetworkManager();

        try {
            piNetworkManager.serverInit(PI_PORT);

            try {
                piNetworkManager.serverWaitingClient();
            } catch (IOException e) {
                e.printStackTrace();
            }  
        } catch (IOException e) {
            e.printStackTrace();
        }  

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            if (gameNetworkManager != null) {
                gameNetworkManager.sendString("{\"type\":\"kill\"}");
            }

            if (piNetworkManager != null) {
                piNetworkManager.sendString("{\"type\":\"kill\"}");
            }
        }));

        if((gameNetworkManager != null) && (piNetworkManager != null))
            clientObject.running(); 
    }

    public void running()
    {
        new Thread(() -> {
            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    String line = sc.nextLine();
                    if (line.equalsIgnoreCase("exit")) break;
                    gameNetworkManager.sendCommand("send", "notch", Integer.parseInt(line));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("exit");
        }).start();
        
        new Thread(() ->
        {
            if(!reader.isRunning()) reader.start();

            boolean isFirst = true;

            piNetworkManager.startHeartbeat(
                3000,  // intervalMs
                7000,  // timeoutMs
                () -> {
                    System.err.println("Peer dead. Exiting...");
                    try { piNetworkManager.clientClose(); } catch (Exception ignored) {}
                    try { piNetworkManager.serverClose(); } catch (Exception ignored) {}
                    System.exit(0);
                }
            );
            
            while(true)
            {

                if (isFirst) {
                    piNetworkManager.serverStartRead();
                    piNetworkManager.sendString("{\"type\":\"start\",\"version\":null}");
                    isFirst = false;
                }

                // データのブリッジ処理
                String fetchGameData = null;

                try {
                    fetchGameData = gameNetworkManager.clientReciveString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                String fetchPiData = null;
                fetchPiData = piNetworkManager.getLatestReceivedString();

                if(fetchGameData != null)
                {
                    piNetworkManager.sendString(fetchGameData);

                    JSONObject jsonObj = new JSONObject(fetchGameData);
                    switch (jsonObj.getString("type"))
                    {
                        case "kill":

                            try {
                                gameNetworkManager.clientClose();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            System.exit(0);
                            break;
                    }
                    
                    System.out.println(fetchGameData);
                }
                else
                {
                    gameNetworkManager = new NetworkManager();

                    try {
                        gameNetworkManager.clientInit("localhost", GAME_PORT);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(fetchPiData != null)
                {
                    gameNetworkManager.sendString(fetchPiData);

                    JSONObject jsonObj = new JSONObject(fetchPiData);
                    switch (jsonObj.getString("type"))
                    {
                        case "kill":
                            try {
                                piNetworkManager.serverClose();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            System.exit(0);
                            break;
                    }

                    System.out.println(fetchPiData);
                }

                if((fetchGameData == null ) && (fetchPiData == null))
                {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(reader.isRunning()) gameNetworkManager.sendCommand("send", "notch", MasConReader.mapYtoNotch(reader.getValue("y")));
            }
        }).start();
    }
}
