package jp.akidukisystems.software.TrainDataClient.Protector;

import jp.akidukisystems.software.TrainDataClient.TrainControl;

public class ATSPController
{
    public static final int ATSP_BRAKE_NWC_TIME = 50;
    private final TrainControl train;
    private Thread atspPatternWatcher;

    private float distance = 0f;
    private boolean isPatternActive = false;
    private float catchedDistance = 0f;
    private int targetSpeed = -1;

    private int targetSpeedTemp = -1;
    private float distancetemp = 0f;

    private boolean isATSPBrakeWorking = true;
    public boolean isATSPBrakeWorking()
    {
        return isATSPBrakeWorking;
    }
    public void setATSPBrakeWorking(boolean isATSPBrakeWorking)
    {
        this.isATSPBrakeWorking = isATSPBrakeWorking;
    }

    public ATSPController(TrainControl train)
    {
        this.train = train;
    }

    public boolean isPatternActive()
    {
        return isPatternActive;
    }

    public void setTargetSpeed(int targetSpeed)
    {
        targetSpeedTemp = targetSpeed;
    }
    
    public void setDistance(float ATSPPatternDistance)
    {
        distancetemp = ATSPPatternDistance;
    }

    public void setStopPattern(float distance)
    {
        resetATSP();
        
        this.distance = distance;
        if(distance != 0)
        {
            catchedDistance = train.getMove();
            targetSpeed = -1; // 停止パターンなのでターゲット速度は0にする
            isPatternActive = true;

            // ビーコン受信してパターン生成したら緩解表示は消える
            // ついでにATS-Pの計器ランプもつける
            train.setboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE, false);
            train.setboolTrainStat(TrainControl.ATS_P_ACTIVE, true);
        }
    }

    public void setDecelPattern()
    {
        resetATSP();

        targetSpeed = targetSpeedTemp;
        distance = distancetemp;
        catchedDistance = train.getMove();
        isPatternActive = true;

        // ビーコン受信してパターン生成したら緩解表示は消える
        // ついでにATS-Pの計器ランプもつける
        train.setboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE, false);
        train.setboolTrainStat(TrainControl.ATS_P_ACTIVE, true);
    }

    public void startPatternWatcher()
    {
        if (atspPatternWatcher == null || !atspPatternWatcher.isAlive())
        {
            atspPatternWatcher = new Thread(() ->
            {
                final float decel = 1.25f; // 減速度 m/s/s
                while (true)
                {
                    if (isPatternActive && train.getboolTrainStat(TrainControl.ATS_POWER)
                            && train.getboolTrainStat(TrainControl.ATS_P_POWER) && train.getboolTrainStat(TrainControl.ATS_P_ACTIVE))
                    {
                        float movedDistance = train.getMove() - catchedDistance;
                        if (movedDistance < 0) movedDistance *= -1;

                        float remain = distance - movedDistance -5; // 余裕持って5m手前にする
                        float speed = train.getSpeed();

                        System.out.println("ptrnDist:"+ distance +" movedDist:"+ movedDistance +" rem:"+ remain +"");

                        if ((remain <= 0) && (targetSpeed == -1))
                        {
                            // 停止限界超過
                            train.setboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING_EB, true);
                            train.setboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE, false);
                        }
                        else
                        {
                            // パターン生成
                            float vPattern = 0;

                            if (targetSpeed != -1)
                            {
                                // 減速パターン
                                if (remain > 0)
                                    vPattern = (float)Math.sqrt((targetSpeed / 3.6f) * (targetSpeed / 3.6f) + 2 * decel * remain) * 3.6f;
                                else
                                    vPattern = targetSpeed; // 制限区間内ならtargetSpeed
                            }
                            else
                                vPattern = (float)Math.sqrt(2 * decel * remain) * 3.6f; // 停車パターン
                            
                            System.out.println("speed:"+ speed +" vpat:"+ vPattern +"");

                            // パターン超過
                            if (speed > vPattern)
                            {
                                train.setboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING, true);
                                train.setboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE, false);
                            }

                            // 減速パターンの場合のみ自動で緩解
                            if ((targetSpeed != -1) && (speed < targetSpeed))
                                train.setboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING, false);

                            if (speed +10 > vPattern)
                            {
                                // パターン接近
                                train.setboolTrainStat(TrainControl.ATS_P_NEAR_PATTERN, true);
                            }
                            else
                            {
                                // パターンの範囲内なら勝手に消える
                                if(!train.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING) && !train.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING_EB))
                                    train.setboolTrainStat(TrainControl.ATS_P_NEAR_PATTERN, false);
                            }                        
                        }
                    }

                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }, "ATSPPatternWatcher");

            atspPatternWatcher.setDaemon(true);
            atspPatternWatcher.start();
        }
    }

    public void releaseATSPBrake()
    {
        // ふつうに緩解
        if(train.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING))
            train.setboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE, true);
            
        train.setboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING, false);
    }

    public void resetATSP()
    {
        // ATS-P非常制動にあたっちゃったとかで緩解できない場合とか
        // その場合はsuperがどうにかしてくれる
        isPatternActive = false;
        distance = 0f;
        catchedDistance = 0f;
        targetSpeed = -1;
        isATSPBrakeWorking = true;
        ATSPBrakeNWC = 0;
    }

    public void resetATSPFromInterface()
    {
        // ATS-P非常制動にあたっちゃったとかで緩解できない場合とか
        // その場合はsuperがどうにかしてくれる
        if (!train.isRunningTrain())
        {
            resetATSP();
            releaseATSPBrake();
        }
    }

    private int ATSPBrakeNWC = 0;

    // ATS-P異常時に列車とめる
    public void handleATSNW()
    {
        if (train.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING) && (train.getBc() < 50))
        {
            // ATS-Pブレーキ動作時にBC圧が50kpa未満
            if (isATSPBrakeWorking)
            {
                ATSPBrakeNWC ++;
                if (ATSPBrakeNWC > ATSP_BRAKE_NWC_TIME)
                {
                    isATSPBrakeWorking = false;
                }
            }
        }
        else
        {
            ATSPBrakeNWC = 0;
        }
    }
}
