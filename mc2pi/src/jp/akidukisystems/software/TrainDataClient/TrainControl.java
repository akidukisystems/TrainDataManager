package jp.akidukisystems.software.TrainDataClient;

import javax.swing.Timer;
import jp.akidukisystems.software.TrainDataClient.Protector.ATSPController;

public class TrainControl
{

    public static final int ATS_OPERATING = 0;
    public static final int ATS_POWER = 1;
    public static final int ATS_P_ERROR = 2;
    public static final int ATS_P_ACTIVE = 3;
    public static final int ATS_P_BRAKE_RELEASE = 4;
    public static final int ATS_P_BRAKE_OPERATING = 5;
    public static final int ATS_P_NEAR_PATTERN = 6;
    public static final int ATS_P_POWER = 7;
    public static final int ATS_P_BRAKE_OPERATING_EB = 8;

    public static final int TRAINSTAT_PARKING = 10;
    public static final int TRAINSTAT_CONSTANT_SPEED = 11;
    public static final int TRAINSTAT_DS_BRAKE = 12;
    public static final int TRAINSTAT_SNOW_BRAKE = 13;
    public static final int TRAINSTAT_EMERG_SHORT = 14;
    public static final int TRAINSTAT_THREE_PHASE = 15;
    public static final int TRAINSTAT_EB = 16;

    public static final int TASC_POWER = 20;
    public static final int TASC_PATTERN_ACTIVE = 21;
    public static final int TASC_BRAKE = 22;
    public static final int TASC_OFF = 23;
    public static final int TASC_ERROR = 24;

    public static final int TRAINSTAT_EX_DOOR_CLOSE = 30;
    public static final int TRAINSTAT_EX_EB = 31;
    public static final int TRAINSTAT_EX_STA = 32;

    public static final int TRAINSTAT_HIDE_EBBUZZER = 100;

    public static final int NOTCH_EB = -8;
    public static final int NOTCH_MAX = -7;
    public static final int NOTCH_N = 0;
    public static final int NOTCH_NONE = -32768;

    public static final int DOOR_CLOSE_TIME = 8;

    Timer ebTimer;
    Timer ebActiveTimer;

    enum formationInfo
    {
        Tc,
        T,
        M,
        Mc
    }

    public TrainControl()
    {
        atspController = new ATSPController(this);
    }
    
    private ATSPController atspController;
    public ATSPController getATSPController() 
    {
        return atspController;
    }

    private int id;
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    private int prevId;

    public int getPrevId()
    {
        return prevId;
    }
    public void setPrevId(int prevId)
    {
        this.prevId = prevId;
    }

    private float speed = 0f;
    public float getSpeed()
    {
        return speed;
    }
    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

    private int notch = 0;
    public int getNotch()
    {
        return notch;
    }
    public void setNotch(int notch)
    {
        this.notch = notch;
    }

    private int door = 0;
    public int getDoor()
    {
        return door;
    }
    public void setDoor(int door)
    {
        this.door = door;
    }

    private int bc = 0;
    public int getBc()
    {
        return bc;
    }
    public void setBc(int bc)
    {
        this.bc = bc;
    }

    private int mr = 0;
    public int getMr()
    {
        return mr;
    }
    public void setMr(int mr)
    {
        this.mr = mr;
    }

    private float move = 0f;
    public float getMove()
    {
        return move;
    }
    public void setMove(float move)
    {
        this.move = move;
    }

    private int moveTo = 1;
    public int getMoveTo()
    {
        return moveTo;
    }
    public void setMoveTo(int moveTo)
    {
        this.moveTo = moveTo;
    }

    private int reverser = 0;
    public int getReverser()
    {
        return reverser;
    }
    public void setReverser(int reverser)
    {
        this.reverser = reverser;
    }

    private int limit = Integer.MAX_VALUE;
    public int getLimit()
    {
        return limit;
    }
    public void setLimit(int limit) 
    {
        this.limit = limit;
    }

    private boolean isTASCEnable = true;
    public boolean isTASCEnable()
    {
        return isTASCEnable;
    }
    public void setTASCEnable(boolean isTASCEnable)
    {
        this.isTASCEnable = isTASCEnable;
    }

