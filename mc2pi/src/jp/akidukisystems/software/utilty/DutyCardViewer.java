package jp.akidukisystems.software.utilty;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class DutyCardViewer extends JFrame {
    private DutyCardData data;
    private Path loadedPath;
    private Charset loadedCharset;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Path path = (args.length > 0) ? Paths.get(args[0]) : Paths.get("card.csv");
                Charset cs = (args.length > 1) ? Charset.forName(args[1]) : Charset.forName("UTF-8");
                DutyCardData d = DutyCardReader.parse(path, cs);

                DutyCardViewer v = new DutyCardViewer(d, path, cs);
                v.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "読み込みエラー: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                // 空データで起動
                DutyCardViewer v = new DutyCardViewer(new DutyCardData(), null, Charset.forName("UTF-8"));
                v.setVisible(true);
            }
        });
    }

    public DutyCardViewer(DutyCardData data, Path path, Charset cs) {
        super("仕業カードビューア");
        this.data = data;
        this.loadedPath = path;
        this.loadedCharset = cs;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationByPlatform(true);

        setJMenuBar(makeMenuBar());
        setContentPane(makeTabs());
    }

    private JMenuBar makeMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("ファイル");
        file.add(new AbstractAction("開く...") {
            @Override public void actionPerformed(ActionEvent e) {
                chooseAndLoad();
            }
        });
        file.add(new AbstractAction("再読込") {
            @Override public void actionPerformed(ActionEvent e) {
                reload();
            }
        });
        file.addSeparator();
        file.add(new AbstractAction("終了") {
            @Override public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        mb.add(file);
        return mb;
    }

    private void chooseAndLoad() {
        JFileChooser ch = new JFileChooser();
        if (loadedPath != null) ch.setSelectedFile(loadedPath.toFile());
        int r = ch.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();

        String cs = (String) JOptionPane.showInputDialog(
                this, "文字コードを指定してください", "文字コード",
                JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"UTF-8", "Windows-31J", "Shift_JIS"}, "UTF-8");
        if (cs == null || cs.isEmpty()) cs = "UTF-8";

        try {
            DutyCardData d = DutyCardReader.parse(f.toPath(), Charset.forName(cs));
            this.data = d;
            this.loadedPath = f.toPath();
            this.loadedCharset = Charset.forName(cs);
            setContentPane(makeTabs());
            validate();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "読み込み失敗: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reload() {
        if (loadedPath == null) return;
        try {
            this.data = DutyCardReader.parse(loadedPath, loadedCharset);
            setContentPane(makeTabs());
            validate();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "再読込失敗: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent makeTabs() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Lines", wrap(makeLinesTable()));
        tabs.addTab("Stations", wrap(makeStationsTable()));
        tabs.addTab("Train Types", wrap(makeTrainTypesTable()));
        tabs.addTab("Tracks", wrap(makeTracksTable()));
        tabs.addTab("Stop Patterns", makeStopPatternPanel());
        tabs.addTab("Time Tables", makeTimeTablePanel());
        tabs.addTab("Train Numbers", makeTrainNumberPanel());

        return tabs;
    }

    private static JComponent wrap(JComponent c) {
        return new JScrollPane(c);
    }

    // ====== Lines ======
    private JTable makeLinesTable() {
        List<DcrLine> list = new ArrayList<>(data.lines.values());
        list.sort(Comparator.comparingInt(l -> l.id));
        return new JTable(new AbstractTableModel() {
            @Override public int getRowCount() { return list.size(); }
            @Override public int getColumnCount() { return 2; }
            @Override public String getColumnName(int c) { return new String[]{"ID","LineName"}[c]; }
            @Override public Object getValueAt(int r, int c) {
                DcrLine x = list.get(r);
                return switch (c) { case 0 -> x.id; case 1 -> x.name; default -> ""; };
            }
        });
    }

    // ====== Stations ======
    private JTable makeStationsTable() {
        List<Station> list = new ArrayList<>(data.stations.values());
        list.sort(Comparator.comparingInt(s -> s.id));
        return new JTable(new AbstractTableModel() {
            @Override public int getRowCount() { return list.size(); }
            @Override public int getColumnCount() { return 5; }
            @Override public String getColumnName(int c) {
                return new String[]{"ID","StationName","LineId","LineName","kp"}[c];
            }
            @Override public Object getValueAt(int r, int c) {
                Station s = list.get(r);
                return switch (c) {
                    case 0 -> s.id;
                    case 1 -> s.name;
                    case 2 -> s.lineId;
                    case 3 -> DutyCardReader.lineName(data, s.lineId);
                    case 4 -> (s.linePost >= 0 ? String.format("%.1f", s.linePost) : "");
                    default -> "";
                };
            }
        });
    }

    // ====== Train Types ======
    private JTable makeTrainTypesTable() {
        List<TrainType> list = new ArrayList<>(data.trainTypes.values());
        list.sort(Comparator.comparingInt(t -> t.id));
        return new JTable(new AbstractTableModel() {
            @Override public int getRowCount() { return list.size(); }
            @Override public int getColumnCount() { return 3; }
            @Override public String getColumnName(int c) { return new String[]{"ID","JA","EN"}[c]; }
            @Override public Object getValueAt(int r, int c) {
                TrainType t = list.get(r);
                return switch (c) { case 0 -> t.id; case 1 -> t.nameJa; case 2 -> t.nameEn; default -> ""; };
            }
        });
    }

    // ====== Tracks ======
    private JTable makeTracksTable() {
        List<Track> list = new ArrayList<>(data.tracks.values());
        list.sort(Comparator.comparingInt(t -> t.id));
        return new JTable(new AbstractTableModel() {
            @Override public int getRowCount() { return list.size(); }
            @Override public int getColumnCount() { return 2; }
            @Override public String getColumnName(int c) { return new String[]{"ID","TrackName"}[c]; }
            @Override public Object getValueAt(int r, int c) {
                Track t = list.get(r);
                return (c == 0) ? t.id : t.name;
            }
        });
    }

    // ====== Stop Patterns ======
    private JComponent makeStopPatternPanel() {
        JPanel root = new JPanel(new BorderLayout());

        // ★ Integer じゃなく StopPattern を入れる
        DefaultListModel<StopPattern> model = new DefaultListModel<>();
        ArrayList<StopPattern> sps = new ArrayList<>(data.stopPatterns.values());
        sps.sort(Comparator.comparingInt(sp -> sp.id));
        sps.forEach(model::addElement);

        // ★ JList<StopPattern>
        JList<StopPattern> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!model.isEmpty()) list.setSelectedIndex(0);

        // ★ “ID + メモ” を描くレンダラ
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                DutyCardReader.StopPattern sp = (DutyCardReader.StopPattern) value;
                String memo = (sp.memo == null || sp.memo.isBlank()) ? "" : "  " + sp.memo;
                lbl.setText(sp.id + memo); // 例: "23  準急(桜碧線)"
                return lbl;
            }
        });

        JTable table = new JTable();
        list.addListSelectionListener(e -> {
            StopPattern sel = list.getSelectedValue();
            if (sel != null) table.setModel(makeStopPatternTableModel(sel.id)); // ← id を渡す
        });
        if (!model.isEmpty()) table.setModel(makeStopPatternTableModel(model.get(0).id));

        root.add(new JScrollPane(list), BorderLayout.WEST);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        list.setFixedCellWidth(220); // ちょい広げると見やすい

        return root;
    }


    private AbstractTableModel makeStopPatternTableModel(int spId) {
        StopPattern sp = data.stopPatterns.get(spId);
        List<StopPatternEntry> entries = (sp != null) ? sp.entries : List.of();

        return new AbstractTableModel() {
            @Override public int getRowCount() { return entries.size(); }
            @Override public int getColumnCount() { return 5; }
            @Override public String getColumnName(int c) {
                return new String[]{"#", "StationId", "StationName", "LineName", "Passing"}[c];
            }
            @Override public Object getValueAt(int r, int c) {
                StopPatternEntry e = entries.get(r);
                return switch (c) {
                    case 0 -> (r + 1);
                    case 1 -> e.stationId;
                    case 2 -> DutyCardReader.stationName(data, e.stationId);
                    case 3 -> DutyCardReader.stationLineName(data, e.stationId);
                    case 4 -> (e.passing ? "通過" : "");
                    default -> "";
                };
            }
        };
    }

    // ====== Time Tables ======
    private JComponent makeTimeTablePanel() {
        JPanel root = new JPanel(new BorderLayout());

        DefaultListModel<TimeTable> model = new DefaultListModel<>();
        ArrayList<TimeTable> tts = new ArrayList<>(data.timeTables.values());
        tts.sort(Comparator.comparingInt(tt -> tt.id));
        tts.forEach(model::addElement);

        JList<TimeTable> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!model.isEmpty()) list.setSelectedIndex(0);

        // ★ “ID (N駅)” のレンダラ
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                DutyCardReader.TimeTable tt = (DutyCardReader.TimeTable) value;
                DutyCardReader.Direction dir = DutyCardReader.detectDirection(data, tt);
                String dirTxt = DutyCardReader.directionLabel(dir);
                lbl.setText(tt.id + " (" + tt.entries.size() + "駅) [" + dirTxt + "]");
                return lbl;
            }
        });

        JTable table = new JTable();
        list.addListSelectionListener(e -> {
            TimeTable sel = list.getSelectedValue();
            if (sel != null) table.setModel(makeTimeTableModel(sel.id));
        });
        if (!model.isEmpty()) table.setModel(makeTimeTableModel(model.get(0).id));

        root.add(new JScrollPane(list), BorderLayout.WEST);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        list.setFixedCellWidth(200);

        return root;
    }

    private AbstractTableModel makeTimeTableModel(int ttId) {
        TimeTable tt = data.timeTables.get(ttId);
        List<TimeTableEntry> entries = (tt != null) ? tt.entries : List.of();

        return new AbstractTableModel() {
            @Override public int getRowCount() { return entries.size(); }
            @Override public int getColumnCount() { return 10; } // 9→10
            @Override public String getColumnName(int c) {
                return new String[]{
                    "#","StationId","StationName","Arrive","Depart",
                    "EnterLimit","ExitLimit","TrackId","TrackName","Dir"  // ← 追加
                }[c];
            }
            @Override public Object getValueAt(int r, int c) {
                TimeTableEntry e = entries.get(r);
                return switch (c) {
                    case 0 -> (r + 1);
                    case 1 -> e.stationId;
                    case 2 -> DutyCardReader.stationName(data, e.stationId);
                    case 3 -> valOrDash(e.arrive);
                    case 4 -> valOrDash(e.depart);
                    case 5 -> (e.enterLimit >= 0 ? e.enterLimit : "-");
                    case 6 -> (e.exitLimit  >= 0 ? e.exitLimit  : "-");
                    case 7 -> e.trackId;
                    case 8 -> DutyCardReader.trackName(data, e.trackId);
                    case 9 -> {
                        DutyCardReader.TimeTable tt = data.timeTables.get(ttId);
                        DutyCardReader.Direction dir = DutyCardReader.detectDirection(data, tt);
                        yield DutyCardReader.directionLabel(dir);
                    }
                    default -> "";
                };
            }
        };
    }

    // ====== Train Numbers ======
    private JComponent makeTrainNumberPanel() {
        JPanel root = new JPanel(new BorderLayout());

        java.util.List<TrainNumber> list = new ArrayList<>(data.trainNumbers.values());
        list.sort(Comparator.comparingInt(t -> t.id));

        JTable table = new JTable(new AbstractTableModel() {
            @Override public int getRowCount() { return list.size(); }
            @Override public int getColumnCount() { return 10; }
            @Override public String getColumnName(int c) {
                return new String[]{
                        "ID","列番(合成)","trainNumber_int","trainNumber_str",
                        "LineId","LineName","TypeId","TypeName","StopPattern","TimeTable"
                }[c];
            }
            @Override public Object getValueAt(int r, int c) {
                TrainNumber tn = list.get(r);
                String composed = composeTrainNumber(tn.numberInt, tn.numberStr);
                return switch (c) {
                    case 0 -> tn.id;
                    case 1 -> composed;
                    case 2 -> tn.numberInt;
                    case 3 -> tn.numberStr;
                    case 4 -> tn.lineId;
                    case 5 -> DutyCardReader.lineName(data, tn.lineId);
                    case 6 -> tn.trainTypeId;
                    case 7 -> DutyCardReader.trainTypeName(duty(), tn.trainTypeId);
                    case 8 -> tn.stopPatternId;
                    case 9 -> tn.timeTableId;
                    default -> "";
                };
            }
        });

        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        return root;
    }

    private DutyCardData duty() { return data; }

    private static String composeTrainNumber(Integer numInt, String numStr) {
        // 数字 + 文字列の合成（例: 9999 + "M" -> "9999M"、nullは片方のみ）
        String a = (numInt != null) ? String.valueOf(numInt) : "";
        String b = (numStr != null) ? numStr : "";
        String s = (a + b);
        return s.isEmpty() ? "-" : s;
    }

    private static String valOrDash(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }
}
