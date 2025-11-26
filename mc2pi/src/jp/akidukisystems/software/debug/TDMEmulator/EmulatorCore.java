package jp.akidukisystems.software.debug.TDMEmulator;
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import jp.akidukisystems.software.utilty.NetworkManager;

public class EmulatorCore
{    
    private static NetworkManager networkManager = null; 
    private static final int PORT = 34565;
    volatile int bc = 0;
    volatile int targetBc = 0;
    volatile float mr = 880f;
    volatile int notch = -8;
    volatile float speed = 0f;
    volatile boolean isComplessorActive = false;
    volatile int door = 0;
    volatile int reverser = 1;
    volatile float move = 0f;
    volatile int moveTo = 0;
    volatile float totalMove = 0f;
    volatile int speedState = 0;

    public static void main(String[] args)
    {
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
            timer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    float beforeSpeed = speed;
                    int beforeBC = bc;

                    if(bc > targetBc)
                        bc -= 17;

                    if(bc < targetBc)
                        bc += 17;

                    if(beforeBC < bc)
                        mr -= 0.5f;

                    if(mr < 780f)
                        isComplessorActive = true;

                    if(isComplessorActive)
                        mr += 0.4f;

                    if(isComplessorActive && (mr > 880f))
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

                    if((speed > beforeSpeed) && (notch > 0))
                    {
                        speedState = 1;
                    }
                    else if((speed < beforeSpeed) && (notch < 0))
                    {
                        speedState = -1;
                    }
                    else
                    {
                        speedState = 0;
                    }

                    float distancePerCycle = (speed * 1000f / 3600f) * 0.08f;

                    if(moveTo == 0)
                        move -= distancePerCycle;
                    else
                        move += distancePerCycle;

                    totalMove += distancePerCycle;
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
                    JSONObject jsonObj = new JSONObject(fetchData);

                    switch (jsonObj.getString("type"))
                    {
                        case "kill":
                            networkManager.clientClose();
                            System.exit(0);
                            break;

                        default:
                            switch (jsonObj.getString("message"))
                            {
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

                                case "move":
                                    move = jsonObj.getFloat("move");
                                    break;

                                case "moveTo":
                                    moveTo = jsonObj.getInt("moveTo");
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
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                networkManager.sendString("{\"type\":\"send\",\"message\":\"none\",\"id\":0,\"id2\":0,\"speed\":\""+ speed +"\",\"notch\":"+ notch +",\"bc\":"+ bc +",\"mr\":"+ (int) mr +",\"door\":"+ door +",\"reverser\":"+ reverser +",\"destination\":0,\"speedLimit\":95,\"isTASCEnable\":true,\"isTASCBraking\":false,\"isTASCStopPos\":false,\"move\":"+ move +",\"moveTo\":"+ moveTo +",\"totalMove\":"+ totalMove +",\"formation\":4,\"isOnRail\":true,\"isComplessorActive\":"+ isComplessorActive +",\"speedState\":"+ speedState +"}");
            }
        }).start();

        new Thread(() -> {
            try (Scanner sc = new Scanner(System.in)) {
                while (true)
                {
                    String line = sc.nextLine();
                    if (line.equalsIgnoreCase("exit")) break;

                    networkManager.sendString(line);

                    System.out.println("[KEYBOARD] Sended :"+ line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "ConsoleInput").start();
    }
}
