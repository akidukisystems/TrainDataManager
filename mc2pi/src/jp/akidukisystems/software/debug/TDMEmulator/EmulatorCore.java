package jp.akidukisystems.software.debug.TDMEmulator;
import org.json.JSONObject;

import jp.akidukisystems.software.utilty.NetworkManager;

public class EmulatorCore {
    
    private static NetworkManager networkManager = null; 
    private static final int PORT = 34565;
    public static void main(String[] args) {
        EmulatorCore object = new EmulatorCore();
        networkManager = new NetworkManager();
        networkManager.serverInit(PORT);        
        networkManager.serverWaitingClient();
        object.running();
    }

    public void running()
    {
        new Thread(() ->
        {
            boolean isFirst = true;
            int notch = -8;
            int bc = 0;
            
            while(true)
            {
                if (isFirst) {
                    networkManager.serverStartRead();
                    networkManager.sendString("{\"type\":\"start\",\"version\":null}");
                    isFirst = false;
                }

                String fetchData = null;
                fetchData = networkManager.getLatestReceivedString();

                if(fetchData != null)
                {
                    System.out.println(fetchData);
                    JSONObject jsonObj = new JSONObject(fetchData);

                    switch (jsonObj.getString("type"))
                    {
                        case "kill":
                            networkManager.clientClose();
                            System.exit(0);
                            break;

                        default:
                            notch = jsonObj.getInt("notch");

                            if(notch < 0)
                            {
                                bc = notch * -59;
                            }
                            else
                            {
                                bc = 0;
                            }
                            break;
                    }
                }
                else
                {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                networkManager.sendString("{\"type\":\"send\",\"message\":\"none\",\"id\":0,\"id2\":0,\"speed\":\"0.0\",\"notch\":"+ notch +",\"bc\":"+ bc +",\"mr\":880,\"door\":0,\"reverser\":0,\"destination\":0,\"speedLimit\":95,\"isTASCEnable\":true,\"isTASCBraking\":false,\"isTASCStopPos\":false,\"move\":0,\"moveTo\":0,\"formation\":2,\"isOnRail\":true,\"isComplessorActive\":false}");
            }
        }).start();
    }
}
