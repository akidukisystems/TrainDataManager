package jp.akidukisystems.software.utilty;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader;

public class DutyCardRepository {

    private DutyCardReader.DutyCardData data;

    public static void main(String args[])
    {
        DutyCardRepository dcr = new DutyCardRepository();

        Path path = args.length > 0 ? Paths.get(args[0]) : Paths.get("card.csv");
        Charset cs = (args.length > 1) ? Charset.forName(args[1]) : Charset.forName("UTF-8");

        try {
            dcr.load(path, cs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (DutyCardReader.Station s : dcr.getStations()) {
            System.out.println(s.id + ": " + s.name);
        }

        for (DutyCardReader.DcrLine s : dcr.getLines()) {
            System.out.println(s.id + ": " + s.name);
        }

        for (DutyCardReader.StopPattern s : dcr.getStopPatterns()) {
            System.out.println(s.id + ": " + s.memo);
        }

        for (DutyCardReader.TimeTable s : dcr.getTimeTables()) {
            if (s != null) {
                for (DutyCardReader.TimeTableEntry e : s.entries) {
                    System.out.println("駅ID: " + e.stationId +
                                    " Arr:" + e.arrive +
                                    " Dep:" + e.depart +
                                    " Track:" + e.trackId);
                }
            }
        }

        for (DutyCardReader.TrainNumber s : dcr.getTrainNumbers()) {
            System.out.println("列番:"+ s.numberInt +""+ s.numberStr +" 種別:"+ dcr.getTrainType(s.trainTypeId).nameJa +" "+ dcr.getFirstStationByTrainNumber(s).name +"→"+ dcr.getLastStationByTrainNumber(s).name);
        }
    }

    public DutyCardReader.Station getFirstStationByTrainNumber(DutyCardReader.TrainNumber trainNumber) {
        if (trainNumber == null) return null;

        DutyCardReader.TimeTable tt = getTimeTable(trainNumber.timeTableId);
        if (tt == null || tt.entries.isEmpty()) return null;

        int firstStationId = tt.entries.get(0).stationId;
        return getStation(firstStationId);
    }

    public DutyCardReader.Station getLastStationByTrainNumber(DutyCardReader.TrainNumber trainNumber) {
        if (trainNumber == null) return null;

        DutyCardReader.TimeTable tt = getTimeTable(trainNumber.timeTableId);
        if (tt == null || tt.entries.isEmpty()) return null;

        int lastStationId = tt.entries.get(tt.entries.size() - 1).stationId;
        return getStation(lastStationId);
    }

    public DutyCardRepository() {
        this.data = new DutyCardReader.DutyCardData();
    }

    /** CSV から読んで全部セット */
    public void load(Path path, Charset cs) throws Exception {
        this.data = DutyCardReader.parse(path, cs);
    }

    /** 既存の DutyCardData をセット */
    public void setData(DutyCardReader.DutyCardData data) {
        this.data = data;
    }

    /** 生データを返す */
    public DutyCardReader.DutyCardData getRaw() {
        return data;
    }

    // ===== Line =====
    public DutyCardReader.DcrLine getLine(int id) {
        return data.lines.get(id);
    }

    public Collection<DutyCardReader.DcrLine> getLines() {
        return data.lines.values();
    }

    // ===== Station =====
    public DutyCardReader.Station getStation(int id) {
        return data.stations.get(id);
    }

    public Collection<DutyCardReader.Station> getStations() {
        return data.stations.values();
    }

    public List<DutyCardReader.Station> getStationsByLine(int lineId) {
        List<DutyCardReader.Station> s = new ArrayList<>();
        for (DutyCardReader.Station st : data.stations.values()) {
            if (st.lineId == lineId) s.add(st);
        }
        return s;
    }

    public DutyCardReader.Station getStationByName(String name) {
        if (name == null) return null;

        for (DutyCardReader.Station st : data.stations.values()) {
            if (name.equals(st.name)) {
                return st;
            }
        }

        return null; // 見つからないとき
    }

    public DutyCardReader.TimeTableEntry[] getSurroundingStations
    (
        double currentKilopost,
        DutyCardReader.DcrLine line,
        DutyCardReader.Direction direction,
        DutyCardReader.TimeTable timeTable
    ) {

        if (timeTable == null || timeTable.entries == null || timeTable.entries.isEmpty())
        {
            return new DutyCardReader.TimeTableEntry[]{null, null, null};
        }

        List<DutyCardReader.TimeTableEntry> entries = new ArrayList<>(timeTable.entries);

        DutyCardReader.TimeTableEntry prevStation = null;
        DutyCardReader.TimeTableEntry nextStation = null;
        DutyCardReader.TimeTableEntry nextNextStation = null;

        for (int i = 0; i < entries.size(); i++)
        {
            DutyCardReader.TimeTableEntry e = entries.get(i);
            DutyCardReader.Station station = getStation(e.stationId);

            double stationKilopost = station.linePost;

            if (station == null || station.lineId != line.id) continue;

            if
            (
                (direction == DutyCardReader.Direction.UP   && stationKilopost < currentKilopost) ||
                (direction == DutyCardReader.Direction.DOWN && stationKilopost > currentKilopost)
            ) {

                if(nextStation == null) nextStation = e;
            }
            
            // 現在地点と同じキロポストなら
            // その次の駅が次駅になる
            // その次の駅が無いなら（つまり、終点）
            // 仕方ないのでこの駅を次駅にする
            if (Math.round(stationKilopost * 100.0) / 100.0 == Math.round(currentKilopost * 100.0) / 100.0)
            {
                if((i +1) < entries.size())
                    nextStation = entries.get(i +1);
                else
                {
                    prevStation = e;
                    nextStation = null;
                    nextNextStation = null;

                    return new DutyCardReader.TimeTableEntry[]
                    {
                        prevStation,
                        nextStation,
                        nextNextStation
                    };
                }
            }
        }

        // 次の駅がわからなかったとき 境界駅の可能性があるので、とりあえず境界駅を探してみる
        if(nextStation == null)
        {
            double prevStationKilopost = -1.0;

            for (int i = 0; i < entries.size(); i++)
            {
                DutyCardReader.TimeTableEntry e = entries.get(i);
                DutyCardReader.Station station = getStation(e.stationId);

                double stationKilopost = station.linePost;

                if ((prevStationKilopost != -1.0) && (direction == DutyCardReader.Direction.UP))
                {
                    if(prevStationKilopost < stationKilopost)
                    {
                        nextStation = e;
                        break;
                    }
                }

                if ((prevStationKilopost != -1.0) && (direction == DutyCardReader.Direction.DOWN))
                {
                    if(prevStationKilopost > stationKilopost)
                    {
                        nextStation = e;
                        break;
                    }
                }

                prevStationKilopost = stationKilopost;
            }
        }

        // ひとまず次の駅をもってくる

        // この「次の駅」とやらが時刻表で何駅目か探す
        int index = -1;
        for (int i = 0; i < entries.size(); i++)
        {
            DutyCardReader.TimeTableEntry e = entries.get(i);
            if (e == nextStation)
            {
                index = i;
                break;
            }
        }

        if (index != -1)
        {
            // 前の駅！
            if (index - 1 >= 0)
            {
                prevStation = entries.get(index - 1);
            }

            // 次の次の駅！
            if (index + 1 < entries.size())
            {
                nextNextStation = entries.get(index + 1);
            }
        }

        return new DutyCardReader.TimeTableEntry[]
        {
            prevStation,
            nextStation,
            nextNextStation
        };
    }


    public DutyCardReader.TimeTableEntry getNextStation
    (
        double currentKilopost,
        DutyCardReader.DcrLine line,
        DutyCardReader.Direction direction,
        DutyCardReader.TimeTable timeTable
    ) {

        if (timeTable == null || timeTable.entries == null || timeTable.entries.isEmpty())
        {
            return null;
        }

        List<DutyCardReader.TimeTableEntry> entries = new ArrayList<>(timeTable.entries);

        DutyCardReader.TimeTableEntry nextStation = null;

        for (int i = 0; i < entries.size(); i++)
        {
            DutyCardReader.TimeTableEntry e = entries.get(i);
            DutyCardReader.Station station = getStation(e.stationId);

            double stationKilopost = station.linePost;

            if (station == null || station.lineId != line.id) continue;

            if
            (
                (direction == DutyCardReader.Direction.UP   && stationKilopost < currentKilopost) ||
                (direction == DutyCardReader.Direction.DOWN && stationKilopost > currentKilopost)
            ) {

                if(nextStation == null) nextStation = e;
            }
            
            // 現在地点と同じキロポストなら
            // その次の駅が次駅になる
            // その次の駅が無いなら（つまり、終点）
            // 
            if (Math.round(stationKilopost * 100.0) / 100.0 == Math.round(currentKilopost * 100.0) / 100.0)
            {
                if((i +1) < entries.size())
                    nextStation = entries.get(i +1);
                else
                {
                    return null;  
                }
                    
            }
        }

        // 次の駅がわからなかったとき 境界駅の可能性があるので、とりあえず境界駅を探してみる
        if(nextStation == null)
        {
            double prevStationKilopost = -1.0;

            for (int i = 0; i < entries.size(); i++)
            {
                DutyCardReader.TimeTableEntry e = entries.get(i);
                DutyCardReader.Station station = getStation(e.stationId);

                double stationKilopost = station.linePost;

                if ((prevStationKilopost != -1.0) && (direction == DutyCardReader.Direction.UP))
                {
                    if(prevStationKilopost < stationKilopost)
                    {
                        nextStation = e;
                        break;
                    }
                }

                if ((prevStationKilopost != -1.0) && (direction == DutyCardReader.Direction.DOWN))
                {
                    if(prevStationKilopost > stationKilopost)
                    {
                        nextStation = e;
                        break;
                    }
                }

                prevStationKilopost = stationKilopost;
            }
        }

        return nextStation;
    }


    public DutyCardReader.TimeTableEntry getNextStopStation
    (
        double currentKilopost,
        DutyCardReader.DcrLine line,
        DutyCardReader.Direction direction,
        DutyCardReader.TimeTable timeTable
    ) {

        if (timeTable == null || timeTable.entries == null || timeTable.entries.isEmpty())
        {
            return null;
        }

        List<DutyCardReader.TimeTableEntry> entries = new ArrayList<>(timeTable.entries);

        DutyCardReader.TimeTableEntry nextStation = null;

        for (int i = 0; i < entries.size(); i++)
        {
            DutyCardReader.TimeTableEntry e = entries.get(i);
            DutyCardReader.Station station = getStation(e.stationId);

            double stationKilopost = station.linePost;

            if (station == null || station.lineId != line.id) continue;

            if
            (
                (direction == DutyCardReader.Direction.UP   && stationKilopost < currentKilopost) ||
                (direction == DutyCardReader.Direction.DOWN && stationKilopost > currentKilopost)
            ) {

                if(nextStation == null) nextStation = e;
            }
            
            // 現在地点と同じキロポストなら
            // その次の駅が次駅になる
            // その次の駅が無いなら（つまり、終点）
            // 
            if (Math.round(stationKilopost * 100.0) / 100.0 == Math.round(currentKilopost * 100.0) / 100.0)
            {
                if((i +1) < entries.size())
                    nextStation = entries.get(i +1);
                else
                {
                    return null;  
                }
                    
            }
        }

        // 次の駅がわからなかったとき 境界駅の可能性があるので、とりあえず境界駅を探してみる
        if(nextStation == null)
        {
            double prevStationKilopost = -1.0;

            for (int i = 0; i < entries.size(); i++)
            {
                DutyCardReader.TimeTableEntry e = entries.get(i);
                DutyCardReader.Station station = getStation(e.stationId);

                double stationKilopost = station.linePost;

                if ((prevStationKilopost != -1.0) && (direction == DutyCardReader.Direction.UP))
                {
                    if(prevStationKilopost < stationKilopost)
                    {
                        nextStation = e;
                        break;
                    }
                }

                if ((prevStationKilopost != -1.0) && (direction == DutyCardReader.Direction.DOWN))
                {
                    if(prevStationKilopost > stationKilopost)
                    {
                        nextStation = e;
                        break;
                    }
                }

                prevStationKilopost = stationKilopost;
            }
        }

        // とりあえず次の駅はわかった
        // そすたら、外丸駅をさがす

        int index = -1;
        for (int i = 0; i < entries.size(); i++)
        {
            DutyCardReader.TimeTableEntry e = entries.get(i);
            if (e == nextStation)
            {
                index = i;
                break;
            }
        }

        for (int i = index; i < entries.size(); i++)
        {
            DutyCardReader.TimeTableEntry e = entries.get(i);
            if (e.arrive == null || !e.arrive.equals("レ"))
            {
                return e;
            }
        }

        return null;
    }

    // ===== TrainType =====
    public DutyCardReader.TrainType getTrainType(int id) {
        return data.trainTypes.get(id);
    }

    public Collection<DutyCardReader.TrainType> getTrainTypes() {
        return data.trainTypes.values();
    }

    // ===== Track =====
    public DutyCardReader.Track getTrack(int id) {
        return data.tracks.get(id);
    }

    public Collection<DutyCardReader.Track> getTracks() {
        return data.tracks.values();
    }

    // ===== StopPattern =====
    public DutyCardReader.StopPattern getStopPattern(int id) {
        return data.stopPatterns.get(id);
    }

    public Collection<DutyCardReader.StopPattern> getStopPatterns() {
        return data.stopPatterns.values();
    }

    public List<String> getStopPatternPretty(int id) {
        return DutyCardReader.resolveStopPatternPretty(data, id);
    }

    // ===== TimeTable =====
    public DutyCardReader.TimeTable getTimeTable(int id) {
        return data.timeTables.get(id);
    }

    public Collection<DutyCardReader.TimeTable> getTimeTables() {
        return data.timeTables.values();
    }

    public List<String> getTimeTablePretty(int id) {
        return DutyCardReader.resolveTimeTablePretty(data, id);
    }

    // ===== TrainNumber =====
    public DutyCardReader.TrainNumber getTrainNumber(int id) {
        return data.trainNumbers.get(id);
    }

    public Collection<DutyCardReader.TrainNumber> getTrainNumbers() {
        return data.trainNumbers.values();
    }

    public String getTrainNumberPretty(int id) {
        return DutyCardReader.resolveTrainNumberPretty(data, id);
    }

    // ===== Direction =====
    public DutyCardReader.Direction getDirectionOfTimeTable(int id) {
        DutyCardReader.TimeTable tt = getTimeTable(id);
        return DutyCardReader.detectDirection(data, tt);
    }
}
