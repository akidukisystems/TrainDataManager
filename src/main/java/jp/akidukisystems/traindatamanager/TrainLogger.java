package jp.akidukisystems.traindatamanager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.gson.Gson;

import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class TrainLogger {
    private int tickCounter = 0;
    public float movedDistance = 0;
    NetworkManager networkManager = null ;

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (networkManager != null) {
            networkManager.serverClose();
        }

        networkManager = new NetworkManager();
        networkManager.serverInit(ConfigManager.networkPort);
        networkManager.serverWaitingClient();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < 10) return; // 10tickごと
        tickCounter = 0;

        if (event.player.isRiding() && event.player.getRidingEntity() instanceof EntityTrainBase) {

            // trainを取得、ついでにvehicleも
            EntityTrainBase train = (EntityTrainBase) event.player.getRidingEntity();
            EntityVehicleBase vehicle = (EntityVehicleBase) event.player.getRidingEntity();

            // ステータスを取得
            byte doorState = vehicle.getVehicleState(TrainState.TrainStateType.Door);

            // 速度とノッチ位置取得
            float speed = train.getSpeed();
            int notch = train.getNotch();

            // BC MR圧力
            int bc = train.brakeCount;
            int mr = train.brakeAirCount;

            boolean isOnRail = train.onRail;
            boolean isComplessorActive = train.complessorActive;

            // 1秒ごとに距離を積算する
            // なぜか2倍の値になるので/2する
            movedDistance += (float)speed *72f *(1000f /3600f) /4f;
            
            // 出力 速度は72倍すること
            if (ConfigManager.isLogging) System.out.println(String.format("speed:%.2fkm/h notch:%d door:%d bc:%d mr:%d move:%.2f onRail:%b compAct:%d", speed *72f, notch, (int)doorState, bc *3, mr, movedDistance, isOnRail, isComplessorActive));

            String json = "{\"type\":\"send\",\"speed\":" + speed *72f + ",\"notch\":"+ notch +",\"door\":"+ doorState +",\"bc\":"+ bc *3 +",\"mr\":"+ mr +",\"move\":"+ movedDistance +"}";
            try {
                networkManager.serverSendString(json);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            

            String getData = null;
            try {
                getData = networkManager.serverReciveString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (getData != null) {
                Gson gson = new Gson();
                GsonManager getDataParsed = gson.fromJson(getData, GsonManager.class);
                
                switch (getDataParsed.doAny) {
                    case "notch":
                        int notchLevel = getDataParsed.notch;
                        if((-9<notchLevel) && (notchLevel<6)) {
                            train.setNotch(notchLevel);
                        }
                        break;

                    case "door":
                        byte doorStatus = (byte) getDataParsed.door;
                        if(doorStatus<4) {
                            vehicle.setVehicleState(TrainState.TrainStateType.Door, doorStatus);
                        }
                        break;

                    case "distance":
                        movedDistance = getDataParsed.move;
                        break;
                    
                    default:
                        break;
                }

                if (ConfigManager.isLogging) System.out.println(String.format("GET notch:%d door:%d move:%.2f", getDataParsed.notch, getDataParsed.door, getDataParsed.move));
            }
        }
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        System.out.println("disconnected.");
        try {
            networkManager.serverSendString("{\"type\":\"kill\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
        networkManager.serverClose();
    }
}