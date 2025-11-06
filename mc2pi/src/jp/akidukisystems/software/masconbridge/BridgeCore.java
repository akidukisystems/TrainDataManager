package jp.akidukisystems.software.MasconBridge;

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
        gameNetworkManager.clientInit(GAME_PORT);  

        piNetworkManager = new NetworkManager();
        piNetworkManager.serverInit(PI_PORT);  
        piNetworkManager.serverWaitingClient();    

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            if (piNetworkManager != null) {
                piNetworkManager.sendString("{\"type\":\"kill\"}");
                piNetworkManager.serverClose();
            }
        }));

        clientObject.running(); 
    }

    public void running()
    {
        new Thread(() ->
        {
            if(!reader.isRunning()) reader.start();

            boolean isFirst = true;
            
            while(true)
            {

                if (isFirst) {
                    piNetworkManager.serverStartRead();
                    piNetworkManager.sendString("{\"type\":\"start\",\"version\":null}");
                    isFirst = false;
                }

                // データのブリッジ処理
                String fetchGameData = gameNetworkManager.clientReciveString();
                
                String fetchPiData = null;
                fetchPiData = piNetworkManager.getLatestReceivedString();

                if(fetchGameData != null)
                {
                    piNetworkManager.sendString(fetchGameData);
                    System.out.println(fetchGameData);
                }

                if(fetchPiData != null)
                {
                    gameNetworkManager.sendString(fetchPiData);
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
