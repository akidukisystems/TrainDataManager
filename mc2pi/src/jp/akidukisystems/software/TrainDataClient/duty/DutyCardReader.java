package jp.akidukisystems.software.TrainDataClient.duty;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * 仕業カード（LINE/STATION/...）を読み込んで ID→名称を解決するユーティリティ。
 * - STOP PATTERN: passing(1=通過) と memo（パターン説明）に対応
 * - TIME TABLE: arrive/ depart/ enter(=arriveSpeedLimit)/
 * exit(=departSpeedLimit)/ track 取り込み
 * - trackId=-1 は「-」、1～9 は表がなくても番号をそのまま表示
 */
public class DutyCardReader {

    // ---------- モデル ----------
    public static class Line {
        public int id;
        public String name;
    }

    public static class Station {
        public int id;
        public String name;
        public int lineId;
        public double linePost;
    }

    public static class TrainType {
        public int id;
        public String nameJa;
        public String nameEn;
    }

    public static class Track {
        public int id;
        public String name;
    }

    public static class StopPatternEntry {
        public int stationId;
        public boolean passing; // true=通過
    }

    public static class StopPattern {
        public int id;
        public String memo; // パターン全体の説明（任意）
        public final List<StopPatternEntry> entries = new ArrayList<>();
    }

    public static class TimeTableEntry {
        public int stationId;
        public String arrive; // "12:34:56" / "レ" / null
        public String depart; // 同上
        public int enterLimit; // -1=なし（arriveSpeedLimit）
        public int exitLimit; // -1=なし（departSpeedLimit）
        public int trackId; // -1=未指定
    }

    public static class TimeTable {
        public int id;
        public final List<TimeTableEntry> entries = new ArrayList<>();
    }

    public static class TrainNumber {
        public int id;
        public Integer numberInt;
        public String numberStr;
        public int lineId;
        public int trainTypeId;
        public String memo;
        public int stopPatternId;
        public int timeTableId;
    }

    public static class DutyCardData {
        public final Map<Integer, Line> lines = new LinkedHashMap<>();
        public final Map<Integer, Station> stations = new LinkedHashMap<>();
        public final Map<Integer, TrainType> trainTypes = new LinkedHashMap<>();
        public final Map<Integer, Track> tracks = new LinkedHashMap<>();
        public final Map<Integer, StopPattern> stopPatterns = new LinkedHashMap<>();
        public final Map<Integer, TimeTable> timeTables = new LinkedHashMap<>();
        public final Map<Integer, TrainNumber> trainNumbers = new LinkedHashMap<>();
    }

    // ---------- セクション ----------
    private enum Section {
        NONE, LINE, STATION, TRAIN_TYPE, TRACK, STOP_PATTERN, TIME_TABLE, TRAIN_NUMBER
    }

    private static Section toSection(String line) {
        String s = norm(line);
        return switch (s) {
            case "LINE SECTION" -> Section.LINE;
            case "STATION SECTION" -> Section.STATION;
            case "TRAIN TYPE SECTION" -> Section.TRAIN_TYPE;
            case "TRACK SECTION", "TRUCK SECTION" -> Section.TRACK;
            case "STOP PATTERN SECTION" -> Section.STOP_PATTERN;
            case "TIME TABLE SECTION" -> Section.TIME_TABLE;
            case "TRAIN NUMBER SECTION" -> Section.TRAIN_NUMBER;
            default -> Section.NONE;
        };
    }

    // ---------- パース ----------
    public static DutyCardData parse(Path file, Charset cs) throws IOException {
        return parse(Files.readAllLines(file, cs));
    }