    private boolean isTASCBraking = false;
    public boolean isTASCBraking()
    {
        return isTASCBraking;
    }
    public void setTASCBraking(boolean isTASCBraking)
    {
        this.isTASCBraking = isTASCBraking;
    }

    private boolean isTE = false;
    public boolean isTE()
    {
        return isTE;
    }
    public void setTE(boolean isTE)
    {
        this.isTE = isTE;
    }

    private boolean isArrivingStation = false;
    private float beaconGetedPos = 0;
    private boolean isRaisingEP = false;
    private boolean isCatchingEP = false;

    public boolean isRaisingEP()
    {
        return isRaisingEP;
    }
    public void setRaisingEP(boolean isRaisingEP)
    {
        this.isRaisingEP = isRaisingEP;
    }

    public boolean isCatchingEP()
    {
        return isCatchingEP;
    }
    public void setCatchingEP(boolean isCatchingEP)
    {
        this.isCatchingEP = isCatchingEP;
    }

    private boolean boolTrainStat[];
    public void boolTrainStatInit(int size)
    {
        boolTrainStat = new boolean[size];
    }

    private final Object boolLock = new Object();

    public boolean getboolTrainStat(int index)
    {
        synchronized (boolLock)
        {
            if (index < 0 || index >= boolTrainStat.length)
                throw new IndexOutOfBoundsException("Invalid index");
            return boolTrainStat[index];
        }
    }

    public void setboolTrainStat(int index, boolean value)
    {
        synchronized (boolLock)
        {
            if (index < 0 || index >= boolTrainStat.length)
                throw new IndexOutOfBoundsException("Invalid index");
            boolTrainStat[index] = value;
        }
    }

    private int cars = 0;
    public int getCars()
    {
        return cars;
    }
    public void setCars(int cars)
    {
        this.cars = cars;
    }

    String txtTrainStat = "";
    String txtATS = "";
    String txtTASC = "";
    String txtTrainStatEx = "";

    private boolean isDoorClose = true;
    private boolean isRunningDoorOpen = false;

    private int prevDoor = -1;
    public int getPrevDoor()
    {
        return prevDoor;
    }
    public void setPrevDoor(int prevDoor)
    {
        this.prevDoor = prevDoor;
    }

    private int prevNotch = 0;
    public int getPrevNotch()
    {
        return prevNotch;
    }
    public void setPrevNotch(int prevNotch) 
    {
        this.prevNotch = prevNotch;
    }

    private int doorCloseCount = 0;
    private int prevReverser = -1;

    public boolean isRunningTrain()
    {
        return (speed > 5f);
    }

    public void handleArrivingStation()
    {
        // 次駅まで300m未満
        if (beaconGetedPos != 0f)
        {
            System.out.println(beaconGetedPos + 300f - move);
            System.out.println(speed * 3);

            // 次駅接近報知
            if (!isArrivingStation)
            {
                // 今の速度から駅までの予想停車距離を計算し、駅までの距離を上回ったら報知
                if ((speed * 3) > (beaconGetedPos + 300f - move))
                {
                    isArrivingStation = true;
                }
            }
        }

        // 所定停目付近に停車か、100m以上通過した場合解除
        if ((0 >= (beaconGetedPos + 300f - move + 100f)) || (!isRunningTrain() && (5f >= (beaconGetedPos + 300f - move))))
        {
            isArrivingStation = false;
            beaconGetedPos = 0f;
        }
    }

    // 列車接近報知機の地上子設定
    public void setArraivingStation(int signal_1)
    {
        if (signal_1 == 1) beaconGetedPos = move;
        if (signal_1 == 2)
        {
            // 強制的に通過判定
            beaconGetedPos = 0f;
            isArrivingStation = false;
        }
    }

    public void handleTEunlock() 
    {
        // TE装置解除用
        if ((isTE) && (notch != NOTCH_EB)) 
        {
            isTE = false;
        }
    }

