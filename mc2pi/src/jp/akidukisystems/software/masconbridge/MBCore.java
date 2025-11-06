package jp.akidukisystems.software.masconbridge;

import jp.akidukisystems.software.masconbridge.Controller.MasConReader;
import jp.akidukisystems.software.utilty.NetworkManager;

public class MBCore {

    private static final int GAME_PORT = 34565;
    private static final int PI_PORT = 34575;
    private static NetworkManager gameNetworkManager = null; 
    private static NetworkManager piNetworkManager = null;   
    private static MasConReader reader;

    public static void main(String[] args)
    {
        MBCore clientObject = new MBCore();
        reader = new MasConReader();
        
        gameNetworkManager = new NetworkManager();
        gameNetworkManager.clientInit(GAME_PORT);  
        piNetworkManager = new NetworkManager();
        piNetworkManager.clientInit(PI_PORT);      

        clientObject.running(); 
    }

    public void running()
    {
        if(!reader.isRunning()) reader.start();

        new Thread(() ->
        {
            while(true)
            {
                // データのブリッジ処理
                String fetchGameData = gameNetworkManager.clientReciveString();
                String fetchPiData = piNetworkManager.clientReciveString();

                if(fetchGameData != null)
                {
                    piNetworkManager.clientSendString(fetchGameData);;
                }

                if(fetchPiData != null)
                {
                    gameNetworkManager.clientSendString(fetchPiData);;
                }

                if(reader.isRunning()) gameNetworkManager.sendCommand("send", "notch", MasConReader.mapYtoNotch(reader.getValue("y")));
            }
        });
    }
}