    public static DutyCardData parse(List<String> lines) {
        DutyCardData data = new DutyCardData();
        Section section = Section.NONE;
        String[] headers = null;
        boolean headerSeen = false;

        for (String raw : lines) {
            String l = norm(raw);
            if (l.isEmpty())
                continue;

            Section maybe = toSection(l);
            if (maybe != Section.NONE) {
                section = maybe;
                headers = null;
                headerSeen = false;
                continue;
            }
            if (section == Section.NONE)
                continue;

            if (!headerSeen) {
                headers = splitKeepEmpty(raw);
                headerSeen = true;
                continue;
            }

            String[] cols = splitKeepEmpty(raw);
            if (cols.length == 0)
                continue;

            switch (section) {
                case LINE -> {
                    int id = getInt(cols, idx(headers, "index"), -1);
                    String name = getStr(cols, idx(headers, "lineName"));
                    if (id >= 0 && name != null) {
                        Line x = new Line();
                        x.id = id;
                        x.name = name;
                        data.lines.put(id, x);
                    }
                }
                case STATION -> {
                    int id = getInt(cols, idx(headers, "index"), -1);
                    String name = getStr(cols, idx(headers, "stationName"));
                    int lineId = getInt(cols, idx(headers, "lineName"), -1);
                    double post = getDouble(cols, idx(headers, "linePost"), -1.0);
                    if (id >= 0 && name != null) {
                        Station s = new Station();
                        s.id = id;
                        s.name = name;
                        s.lineId = lineId;
                        s.linePost = post;
                        data.stations.put(id, s);
                    }
                }
                case TRAIN_TYPE -> {
                    int id = getInt(cols, idx(headers, "index"), -1);
                    String ja = getStr(cols, idx(headers, "trainType"));
                    String en = getStr(cols, idx(headers, "trainTypeEN"));
                    if (id >= 0) {
                        TrainType t = new TrainType();
                        t.id = id;
                        t.nameJa = ja;
                        t.nameEn = en;
                        data.trainTypes.put(id, t);
                    }
                }
                case TRACK -> {
                    int id = getInt(cols, idx(headers, "index"), -1);
                    String name = getStr(cols, idx(headers, "trackName"));
                    if (id >= 0 && name != null) {
                        Track t = new Track();
                        t.id = id;
                        t.name = name;
                        data.tracks.put(id, t);
                    }
                }
                case STOP_PATTERN -> {
                    // ヘッダ: index,stopStationCode,passing,memo,,,,
                    int id = getInt(cols, idx(headers, "index"), -1);
                    int st = getInt(cols, idx(headers, "stopStationCode"), -1);
                    if (id < 0 || st < 0)
                        break;

                    StopPattern sp = data.stopPatterns.computeIfAbsent(id, k -> {
                        StopPattern x = new StopPattern();
                        x.id = k;
                        return x;
                    });

                    // memo はパターン全体の説明。どの行に書いてあっても拾う
                    String memo = getStr(cols, idx(headers, "memo"));
                    if (memo != null && (sp.memo == null || sp.memo.isEmpty()))
                        sp.memo = memo;

                    StopPatternEntry e = new StopPatternEntry();
                    e.stationId = st;
                    e.passing = getInt(cols, idx(headers, "passing"), 0) == 1;
                    sp.entries.add(e);
                }
                case TIME_TABLE -> {
                    // ヘッダ:
                    // index,stopStationCode,arriveTime,departTime,arriveSpeedLimit,departSpeedLimit,track,
                    int id = getInt(cols, idx(headers, "index"), -1);
                    int st = getInt(cols, idx(headers, "stopStationCode"), -1);
                    if (id < 0 || st < 0)
                        break;

                    String arrH = headerAny(headers, "arriveTime", "arrivalTime", "arrTime");
                    String depH = headerAny(headers, "departTime", "depothTime", "departureTime", "depTime");
                    String enterH = headerAny(headers, "arriveSpeedLimit", "enterSpeedLimit", "inLimit");
                    String exitH = headerAny(headers, "departSpeedLimit", "exitSpeedLimit", "outLimit");
                    String trackH = headerAny(headers, "track", "truck");

                    TimeTable tt = data.timeTables.computeIfAbsent(id, k -> {
                        TimeTable x = new TimeTable();
                        x.id = k;
                        return x;
                    });
                    TimeTableEntry e = new TimeTableEntry();
                    e.stationId = st;
                    e.arrive = getStr(cols, idx(headers, arrH));
                    e.depart = getStr(cols, idx(headers, depH));
                    e.enterLimit = getInt(cols, idx(headers, enterH), -1);
                    e.exitLimit = getInt(cols, idx(headers, exitH), -1);
                    e.trackId = getInt(cols, idx(headers, trackH), -1);
                    tt.entries.add(e);
                }
                case TRAIN_NUMBER -> {
                    int id = getInt(cols, idx(headers, "index"), -1);
                    if (id < 0)
                        break;
                    TrainNumber tn = new TrainNumber();
                    tn.id = id;
                    tn.numberInt = parseNullableInt(getStr(cols, idx(headers, "trainNumber_int")));
                    tn.numberStr = getStr(cols, idx(headers, "trainNumber_str"));
                    tn.lineId = getInt(cols, idx(headers, "lineName"), -1);
                    tn.trainTypeId = getInt(cols, idx(headers, "trainType"), -1);
                    tn.memo = getStr(cols, idx(headers, "memo"));
                    tn.stopPatternId = getInt(cols, idx(headers, "stopPattern"), -1);
                    tn.timeTableId = getInt(cols, idx(headers, "timeTable"), -1);
                    data.trainNumbers.put(id, tn);
                }
                default -> {
                }
            }
        }
        return data;
    }

