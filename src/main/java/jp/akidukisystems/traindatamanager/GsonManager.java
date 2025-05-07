package jp.akidukisystems.traindatamanager;

import scala.annotation.meta.companionObject;

public class GsonManager {
    public String type;
    public String doAny;

    public int id;
    public int id2;

    public float speed;
    public int notch;
    public int bc;
    public int mr;

    public int door;
    public int light;
    public int rollsign;
    public int reverser;
    public int pantograph;
    public int interiorlight;

    public float move;

    public boolean isOnRail;
    public boolean isComplessorActive;

    public GsonManager(
        String type, 
        String doAny, 

        int id, 
        int id2, 
        
        float speed, 
        int notch, 
        int bc, 
        int mr, 

        int door, 
        int light, 
        int rollsign, 
        int reverser, 
        int pantograph, 
        int interiorlight, 

        float move, 

        boolean isOnRail, 
        boolean isComplessorActive
        ) {
        this.type = type;
        this.doAny = doAny;

        this.id = id;
        this.id2 = id2;

        this.speed = speed;
        this.notch = notch;
        this.bc = bc;
        this.mr = mr;

        this.door = door;
        this.light = light;
        this.rollsign = rollsign;
        this.reverser = reverser;
        this.pantograph = pantograph;
        this.interiorlight = interiorlight;

        this.move = move;

        this.isOnRail = isOnRail;
        this.isComplessorActive = isComplessorActive;
    }
}