    public void handleEBunlock()
    {
        // EB装置 非常制動信号解除用
        if ((boolTrainStat[TRAINSTAT_EX_EB]) && !isRunningTrain())
        {
            boolTrainStat[TRAINSTAT_EX_EB] = false;
            ebTimer.stop();
            ebActiveTimer.stop();
        }

        // EB装置 ブザー解除用
        if ((boolTrainStat[TRAINSTAT_HIDE_EBBUZZER]) && (notch != NOTCH_EB) && !isRunningTrain())
        {
            boolTrainStat[TRAINSTAT_HIDE_EBBUZZER] = false;
        }
    }

    public void handleEB()
    {
        // EB装置
        if (isRunningTrain())
        {
            if (prevNotch == notch)
            {
                // ノッチいじいじされてないなら、タイマースタート
                if (!ebTimer.isRunning()) ebTimer.start();
            }
            else
            {
                // ノッチいじいじしたのでタイマー消す
                if (ebActiveTimer.isRunning() && !boolTrainStat[TRAINSTAT_EX_EB])
                {
                    boolTrainStat[TRAINSTAT_EX_EB] = false;
                    boolTrainStat[TRAINSTAT_HIDE_EBBUZZER] = false;
                }
                ebTimer.stop();
                ebActiveTimer.stop();
            }
        }
        else
        {
            if (ebActiveTimer.isRunning())
            {
                boolTrainStat[TRAINSTAT_EX_EB] = false;
                boolTrainStat[TRAINSTAT_HIDE_EBBUZZER] = false;
            }
            ebTimer.stop();
            ebActiveTimer.stop();
        }
    }

    public void handleRunningOpen()
    {
        // 走行中戸開時 
        if (isRunningTrain() && !isDoorClose)
        {
            isRunningDoorOpen = true;
        }
        else
        {
            isRunningDoorOpen = false;
        }
    }

    public void handleDoors()
    {
        // ドア閉めるとき時間差で表示
        if (door == 0)
        {
            if (!isDoorClose)
            {
                doorCloseCount ++;
                if (doorCloseCount > DOOR_CLOSE_TIME)
                {
                    isDoorClose = true;
                }
            }
        }
        else
        {
            doorCloseCount = 0;
            isDoorClose = false;
        }
    }

    private boolean isTestingATS = false;
    private long testStartTime = 0L;
    private static final long TEST_DURATION_MS = 3000;

