package jp.akidukisystems.software.TrainDataClient.duty;

public class TimsToolkit
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

    private DutyCardReader.DcrLine line;

    public DutyCardReader.DcrLine getLine() {
        return line;
    }

    public void setLine(DutyCardReader.DcrLine line) {
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

    public String formatString(String input) {
        if (input == null) {
            return "";
        }

        int length = input.length();

        switch (length) {
            case 1:
                // 1文字なら前に半角スペース3つ
                return "   " + input;
            case 2:
                // 2文字なら前に半角スペース1つ + 文字の間に半角スペース2つ
                return " " + input.charAt(0) + "  " + input.charAt(1);
            case 3:
                // 3文字なら前に半角スペース1つ
                return " " + input;
            case 4:
                // 4文字なら何もしない
                return input;
            default:
                // それ以外はそのまま
                return input;
        }
    }

    public String formatSpeedLimit(int p1)
    {
        if(p1 == -1)
        {
            return "";
        }

        return Integer.toString(p1);
    }

    public String[] splitTime(String p1)
    {
        if(p1 == null)
            return null;
        String[] parts = p1.split(":");
        return parts;
    }

    public String formattingHourTime(String p1)
    {
        if(p1.length() == 1)
        {
            return "  "+ p1;
        }

        return p1;
    }
}
