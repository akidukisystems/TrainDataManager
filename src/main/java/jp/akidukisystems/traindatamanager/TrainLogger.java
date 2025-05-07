package jp.akidukisystems.traindatamanager;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.gson.Gson;

import jp.ngt.rtm.entity.npc.macro.TrainCommand;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;

@SideOnly(Side.CLIENT)
public class TrainLogger {
    private int tickCounter = 0;
    
    private NetworkManager networkManager = null;

    private EntityTrainBase train;
    private EntityVehicleBase vehicle;

    // ID取得
    public int id;
    public int id2;

    // ステータスを取得
    public byte stateDoor;
    public byte stateLight;
    public byte stateRollsign;
    public byte stateReverser;
    public byte statePantogtraph;
    public byte stateInteriorLight;

    // 速度とノッチ位置取得
    public float speed;
    public int notch;

    // BC MR圧力
    public int bc;
    public int mr;

    // 脱線・コンプレッサ
    public boolean isOnRail;
    public boolean isComplessorActive;

    public float movedDistance;

    private static final Gson gson = new Gson();
    private static final float SCALE_SPEED = 72f;
    private static final int SCALE_BC = 3;
    private static final float SCALE_MR = 0.311f;
    private static final float SCALE_DISTANCE = 1000f /3600f /4f;

    private boolean isFirst = true;

    enum commands {
        NOTCH, DOOR, MOVE, DISTANCE
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (networkManager != null) {
            networkManager.serverClose();
        }

        networkManager = new NetworkManager();
        networkManager.serverInit(ConfigManager.networkPort);
        networkManager.serverWaitingClient();

        this.id = 0;
        this.id2 = 0;

        // ステータスを取得
        this.stateDoor = 0;
        this.stateLight = 0;
        this.stateRollsign = 0;
        this.stateReverser = 0;
        this.statePantogtraph = 0;
        this.stateInteriorLight = 0;


        // 速度とノッチ位置取得
        this.speed = 0f;
        this.notch = 0;

        // BC MR圧力
        this.bc = 0;
        this.mr = 0;

        // 脱線・コンプレッサ
        this.isOnRail = false;
        this.isComplessorActive = false;

        this.movedDistance = 0f;
    }

    // MARK: INIT
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != Minecraft.getMinecraft().player) return;

        // Client側じゃないなら帰る
        if (!event.player.world.isRemote) return;

        // 通信できてないなら帰る
        if (networkManager == null) return;

        if (isFirst) {
            networkManager.serverStartRead();
            isFirst = false;
        }

        // お前列車に乗ってんの？
        if (event.player.isRiding() && event.player.getRidingEntity() instanceof EntityTrainBase) {
            // MARK: GET
            // データ取得
            String getData = null;

            getData = networkManager.getLatestReceivedString();

            // jsonパース
            // データあるのとデータ送信するtickなら初期化

            tickCounter++;
            if (getData != null || tickCounter >= 10) {
                // 列車の情報を取得
                this.train = (EntityTrainBase) event.player.getRidingEntity();
                this.vehicle = (EntityVehicleBase) event.player.getRidingEntity();

                // ID取得
                this.id = this.train.getEntityId();
                this.id2 = this.vehicle.getEntityId();

                // ステータスを取得
                this.stateDoor = this.vehicle.getVehicleState(TrainState.TrainStateType.Door);
                this.stateLight = this.vehicle.getVehicleState(TrainState.TrainStateType.Light);
                this.stateRollsign = this.vehicle.getVehicleState(TrainState.TrainStateType.Destination);
                this.stateReverser = this.vehicle.getVehicleState(TrainState.TrainStateType.Role);
                this.statePantogtraph = this.vehicle.getVehicleState(TrainState.TrainStateType.Pantograph);
                this.stateInteriorLight = this.vehicle.getVehicleState(TrainState.TrainStateType.InteriorLight);

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
                this.speed *= SCALE_SPEED;
                this.bc *= SCALE_BC;
                this.mr *= SCALE_MR;
            }

            if (getData != null) {
                GsonManager getDataParsed = gson.fromJson(getData, GsonManager.class);

                NetworkCommands command = NetworkCommands.fromString(getDataParsed.doAny);

                switch (command) {
                    case NOTCH:
                        int notchLevel = getDataParsed.notch;
                        if((-9<notchLevel) && (notchLevel<6)) {
                            this.train.setNotch(notchLevel);
                        }
                        break;
                
                    case DOOR:
                        byte doorStatus = (byte) getDataParsed.door;
                        if(doorStatus < 4) {
                            this.vehicle.setVehicleState(TrainState.TrainStateType.Door, doorStatus);
                        }
                        break;
                
                    case MOVE:
                        this.movedDistance = getDataParsed.move;
                        break;
                
                    case UNKNOWN:
                    default:
                        if (ConfigManager.isLogging) {
                            System.out.println("Unknown command: " + getDataParsed.doAny);
                        }
                        break;
                }

                if (ConfigManager.isLogging) System.out.println(String.format("GET"));
            }

            if (tickCounter < 10) return; // 10tickごと
            tickCounter = 0;

            // MARK: SEND
            // 1秒ごとに距離を積算する
            // なぜか2倍の値になるので/2する
            this.movedDistance += (float)this.speed * SCALE_DISTANCE;
            
            // 出力 jsonにするよ～
            GsonManager gsonManager = new GsonManager(
                "send", 
                "none", 

                this.id, 
                this.id2, 

                this.speed, 
                this.notch, 
                this.bc, 
                this.mr, 

                this.stateDoor, 
                this.stateLight, 
                this.stateRollsign, 
                this.stateReverser, 
                this.statePantogtraph, 
                this.stateInteriorLight, 

                this.movedDistance, 

                this.isOnRail, 
                this.isComplessorActive
            );

            

            // 送信
            String json = gson.toJson(gsonManager);
            if (ConfigManager.isLogging) System.out.println(json);

            networkManager.serverSendString(json);
        } else {
            networkManager.serverSendString("{\"type\":\"notRidingTrain\"}");
        }
    }


    // お切断
    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        System.out.println("disconnected.");
        networkManager.serverSendString("{\"type\":\"kill\"}");
        isFirst = true;
        networkManager.serverClose();
    }
}