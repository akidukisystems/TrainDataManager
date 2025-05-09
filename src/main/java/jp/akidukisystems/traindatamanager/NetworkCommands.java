package jp.akidukisystems.traindatamanager;

public enum NetworkCommands {
    NOTCH,
    DOOR,
    MOVE,
    MOVETO,
    REVERSER,
    UNKNOWN;

    public static NetworkCommands fromString(String value) {
        if (value == null) return UNKNOWN;
        try {
            return NetworkCommands.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}