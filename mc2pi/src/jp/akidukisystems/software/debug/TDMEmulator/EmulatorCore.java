package jp.akidukisystems.software.debug.TDMEmulator;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import jp.akidukisystems.software.utilty.NetworkManager;

public class EmulatorCore {
    
    private static NetworkManager networkManager = null; 
    private static final int PORT = 34565;
    int bc = 0;
    int targetBc = 0;
    int mr = 880;
    int notch = -8;
    float speed = 0f;
    boolean isComplessorActive = false;
    int door = 0;
    int reverser = 1;

    public static void main(String[] args) {
        EmulatorCore object = new EmulatorCore();
        networkManager = new NetworkManager();

        try {
            networkManager.serverInit(PORT);

            try {
                networkManager.serverWaitingClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }       

        if(networkManager != null)
            object.running();
    }

    public void running()
    {
        new Thread(() ->
        {
            boolean isFirst = true;

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int beforeBC = bc;

                    if(bc > targetBc)
                        bc -= 17;

                    if(bc < targetBc)
                        bc += 17;

                    if(beforeBC < bc)
                        mr -= 1;

                    if(mr < 800)
                        isComplessorActive = true;

                    if(isComplessorActive)
                        mr += 1;

                    if(isComplessorActive && (mr > 880))
                        isComplessorActive = false;



                    if(notch > 0)
                    {
                        speed += 0.07f * notch;
                    }

                    if(bc > 0)
                    {
                        speed -= 0.0012f * bc;
                        if(speed < 0f)
                            speed = 0f;
                    }
                }
            }, 0, 80);
            
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
                            try {
                                networkManager.clientClose();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            System.exit(0);
                            break;

                        default:

                            switch (jsonObj.getString("message")) {
                                case "notch":
                                    notch = jsonObj.getInt("notch");

                                    if(notch < 0)
                                    {
                                        targetBc = notch * -59;
                                    }
                                    else
                                    {
                                        targetBc = 0;
                                    }

                                    break;

                                case "door":
                                    door = jsonObj.getInt("door");
                                    break;

                                case "reverser":
                                    reverser = jsonObj.getInt("reverser");
                                    break;

                                default:
                                    break;
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

                networkManager.sendString("{\"type\":\"send\",\"message\":\"none\",\"id\":0,\"id2\":0,\"speed\":\""+ speed +"\",\"notch\":"+ notch +",\"bc\":"+ bc +",\"mr\":"+ mr +",\"door\":"+ door +",\"reverser\":"+ reverser +",\"destination\":0,\"speedLimit\":95,\"isTASCEnable\":true,\"isTASCBraking\":false,\"isTASCStopPos\":false,\"move\":0,\"moveTo\":0,\"formation\":2,\"isOnRail\":true,\"isComplessorActive\":"+ isComplessorActive +"}");
            }
        }).start();
    }
}
