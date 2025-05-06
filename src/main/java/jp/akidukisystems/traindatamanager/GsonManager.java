package jp.akidukisystems.traindatamanager;

import scala.annotation.meta.companionObject;

public class GsonManager {
    public String type;
    public String doAny;
    public int id;
    public int id2;
    public float speed;
    public int notch;
    public int door;
    public int bc;
    public int mr;
    public float move;
    public boolean isOnRail;
    public boolean isComplessorActive;

    public GsonManager(String type, String doAny, int id, int id2, float speed, int notch, int door, int bc, int mr, float move, boolean isOnRail, boolean isComplessorActive) {
        this.type = type;
        this.doAny = doAny;
        this.id = id;
        this.id2 = id2;
        this.speed = speed;
        this.notch = notch;
        this.door = door;
        this.bc = bc;
        this.mr = mr;
        this.move = move;
        this.isOnRail = isOnRail;
        this.isComplessorActive = isComplessorActive;
    }
}
