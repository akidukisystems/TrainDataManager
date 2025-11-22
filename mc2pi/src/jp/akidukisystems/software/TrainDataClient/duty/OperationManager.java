package jp.akidukisystems.software.TrainDataClient.duty;

public class OperationManager
{
    private DutyCardReader.TrainNumber trainNumber;

    public DutyCardReader.TrainNumber getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(DutyCardReader.TrainNumber trainNumber) {
        this.trainNumber = trainNumber;
    }

    private DutyCardReader.Station station;

    public DutyCardReader.Station getStation() {
        return station;
    }

    public void setStation(DutyCardReader.Station station) {
        this.station = station;
    }

    private DutyCardReader.Direction direction;

    public DutyCardReader.Direction getDirection() {
        return direction;
    }

    public void setDirection(DutyCardReader.Direction direction) {
        this.direction = direction;
    }

    private DutyCardReader.Line line;

    public DutyCardReader.Line getLine() {
        return line;
    }

    public void setLine(DutyCardReader.Line line) {
        this.line = line;
    }

    private DutyCardReader.StopPattern stopPattern;

    public DutyCardReader.StopPattern getStopPattern() {
        return stopPattern;
    }

    public void setStopPattern(DutyCardReader.StopPattern stopPattern) {
        this.stopPattern = stopPattern;
    }

    private DutyCardReader.TimeTable timeTable;

    public DutyCardReader.TimeTable getTimeTable() {
        return timeTable;
    }

    public void setTimeTable(DutyCardReader.TimeTable timeTable) {
        this.timeTable = timeTable;
    }

    private boolean isPassing = false;

    public boolean isPassing() {
        return isPassing;
    }

    public void setPassing(boolean isPassing) {
        this.isPassing = isPassing;
    }

    // 2文字を3文字にする　例：上野→上  野
    public String formatStationName(String name) {
        if (name == null) return null;
        name = name.replace(" ", "");
        if (name.length() == 2) {
            return name.charAt(0) + "  " + name.charAt(1);
        }
        return name;
    }

    // 3文字を2文字にする　例：上  野→上野
    public String unformatStationName(String formattedName) {
        if (formattedName == null) return null;
        return formattedName.replace("  ", "");
    }
    
    public String toZenkaku(String s) {
        if(s == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            // 半角英数字
            if (c >= 0x21 && c <= 0x7E) {
                sb.append((char)(c + 0xFEE0));  // 全角へ
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