    // ---------- 名前解決 ----------
    public static String lineName(DutyCardData d, int id) {
        Line l = d.lines.get(id);
        return l != null ? l.name : "#" + id;
    }

    public static String stationName(DutyCardData d, int id) {
        Station s = d.stations.get(id);
        return s != null ? s.name : "#" + id;
    }

    public static String stationLineName(DutyCardData d, int id) {
        Station s = d.stations.get(id);
        return s != null ? lineName(d, s.lineId) : "?";
    }

    public static String trainTypeName(DutyCardData d, int id) {
        TrainType t = d.trainTypes.get(id);
        return t == null ? "#" + id : (t.nameJa != null ? t.nameJa : t.nameEn);
    }

    public static String trackName(DutyCardData d, int id) {
        if (id < 0)
            return "-";
        Track t = d.tracks.get(id);
        if (t != null && t.name != null && !t.name.isEmpty())
            return t.name;
        if (1 <= id && id <= 9)
            return String.valueOf(id);
        return "#" + id;
    }

    /** 停車パターン（人間可読） */
    public static List<String> resolveStopPatternPretty(DutyCardData d, int id) {
        StopPattern sp = d.stopPatterns.get(id);
        if (sp == null)
            return List.of("(stopPattern " + id + " not found)");
        List<String> out = new ArrayList<>();
        if (sp.memo != null && !sp.memo.isEmpty())
            out.add("〈" + sp.memo + "〉");
        int i = 1;
        for (StopPatternEntry e : sp.entries) {
            String mark = e.passing ? "｜通過" : "●停車";
            out.add(String.format("%02d) %s （%s） %s", i++,
                    stationName(d, e.stationId), stationLineName(d, e.stationId), mark));
        }
        return out;
    }

    /** 時刻表（人間可読） */
    public static List<String> resolveTimeTablePretty(DutyCardData d, int id) {
        TimeTable tt = d.timeTables.get(id);
        if (tt == null)
            return List.of("(timeTable " + id + " not found)");
        List<String> out = new ArrayList<>();
        int i = 1;
        for (TimeTableEntry e : tt.entries) {
            out.add(String.format(
                    "%02d) %-8s Arr:%-9s Dep:%-9s In:%3s  Out:%3s  Track:%s",
                    i++,
                    stationName(d, e.stationId),
                    valOrDash(e.arrive),
                    valOrDash(e.depart),
                    (e.enterLimit >= 0 ? String.valueOf(e.enterLimit) : "-"),
                    (e.exitLimit >= 0 ? String.valueOf(e.exitLimit) : "-"),
                    trackName(d, e.trackId)));
        }
        return out;
    }

    /** 列番（人間可読）…数値+文字の合成（例: 9999M）も対応 */
    public static String resolveTrainNumberPretty(DutyCardData d, int id) {
        TrainNumber tn = d.trainNumbers.get(id);
        if (tn == null)
            return "(trainNumber " + id + " not found)";
        String head = (tn.numberInt != null ? String.valueOf(tn.numberInt) : "");
        String tail = (tn.numberStr != null ? tn.numberStr : "");
        String num = (head + tail).isEmpty() ? "-" : (head + tail);
        return String.format("#%d 列番:%s 種別:%s 路線:%s 停車P:%d 時刻:%d %s",
                tn.id, num, trainTypeName(d, tn.trainTypeId), lineName(d, tn.lineId),
                tn.stopPatternId, tn.timeTableId, (tn.memo != null ? tn.memo : ""));
    }

