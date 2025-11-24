package jp.akidukisystems.traindatamanager;

import com.google.gson.Gson;

import jp.akidukisystems.traindatamanager.Gson.NetworkPacket;
import jp.kaiz.atsassistmod.api.TrainControllerClient;
import jp.kaiz.atsassistmod.api.TrainControllerClientManager;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.Formation;
import jp.ngt.rtm.entity.train.util.FormationEntry;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TrainLogger {
    private int tickCounter = 0;
    
    private NetworkManager networkManager = null;

    private EntityTrainBase train;
    private EntityVehicleBase<?> vehicle;

    // ID取得
    private int id;
    private int id2;

    // ステータスを取得
    private byte stateDoor;
    private byte stateLight;
    private byte stateRollsign;
    private byte stateReverser;
    private byte statePantogtraph;
    private byte stateInteriorLight;
    private byte stateDestination;

    // 速度とノッチ位置取得
    private float speed;
    private int notch;

    // BC MR圧力
    private int bc;
    private int mr;

    // 脱線・コンプレッサ
    private boolean isOnRail;
    private boolean isComplessorActive;

    private int formation;

    // ATSA
    private TrainControllerClient tcc;
    private int speedLimit;
    private boolean isTASCEnable;
    private boolean isTASCBraking;
    private boolean isTASCStopPos;

    // キロ程
    private float movedDistance;
    private int moveTo;
    private float totalMove;

    public float getTotalMove() {
        return totalMove;
    }
    public void setTotalMove(float totalMove) {
        this.totalMove = totalMove;
    }
    // Getter and Setter
    public int getBc() {
        return bc;
    }
    public void setBc(int bc) {
        this.bc = bc;
    }

    public int getMr() {
        return mr;
    }
    public void setMr(int mr) {
        this.mr = mr;
    }

    public float getMovedDistance() {
        return movedDistance;
    }
    public void setMovedDistance(float movedDistance) {
        this.movedDistance = movedDistance;
    }

    public int getMoveTo() {
        return moveTo;
    }
    public void setMoveTo(int moveTo) {
        this.moveTo = moveTo;
    }

    public static int getCarsCount(EntityTrainBase train) {
        if (train == null) return 0;
        Formation f = train.getFormation();
        if (f == null || f.entries == null) return 0;

        int n = 0;
        for (FormationEntry e : f.entries) {
            if (e != null && e.train != null && !e.train.isDead) {
                n++;
            }
        }
        return n;
    }

    private static final Gson gson = new Gson();
    private static final float SCALE_SPEED = 72f;                   // 72分の1した値が出る
    private static final int SCALE_BC = 3;                          // 3分の1した値が出る
    private static final float SCALE_MR = 0.311f;                   // 0.311倍すると現実的な値になる
    private static final float SCALE_DISTANCE = 1000f /3600f /2f;   // 1km / 1h / 何故か2倍した値なので /2
    private static final int TICK_GET_DATA_INTERVAL = 10;           // 10tickごとに列車データ取得
    private static final int MOVE_DIRECTION_UP = 0;                 // 上り方向
    private static final int MOVE_DIRECTION_DOWN = 1;               // 下り方向

    private boolean isFirst = true;

    private void Initialize(){
        this.id = 0;
        this.id2 = 0;

        // ステータスを取得
        this.stateDoor = 0;
        this.stateLight = 0;
        this.stateRollsign = 0;
        this.stateReverser = 0;
        this.statePantogtraph = 0;
        this.stateInteriorLight = 0;
        this.stateDestination = 0;

        // 速度とノッチ位置取得
        this.speed = 0f;
        this.notch = 0;

        // BC MR圧力
        this.bc = 0;
        this.mr = 0;

        // 脱線・コンプレッサ
        this.isOnRail = false;
        this.isComplessorActive = false;

        // ATSA
        this.speedLimit = Integer.MAX_VALUE;
        this.isTASCEnable = false;
        this.isTASCBraking = false;
        this.isTASCStopPos = false;

        // 移動距離
        // moveTo = 0...上り（カウントダウン）　1...下り（カウントアップ）
        this.movedDistance = 0f;
        this.moveTo = 1;
        this.totalMove = 0f;

        this.formation = 0;
    }

    private void getTrainData(EntityPlayer player){
        // 列車の情報を取得
        Entity entity = player.getRidingEntity();
        if (entity instanceof EntityTrainBase && entity instanceof EntityVehicleBase) {
            this.train = (EntityTrainBase) entity;
            this.vehicle = (EntityVehicleBase<?>) entity;
        } else {
            return; // 不正な乗り物なら何もしない
        }

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
        this.stateDestination = this.vehicle.getVehicleState(TrainState.TrainStateType.Destination);

        // 速度とノッチ位置取得
        this.speed = this.train.getSpeed();
        this.notch = this.train.getNotch();

        // BC MR圧力
        this.bc = this.train.brakeCount;
        this.mr = this.train.brakeAirCount;

        // 脱線・コンプレッサ
        this.isOnRail = this.train.onRail;
        this.isComplessorActive = this.train.complessorActive;

        this.formation = getCarsCount(train);

        // ATSA
        if ((tcc = TrainControllerClientManager.getTCC(train)) != null) {
            this.speedLimit = tcc.getATCSpeed();
            this.isTASCEnable = true;
            this.isTASCBraking = tcc.isTASC();
            this.isTASCStopPos = false;
        }

        // 値を正規化
        this.speed *= SCALE_SPEED;
        this.bc *= SCALE_BC;
        this.mr *= SCALE_MR;
    }

    private void handleGetData(String str) {
        NetworkPacket getDataParsed = gson.fromJson(str, NetworkPacket.class);

        NetworkCommands command = NetworkCommands.fromString(getDataParsed.message);

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

            case MOVETO:
                this.moveTo = getDataParsed.moveTo;
                break;

            case REVERSER:
                this.vehicle.setVehicleState(TrainState.TrainStateType.Role, (byte)getDataParsed.reverser);
                break;
        
            case UNKNOWN:
            default:
                if (ConfigManager.isLogging) {
                    System.out.println("Unknown command: " + getDataParsed.message);
                }
                break;
        }
    }



    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (networkManager != null) {
            networkManager.serverClose();
        }

        networkManager = new NetworkManager();
        networkManager.serverInit(ConfigManager.networkPort);
        networkManager.serverWaitingClient();

        Initialize();
    }

    // MARK: INIT
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;

        if (event.phase != TickEvent.Phase.END) return;
        if (player != Minecraft.getMinecraft().player) return;

        // Client側じゃないなら帰る
        if (!player.world.isRemote) return;

        // 通信できてないなら帰る
        if (networkManager.s2c == null) return;

        if (isFirst) {
            networkManager.serverStartRead();
            networkManager.serverSendString("{\"type\":\"start\",\"version\":\""+ TDMCore.VERSION +"\"}");
            isFirst = false;
        }

        // お前列車に乗ってんの？
        if (player.isRiding() && player.getRidingEntity() instanceof EntityTrainBase)
        {
            // MARK: GET
            // データ取得
            String getData = null;
            boolean refreshedTrain = false;

            while ((getData = networkManager.getLatestReceivedString()) != null)
            {
                System.out.println(getData);
                if(!refreshedTrain)
                {
                    refreshedTrain = true;
                    getTrainData(player);
                }
                handleGetData(getData);   // オラッ！全部吐け！！お前が握ってるのは知ってんだよ！！！！（データを全部吐かせる）
            }


            // jsonパース
            // データあるのとデータ送信するtickなら初期化
            String NBTJson = BlockNBTGetter.getNBTAtPlayerFoot(player);
            if (NBTJson != null) networkManager.serverSendString(NBTJson);

            // 列車情報取得
            // 取得情報処理
            tickCounter++;
            if (!refreshedTrain && tickCounter >= TICK_GET_DATA_INTERVAL) getTrainData(player);

            if (tickCounter < TICK_GET_DATA_INTERVAL) return; // 10tickごと
            tickCounter = 0;

            // MARK: SEND
            // 1秒ごとに距離を積算する
            // なぜか2倍の値になるので/2する

            // 上りで0未満になってしまうならカウントアップ
            float move1sec = (float)this.speed * SCALE_DISTANCE;
            if ((moveTo == MOVE_DIRECTION_UP) && ((this.movedDistance - move1sec) < 0f )) moveTo = MOVE_DIRECTION_DOWN;

            if (moveTo == MOVE_DIRECTION_DOWN) {
                this.movedDistance += move1sec;
            } else {
                this.movedDistance -= move1sec;
            }

            this.totalMove += move1sec;
            
            
            // 出力 jsonにするよ～
            NetworkPacket gsonManager = new NetworkPacket(
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
                this.stateDestination,

                this.speedLimit,
                this.isTASCEnable,
                this.isTASCBraking,
                this.isTASCStopPos,

                this.movedDistance, 
                this.moveTo,
                this.totalMove,
                this.formation,

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
        if (networkManager != null) {
            networkManager.serverSendString("{\"type\":\"kill\"}");
            networkManager.serverClose();
        }
        isFirst = true;
    }
}