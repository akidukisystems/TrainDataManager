package jp.akidukisystems.software.TrainDataClient;

public class EventWatcher
{
    int door = 0;
    float speed = 0f;
    boolean isArrivingStation = false;

    private boolean doorClosed = false;
    private boolean tractionOn = false;

    private boolean passing    = false;

    private boolean arrivingCleared = false;
    private boolean trainStopped = false;
    private boolean doorOpened = false;

    public void eventsCheck(boolean isArrivingStation, int door, float speed)
    {
        departEventCheck(door, speed);
        arriveEventCheck(isArrivingStation, door, speed);

        this.door = door;
        this.speed = speed;
        this.isArrivingStation = isArrivingStation;
    }

    private void departEventCheck(int door, float speed)
    {
        if
        (
            this.door != 0 && 
            this.door != door && 
            door == 0
        ) {
            doorClosed = true;
        }

        if(doorClosed)
        {
            if
            (
                this.speed == 0f &&
                this.speed != speed &&
                speed != 0f
            ) {
                tractionOn = true;
            }
        }
    }

    /* 未検証 */
    private void arriveEventCheck(boolean isArrivingStation, int door, float speed)
    {
        // isArrivingStation が true → false    (次駅接近状態が解除された状態) かつ
        // speed が             >0f → 0f        (列車が停車した瞬間～停車中) かつ
        // door が              0 → !=0         (ドアが開いた瞬間～今開いている) 状態

        if
        (
            this.isArrivingStation &&
            this.isArrivingStation != isArrivingStation &&
            !isArrivingStation
        )
        {
            arrivingCleared = true;
        }

        if
        (
            arrivingCleared &&
            this.speed > 0f &&
            this.speed != speed &&
            speed == 0f
        )
        {
            trainStopped = true;
        }

        if
        (
            trainStopped &&
            arrivingCleared &&
            this.door == 0 &&
            this.door != door &&
            door != 0
        )
        {
            doorOpened = true;
        }
    }

    public void passingEventRaise()
    {
        passing = true;
    }



    public void resetDepartEvent()
    {
        doorClosed = false;
        tractionOn = false;
    }

    public void resetPassingEvent()
    {
        passing = false;
    }

    public void resetArriveEvent()
    {
        arrivingCleared = false;
        trainStopped = false;
        doorOpened = false;
    }

    
    public boolean isArrivedStation()
    {
        return (arrivingCleared && trainStopped && doorOpened);
    }

    public boolean isPassedStation()
    {
        return passing;
    }
}