    public void refreshTrainStat()
    {
        int currentReverser = getReverser();

        if(currentReverser == 0 && prevReverser != currentReverser)
        {
            // ATSテスト
            isTestingATS = true;
            testStartTime = System.currentTimeMillis();
            atspController.resetATSP();
        }

        if(isTestingATS)
        {
            long elapsed = System.currentTimeMillis() - testStartTime;
            if(elapsed < TEST_DURATION_MS)
            {
                // テスト中
                boolTrainStat[ATS_POWER] = true;
                boolTrainStat[ATS_P_POWER] = true;
                boolTrainStat[ATS_P_ERROR] = true;
                boolTrainStat[ATS_OPERATING] = true;
            }
            else
            {
                // テストおわり
                isTestingATS = false;
                boolTrainStat[ATS_POWER] = true;
                boolTrainStat[ATS_P_POWER] = true;
                boolTrainStat[ATS_P_ERROR] = false;
                boolTrainStat[ATS_OPERATING] = false;
            }
        }
        else
        {
            if(currentReverser == 0)
            {
                boolTrainStat[ATS_POWER] = true;
                boolTrainStat[ATS_P_POWER] = true;
            }
            else
            {
                boolTrainStat[ATS_POWER] = false;
                boolTrainStat[ATS_P_POWER] = false;
                boolTrainStat[TASC_POWER] = false;
            }
        }

        // ATS-P
        if (boolTrainStat[ATS_POWER] && boolTrainStat[ATS_P_POWER])
        {
            boolTrainStat[ATS_P_ACTIVE] = limit != Integer.MAX_VALUE ? true : false;
            
            if(!atspController.isPatternActive())
            {
                boolTrainStat[ATS_P_NEAR_PATTERN] = false;
                if ((limit -5) < speed)
                {
                    if( boolTrainStat[ATS_P_ACTIVE] == true)
                        boolTrainStat[ATS_P_NEAR_PATTERN] = true;
                }

                boolTrainStat[ATS_P_BRAKE_OPERATING] = false;
                if (limit < speed)
                {
                    if( boolTrainStat[ATS_P_ACTIVE] == true)
                        boolTrainStat[ATS_P_BRAKE_OPERATING] = true;
                }

                boolTrainStat[ATS_P_BRAKE_OPERATING_EB] = false;
                if (!atspController.isATSPBrakeWorking()) boolTrainStat[ATS_P_BRAKE_OPERATING_EB] = true;
            } 
        }
        else
        {
            boolTrainStat[ATS_P_ACTIVE] = false;
            boolTrainStat[ATS_P_NEAR_PATTERN] = false;
            boolTrainStat[ATS_P_BRAKE_OPERATING] = false;
            boolTrainStat[ATS_P_BRAKE_OPERATING_EB] = false;
        }

        // EB EB EB
        boolTrainStat[TRAINSTAT_EB] = false;
        if (isTE) boolTrainStat[TRAINSTAT_EB] = true;
        if (isRunningDoorOpen) boolTrainStat[TRAINSTAT_EB] = true;
        if (!boolTrainStat[ATS_POWER]) boolTrainStat[TRAINSTAT_EB] = true;
        if (reverser != 0) boolTrainStat[TRAINSTAT_EB] = true;
        if (boolTrainStat[ATS_OPERATING]) boolTrainStat[TRAINSTAT_EB] = true;
        if (boolTrainStat[ATS_P_BRAKE_OPERATING_EB]) boolTrainStat[TRAINSTAT_EB] = true;
        if (boolTrainStat[ATS_P_ERROR]) boolTrainStat[TRAINSTAT_EB] = true;
        if (boolTrainStat[TRAINSTAT_EX_EB]) boolTrainStat[TRAINSTAT_EB] = true;

        
        // 保安ブレーキ
        boolTrainStat[TRAINSTAT_DS_BRAKE] = false;
        if (mr < 700) boolTrainStat[TRAINSTAT_DS_BRAKE] = true;

        
        
        // TASC
        boolTrainStat[TASC_POWER] = isTASCEnable;
        
        boolTrainStat[TASC_PATTERN_ACTIVE] = false;
        boolTrainStat[TASC_BRAKE] = false;
        if (isTASCBraking && (currentReverser == 0))
        {
            boolTrainStat[TASC_POWER] = true;
            boolTrainStat[TASC_PATTERN_ACTIVE] = true;
            boolTrainStat[TASC_BRAKE] = true;
        }

        // ドア
        boolTrainStat[TRAINSTAT_EX_DOOR_CLOSE] = isDoorClose;

        boolTrainStat[TRAINSTAT_EX_STA] = isArrivingStation;

        prevReverser = currentReverser;
    }

    public void resetTrain()
    {
        id = 0;
        prevId = 0;

        speed = 0f;
        notch = 0;
        door = 0;
        bc = 0;
        mr = 0;
        move = 0f;
        moveTo = 1;
        reverser = 0;
        limit = Integer.MAX_VALUE;

        isTASCEnable = true;
        isTASCBraking = false;

        isTE = false;

        prevDoor = -1;
        prevNotch = 0;

        isDoorClose = true;
        isRunningDoorOpen = false;

        doorCloseCount = 0;

        isArrivingStation = false;
        beaconGetedPos = 0;

        txtTrainStat = "";
        txtATS = "";
        txtTASC = "";
        txtTrainStatEx = "";

        boolTrainStat[ATS_POWER] = false;
        boolTrainStat[ATS_P_POWER] = false;
        boolTrainStat[TASC_POWER] = false;

        atspController.startPatternWatcher();
        atspController.resetATSP();
    }

    public void refreshTimer()
    {
        // EB装置関連
        ebTimer = new Timer(60000, keyword ->
        {
            // 60秒間操作ないぞ！！！
            setboolTrainStat(TrainControl.TRAINSTAT_HIDE_EBBUZZER, true);
            ebActiveTimer.start();
            ebTimer.stop();
        });

        ebActiveTimer = new Timer(5000, keyword ->
        {
            // 5秒経ってもなにもしないから止めるぞ！！！
            setboolTrainStat(TrainControl.TRAINSTAT_EX_EB, true);
            ebTimer.stop();
            ebActiveTimer.stop();
        });
    }
    
}