    // ---------- ヘルパ ----------
    private static String norm(String s) {
        if (s == null)
            return "";
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF')
            s = s.substring(1); // BOM除去
        s = s.replaceAll(",+\\s*$", "").trim(); // 末尾カンマ群を除去
        return s;
    }

    private static String[] splitKeepEmpty(String line) {
        String[] a = line.split(",", -1);
        for (int i = 0; i < a.length; i++)
            a[i] = a[i].trim();
        return a;
    }

    private static int idx(String[] h, String n) {
        if (h == null)
            return -1;
        for (int i = 0; i < h.length; i++)
            if (n.equals(h[i]))
                return i;
        return -1;
    }

    private static String headerAny(String[] h, String... cands) {
        for (String n : cands)
            if (idx(h, n) >= 0)
                return n;
        return "____notfound____";
    }

    private static int getInt(String[] c, int i, int def) {
        if (i < 0 || i >= c.length)
            return def;
        String s = c[i].trim();
        if (s.isEmpty())
            return def;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static double getDouble(String[] c, int i, double def) {
        if (i < 0 || i >= c.length)
            return def;
        String s = c[i].trim();
        if (s.isEmpty())
            return def;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static String getStr(String[] c, int i) {
        if (i < 0 || i >= c.length)
            return null;
        String s = c[i].trim();
        return s.isEmpty() ? null : s;
    }

    private static Integer parseNullableInt(String s) {
        if (s == null || s.isEmpty())
            return null;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String valOrDash(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }

    // ===== 方向判定 =====
    public enum Direction {
        UP, DOWN, UNKNOWN
    }

    public static Direction detectDirection(DutyCardData d, TimeTable tt) {
        if (tt == null || tt.entries.size() < 2)
            return Direction.UNKNOWN;

        Integer sign = null; // +1:昇順(下り) / -1:降順(上り)
        for (int i = 1; i < tt.entries.size(); i++) {
            int a = tt.entries.get(i - 1).stationId;
            int b = tt.entries.get(i).stationId;
            if (a == b)
                continue; // 同一駅が続くケースはスキップ
            int diff = Integer.compare(b, a); // >0 なら昇順、<0 なら降順
            int s = (diff > 0) ? 1 : -1;
            if (sign == null) {
                sign = s; // 最初に非ゼロ差が出た方向を採用
            } else if (sign != s) {
                return Direction.UNKNOWN; // 途中で逆転したら不明
            }
        }
        if (sign == null)
            return Direction.UNKNOWN; // 全て同一IDなら判定不能
        return (sign > 0) ? Direction.DOWN : Direction.UP;
    }

    public static String directionLabel(Direction dir) {
        return switch (dir) {
            case DOWN -> "下り";
            case UP -> "上り";
            default -> "?";
        };
    }

    // ---------- デモ出力（任意） ----------
    public static void main(String[] args) throws Exception {
        Path path = args.length > 0 ? Paths.get(args[0]) : Paths.get("card.csv");
        Charset cs = (args.length > 1) ? Charset.forName(args[1]) : Charset.forName("UTF-8");

        DutyCardData d = parse(path, cs);

        System.out.println("=== TRAIN NUMBERS ===");
        for (int id : d.trainNumbers.keySet())
            System.out.println(resolveTrainNumberPretty(d, id));

        System.out.println("\n=== STOP PATTERNS (resolved) ===");
        for (int id : d.stopPatterns.keySet()) {
            System.out.println("[StopPattern " + id + "]");
            resolveStopPatternPretty(d, id).forEach(System.out::println);
        }

        System.out.println("\n=== TIME TABLES (resolved) ===");
        for (int id : d.timeTables.keySet()) {
            System.out.println("[TimeTable " + id + "]");
            resolveTimeTablePretty(d, id).forEach(System.out::println);
        }

        while(true) {
            Thread.sleep(1000);
        }
    }
}
