package jp.akidukisystems.traindatamanager.Gson;

public class NetworkPacket {
    public String type;
    public String message;

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
    public int stateDestination;

    public int speedLimit;
    public boolean isTASCEnable;
    public boolean isTASCBraking;
    public boolean isTASCStopPos;

    public float move;
    public int moveTo;
    public float totalMove;
    
    public int formation;

    public boolean isOnRail;
    public boolean isComplessorActive;

    public NetworkPacket(
        String type, 
        String message, 

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
        int stateDestination,

        int speedLimit,
        boolean isTASCEnable,
        boolean isTASCBraking,
        boolean isTASCStopPos,

        float move, 
        int moveTo,
        float totalMove,

        int formation,

        boolean isOnRail, 
        boolean isComplessorActive
    ) {
        this.type = type;
        this.message = message;

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
        this.stateDestination = stateDestination;

        this.speedLimit = speedLimit;
        this.isTASCEnable = isTASCEnable;
        this.isTASCBraking = isTASCBraking;
        this.isTASCStopPos = isTASCStopPos;

        this.move = move;
        this.moveTo = moveTo;
        this.totalMove = totalMove;

        this.formation = formation;

        this.isOnRail = isOnRail;
        this.isComplessorActive = isComplessorActive;
    }
}