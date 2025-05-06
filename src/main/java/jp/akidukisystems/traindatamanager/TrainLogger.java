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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.event.world.WorldEvent;

public class TrainLogger {
    private int tickCounter = 0;
    public float movedDistance = 0;
    NetworkManager networkManager = null;

    EntityTrainBase train;
    EntityVehicleBase vehicle;

    // ID取得
    int id;
    int id2;

    // ステータスを取得
    byte doorState;

    // 速度とノッチ位置取得
    float speed;
    int notch;

    // BC MR圧力
    int bc;
    int mr;

    // 脱線・コンプレッサ
    boolean isOnRail;
    boolean isComplessorActive;

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (networkManager != null) {
            networkManager.serverClose();
        }

        networkManager = new NetworkManager();
        networkManager.serverInit(ConfigManager.networkPort);
        networkManager.serverWaitingClient();
    }

    // MARK: INIT
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Client側じゃないなら帰る
        if (!event.player.world.isRemote) return;

        // お前列車に乗ってんの？
        if (event.player.isRiding() && event.player.getRidingEntity() instanceof EntityTrainBase) {
            // MARK: GET
            // データ取得
            String getData = null;

            try {
                getData = networkManager.serverReciveString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // jsonパース
            // データあるのとデータ送信するtickなら初期化

            tickCounter++;
            if (getData != null || tickCounter < 10) {
                // 列車の情報を取得
                this.train = (EntityTrainBase) event.player.getRidingEntity();
                this.vehicle = (EntityVehicleBase) event.player.getRidingEntity();

                // ID取得
                this.id = this.train.getEntityId();
                this.id2 = this.vehicle.getEntityId();

                // ステータスを取得
                this.doorState = this.vehicle.getVehicleState(TrainState.TrainStateType.Door);

                // 速度とノッチ位置取得
                this.speed = this.train.getSpeed();
                this.notch = this.train.getNotch();

                // BC MR圧力
                this.bc = this.train.brakeCount;
                this.mr = this.train.brakeAirCount;

                // 脱線・コンプレッサ
                this.isOnRail = this.train.onRail;
                this.isComplessorActive = this.train.complessorActive;

                // 値を正規化
                this.speed *= 72f;
                this.bc *= 3;
                this.mr *= 0.311f;
            }

            if (getData != null) {
                Gson gson = new Gson();
                GsonManager getDataParsed = gson.fromJson(getData, GsonManager.class);
                
                switch (getDataParsed.doAny) {
                    case "notch":
                        int notchLevel = getDataParsed.notch;
                        if((-9<notchLevel) && (notchLevel<6)) {
                            this.train.setNotch(notchLevel);
                        }
                        break;

                    case "door":
                        byte doorStatus = (byte) getDataParsed.door;
                        if(doorStatus < 4) {
                            this.vehicle.setVehicleState(TrainState.TrainStateType.Door, doorStatus);
                        }
                        break;

                    case "move":
                        movedDistance = getDataParsed.move;
                        break;
                    
                    default:
                        break;
                }

                if (ConfigManager.isLogging) System.out.println(String.format("GET"));
            }

            if (tickCounter < 10) return; // 10tickごと
            tickCounter = 0;

            // MARK: SEND
            // 1秒ごとに距離を積算する
            // なぜか2倍の値になるので/2する
            movedDistance += (float)this.speed *(1000f /3600f) /4f;
            
            // 出力 jsonにするよ～
            Gson gson = new Gson();
            GsonManager gsonManager = new GsonManager("send", "none", this.id, this.id2, this.speed, this.notch, this.doorState, this.bc, this.mr, this.movedDistance, this.isOnRail, this.isComplessorActive);

            // 送信
            String json = gson.toJson(gsonManager);
            if (ConfigManager.isLogging) System.out.println(json);

            try {
                networkManager.serverSendString(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 列車に乗ってないね
            try {
                networkManager.serverSendString("{\"type\":\"notRidingTrain\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // お切断
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