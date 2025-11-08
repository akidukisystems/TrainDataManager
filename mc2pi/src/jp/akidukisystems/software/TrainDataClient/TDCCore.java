package jp.akidukisystems.software.TrainDataClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.json.JSONObject;

import jp.akidukisystems.software.utilty.NetworkManager;

public class TDCCore 
{
    private static final int PORT = 34575;
    public static final int DOOR_CLOSE = 0;
    public static final int DOOR_RIGHT = 1;
    public static final int DOOR_LEFT = 2;
    public static final int DOOR_BOTH = 3;

    private static final Color COLOR_DEFAULT = Color.WHITE;
    private static final Color COLOR_ACTIVE = Color.YELLOW;
    private static final Color COLOR_ALERT = Color.RED;

    private static final String NA = "N/A";

    private static NetworkManager networkManager = null;    

    String distanceSetText = "0";

    String buttonCommand = null;
    int buttonDo = -1;

    int signal_0;
    int signal_1;
    
    Timer epButtonTimer;

    Timer blinkTimer;
    Timer refreshTimer;

    JLabel speedLabel;
    JLabel bcLabel;
    JLabel mrLabel;
    JLabel distanceLabel;
    JLabel notchPosLabel;

    JLabel infoTrainLabel;
    JLabel infoAtsLabel;
    JLabel infoTascLabel;
    JLabel infoTrainExLabel;

    JLabel trainNumberLabel;

    JLabel formationLabel;

    JButton doorOpenLButton;
    JButton doorOpenRButton;
    JButton doorCloseButton;
    JButton doorReOpenButton;

    JButton reverserSetFButton;
    JButton reverserSetNButton;
    JButton reverserSetBButton;

    JButton distanceSetUPButton;
    JButton dictanceSetDOWNButton;

    JButton epButton;

    JButton ActionButton;

    private static TrainNumber tn;
    private static TrainControl tc;

    public static void main(String[] args) 
    {
        TDCCore clientObject = new TDCCore();
        
        tn = new TrainNumber();
        networkManager = new NetworkManager();
        networkManager.clientInit("localhost", PORT);
        tc = new TrainControl();
        tc.boolTrainStatInit(128);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            if (networkManager != null) {
                networkManager.sendString("{\"type\":\"kill\"}");
                networkManager.clientClose();
            }
        }));

        try {
            clientObject.running();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // EDTいじいじするのでEdtでよい　Editではない

    private static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    private void resetBlinkState() {
        blinkTimer.stop();
        resetDoorButtonColors();
        buttonCommand = null;
        buttonDo = -1;
    }

    private void resetDoorButtonColors() {
        ActionButton.setBackground(COLOR_DEFAULT);
        doorOpenLButton.setBackground(COLOR_DEFAULT);
        doorOpenRButton.setBackground(COLOR_DEFAULT);
        doorCloseButton.setBackground(COLOR_DEFAULT);
        doorReOpenButton.setBackground(COLOR_DEFAULT);
    }

    private void toggleBlink(JButton source, String command, int value) {
        if (command.equals(buttonCommand) && buttonDo == value) {
            resetBlinkState();
        } else {
            // 新しいボタンを押したとき
            buttonCommand = command;
            buttonDo = value;
            blinkTimer.start();
            resetDoorButtonColors();
            source.setBackground(COLOR_ACTIVE);
        }
    }

    private void createNumberButtons(JPanel parent, int startX, int startY, int size, ActionListener listener) {
        for (int i = 0; i <= 9; i++) {
            JButton btn = new JButton(String.valueOf(i));
            int row = (i < 5) ? 0 : 1;
            int col = (i < 5) ? i : i - 5;
            btn.setBounds(startX + col * size, startY + row * size, size, size);
            btn.addActionListener(listener);
            parent.add(btn);
        }
    }

    private JLabel createLabel(String text, int x, int y, int w, int h) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 20));
        label.setForeground(Color.BLACK);
        label.setBounds(x, y, w, h);
        return label;
    }

    private JFrame createFrame(String title, int w, int h, JPanel content) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(w, h);
        frame.getContentPane().add(content, BorderLayout.CENTER);
        return frame;
    }

    public void setupUI() 
    {
        // フレーム（ウィンドウ）を作成
        JPanel p = new JPanel();
        p.setLayout(null);

        JPanel q = new JPanel();
        q.setLayout(null);

        JPanel r = new JPanel();
        r.setLayout(null);

        JPanel s = new JPanel();
        s.setLayout(null);

        // kilo

            JFrame distanceResetFrame = createFrame("キロ程リセット", 640, 480, q);

            createNumberButtons(q, 0, 0, 50, e -> distanceSetText += ((JButton)e.getSource()).getText());
            
            JButton setDistancePeriodButton = new JButton(".");
            setDistancePeriodButton.setBounds(250, 50, 50, 50);
            JButton setDistanceButton = new JButton("設定");
            setDistanceButton.setBounds(300, 50, 100, 50);
            distanceSetUPButton = new JButton("上り");
            distanceSetUPButton.setBounds(0, 100, 100, 50);
            dictanceSetDOWNButton = new JButton("下り");
            dictanceSetDOWNButton.setBounds(100, 100, 100, 50);

            setDistancePeriodButton.addActionListener(keyword -> distanceSetText += ".");
            distanceSetUPButton.addActionListener(keyword -> networkManager.sendCommand("send", "moveTo", 0));
            dictanceSetDOWNButton.addActionListener(keyword -> networkManager.sendCommand("send", "moveTo", 1));

            setDistanceButton.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    try 
                    {
                        float newDistance = Float.parseFloat(distanceSetText);
                       networkManager.sendCommand("send", "move", newDistance *1000f);
                    }
                    catch (NumberFormatException ef) 
                    {
                        System.out.println(ef);
                    }
                    distanceSetText = "0";
                    distanceResetFrame.setVisible(false);
                }
            });

        // bg

            JFrame epFrame = createFrame("防護無線", 480, 300, r);

            epButton = new JButton("発報");
            epButton.setBounds(140, 75, 200, 150);
            epButton.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));

            epButtonTimer = new Timer(200, new ActionListener() 
            {
                private boolean on = true;
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    r.setBackground(on ? COLOR_ALERT : COLOR_DEFAULT);
                    on = !on;
                } 
            });

            epButton.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    if (!tc.isRaisingEP()) 
                    {
                        epButtonTimer.start();
                        epButton.setText("復位");
                    } 
                    else 
                    {
                        epButtonTimer.stop();
                        r.setBackground(COLOR_DEFAULT);
                        epButton.setText("発報");
                    }
                    tc.setRaisingEP(!tc.isRaisingEP());
                }
            });

        // trainnum

            JFrame trainNumSetFrame = createFrame("列車番号", 640, 480, s);

            createNumberButtons(s, 0, 0, 50, e -> tn.number += ((JButton)e.getSource()).getText());

            JTextField textTrainStr = new JTextField("");
            textTrainStr.setBounds(250, 50, 100, 50);

            JButton setTrainNumButton = new JButton("設定");
            setTrainNumButton.setBounds(300, 50, 100, 50);

            setTrainNumButton.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    tn.alphabet = textTrainStr.getText();
                    tn.half = tn.number + tn.alphabet;
                    trainNumSetFrame.setVisible(false);
                }
            });

        // main

            JFrame frame = new JFrame("NetworkCatcher");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(480, 640);

            JButton teButton = new JButton("TE装置");
            teButton.setBounds(0, 0, 100, 50);

            doorOpenLButton = new JButton("左ドア開");
            doorOpenLButton.setBounds(0, 50, 100, 50);

            doorOpenRButton = new JButton("右ドア開");
            doorOpenRButton.setBounds(100, 50, 100, 50);

            doorCloseButton = new JButton("ドア閉");
            doorCloseButton.setBounds(0, 100, 100, 50);

            doorReOpenButton = new JButton("再開扉");
            doorReOpenButton.setBounds(100, 100, 100, 50);

            ActionButton = new JButton("設定");
            ActionButton.setBounds(200, 50, 200, 100);

            JButton showResetDistanceWindowButon = new JButton("キロ程リセット");
            showResetDistanceWindowButon.setBounds(0, 150, 150, 50);

            JButton showSetTrainNumWindowButton = new JButton("列車番号");
            showSetTrainNumWindowButton.setBounds(150, 150, 150, 50);

            JButton allResetButon = new JButton("リセット");
            allResetButon.setBounds(0, 500, 100, 50);

            reverserSetFButton = new JButton("前");
            reverserSetFButton.setBounds(300, 150, 50, 50);
            reverserSetNButton = new JButton("中");
            reverserSetNButton.setBounds(350, 150, 50, 50);
            reverserSetBButton = new JButton("後");
            reverserSetBButton.setBounds(400, 150, 50, 50);

            speedLabel = createLabel(NA, 0, 200, 100, 20);
            bcLabel = createLabel(NA, 100, 200, 75, 20);
            mrLabel = createLabel(NA, 175, 200, 75, 20);
            distanceLabel = createLabel(NA, 250, 200, 100, 20);
            notchPosLabel = createLabel(NA, 350, 200, 50, 20);
            infoTrainLabel = createLabel("", 0, 250, 600, 20);
            infoAtsLabel = createLabel("", 0, 300, 600, 20);
            infoTascLabel = createLabel("", 0, 350, 600, 20);
            infoTrainExLabel = createLabel("", 0, 400, 600, 20);
            trainNumberLabel = createLabel("", 0, 450, 200, 20);
            formationLabel = createLabel("", 200, 450, 200, 20);

            teButton.addActionListener(new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    tc.setTE(true);
                }
            });

            doorOpenLButton.addActionListener(e -> toggleBlink(doorOpenLButton, "door", DOOR_LEFT));
            doorOpenRButton.addActionListener(e -> toggleBlink(doorOpenRButton, "door", DOOR_RIGHT));
            doorCloseButton.addActionListener(e -> toggleBlink(doorCloseButton, "door", DOOR_CLOSE));

            doorReOpenButton.addActionListener(e -> {
                resetBlinkState();
                if (tc.getPrevDoor() != -1)
                    networkManager.sendCommand("send", "door", tc.getPrevDoor());
            });

            showResetDistanceWindowButon.addActionListener(keyword -> distanceResetFrame.setVisible(true));
            showSetTrainNumWindowButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    trainNumSetFrame.setVisible(true);
                    tn.alphabet = "";
                    tn.number = "";
                    tn.half = "";
                    tn.full = "";
                }
            });

            ActionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (buttonCommand != null) {
                        if ("door".equals(buttonCommand)) {
                            if (buttonDo == 0) tc.setPrevDoor(tc.getDoor());
                            networkManager.sendCommand("send", buttonCommand, buttonDo);
                        }
                        resetBlinkState();
                    }
                }
            });

            allResetButon.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    reset();
                }
            });

            reverserSetFButton.addActionListener(keyword ->networkManager.sendCommand("send", "reverser", 0));
            reverserSetNButton.addActionListener(keyword ->networkManager.sendCommand("send", "reverser", 1));
            reverserSetBButton.addActionListener(keyword ->networkManager.sendCommand("send", "reverser", 2));

            blinkTimer = new Timer(500, new ActionListener()
            {
                private boolean on = true;

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    switch (buttonCommand)
                    {
                        case "door":
                            ActionButton.setBackground(on ? COLOR_ACTIVE : COLOR_DEFAULT);
                            break;
                    
                        default:
                            break;
                    }
                    on = !on;
                }
            });



        p.add(teButton);
        p.add(doorOpenLButton);
        p.add(doorOpenRButton);
        p.add(doorCloseButton);
        p.add(doorReOpenButton);
        p.add(showResetDistanceWindowButon);
        p.add(ActionButton);
        p.add(allResetButon);
        p.add(showSetTrainNumWindowButton);

        p.add(speedLabel);
        p.add(bcLabel);
        p.add(mrLabel);
        p.add(distanceLabel);
        p.add(notchPosLabel);

        p.add(infoTrainLabel);
        p.add(infoAtsLabel);
        p.add(infoTascLabel);
        p.add(infoTrainExLabel);
        p.add(trainNumberLabel);

        p.add(formationLabel);

        p.add(reverserSetFButton);
        p.add(reverserSetNButton);
        p.add(reverserSetBButton);

        q.add(setDistancePeriodButton);
        q.add(setDistanceButton);
        q.add(distanceSetUPButton);
        q.add(dictanceSetDOWNButton);

        r.add(epButton);

        s.add(setTrainNumButton);
        s.add(textTrainStr);

        ActionButton.setBackground(COLOR_DEFAULT);
        doorOpenLButton.setBackground(COLOR_DEFAULT);
        doorOpenRButton.setBackground(COLOR_DEFAULT);
        doorCloseButton.setBackground(COLOR_DEFAULT);

        frame.getContentPane().add(p, BorderLayout.CENTER);
        distanceResetFrame.getContentPane().add(q, BorderLayout.CENTER);
        epFrame.getContentPane().add(r, BorderLayout.CENTER);
        trainNumSetFrame.getContentPane().add(s, BorderLayout.CENTER);


        reverserSetFButton.setEnabled(false);
        reverserSetNButton.setEnabled(false);
        reverserSetBButton.setEnabled(false);

        epButton.setBackground(COLOR_DEFAULT);

        // 表示
        frame.setVisible(true);
        distanceResetFrame.setVisible(false);
        epFrame.setVisible(true);
        trainNumSetFrame.setVisible(false);

        refreshTimer = new Timer(250, keyword -> 
        {
            updateTrainState();
            updateProtectService();
            updateDisplay();
        });
    }

    private void updateDisplay()
    {
        speedLabel.setText(String.format("%dkm/h", (int) tc.getSpeed()));
        bcLabel.setText(String.format("%dkpa", tc.getBc()));
        mrLabel.setText(String.format("%dkpa", tc.getMr()));
        distanceLabel.setText(String.format("%.1fkm", tc.getMove() / 1000f));
        formationLabel.setText(String.format("%d両編成", tc.getCars()));

        StringBuilder trainStat = new StringBuilder("状態:");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_PARKING)) trainStat.append("駐車　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_CONSTANT_SPEED)) trainStat.append("定速　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_DS_BRAKE)) trainStat.append("保B　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_SNOW_BRAKE)) trainStat.append("雪B　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EMERG_SHORT)) trainStat.append("非短　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_THREE_PHASE)) trainStat.append("三相　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EB)) trainStat.append("非常　");
        infoTrainLabel.setText(trainStat.toString());

        StringBuilder trainATS = new StringBuilder("ATS:");
        if(tc.getboolTrainStat(TrainControl.ATS_OPERATING)) trainATS.append("ATS動作　");
        if(tc.getboolTrainStat(TrainControl.ATS_POWER)) trainATS.append("ATS電源　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_ERROR)) trainATS.append("故障　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_ACTIVE)) trainATS.append("ATS-P　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE)) trainATS.append("B解放　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING)) trainATS.append("B　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_NEAR_PATTERN)) trainATS.append("ﾊﾟﾀﾝ接近　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_POWER)) trainATS.append("P電源　");
        if(tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING_EB)) trainATS.append("非常　");
        infoAtsLabel.setText(trainATS.toString());

        StringBuilder trainTASC = new StringBuilder("TASC:");
        if(tc.getboolTrainStat(TrainControl.TASC_POWER)) trainTASC.append("電源　");
        if(tc.getboolTrainStat(TrainControl.TASC_PATTERN_ACTIVE)) trainTASC.append("ﾊﾟﾀﾝ　");
        if(tc.getboolTrainStat(TrainControl.TASC_BRAKE)) trainTASC.append("B　");
        if(tc.getboolTrainStat(TrainControl.TASC_OFF)) trainTASC.append("切　");
        if(tc.getboolTrainStat(TrainControl.TASC_ERROR)) trainTASC.append("故障　");
        infoTascLabel.setText(trainTASC.toString());

        StringBuilder trainStatEX = new StringBuilder("");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_DOOR_CLOSE)) trainStatEX.append("戸閉　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_EB)) trainStatEX.append("EB装置　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_STA)) trainStatEX.append("次駅接近　");
        infoTrainExLabel.setText(trainStatEX.toString());

        if (tn.half != null)
        {
            trainNumberLabel.setText("列車番号: "+ tn.half);
        }
        else
        {
            trainNumberLabel.setText("列車番号を設定してください");
        }
    }

    private void updateProtectService()
    {
        // ATS-P
        tc.refreshTrainStat();

        if (tc.getboolTrainStat(TrainControl.TRAINSTAT_EB)) networkManager.sendCommand("send", "notch", TrainControl.NOTCH_EB);
        if (tc.getboolTrainStat(TrainControl.TRAINSTAT_DS_BRAKE)) networkManager.sendCommand("send", "notch", TrainControl.NOTCH_EB);
    }

    private void updateTrainState()
    {
        // 走行時戸開禁止
        if (tc.isRunningTrain())
        {
            doorCloseButton.setEnabled(false);
            doorOpenLButton.setEnabled(false);
            doorOpenRButton.setEnabled(false);
            doorReOpenButton.setEnabled(false);
            tc.setPrevDoor(-1);
        }
        else
        {
            doorCloseButton.setEnabled(true);
            doorOpenLButton.setEnabled(true);
            doorOpenRButton.setEnabled(true);
            doorReOpenButton.setEnabled(true);
        }
        
        // ノッチ位置表示
        boolean canChangeReverser = false;
        if (tc.getNotch() == TrainControl.NOTCH_EB)
        {
            notchPosLabel.setText("EB");
            canChangeReverser = true;
        }
        else if (tc.getNotch() == TrainControl.NOTCH_N)
        {
            notchPosLabel.setText("N");
        }
        else if (tc.getNotch() > TrainControl.NOTCH_N)
        {
            notchPosLabel.setText(String.format("P%d", tc.getNotch()));
        }
        else
        {
            notchPosLabel.setText(String.format("B%d", tc.getNotch() *-1));
        }

        // キロ程演算方向
        if (tc.getMoveTo() == 0)
        {
            distanceSetUPButton.setEnabled(false);
            dictanceSetDOWNButton.setEnabled(true);
        }
        else
        {
            distanceSetUPButton.setEnabled(true);
            dictanceSetDOWNButton.setEnabled(false);
        }

        // レバーサ
        // ノッチがEB位置じゃないとレバーサいじれないよう変更
        if (canChangeReverser)
        {
            JButton[] reverserButtons = {reverserSetFButton, reverserSetNButton, reverserSetBButton};
            int reversers = tc.getReverser();
            for (int i = 0; i < 3; i++) reverserButtons[i].setEnabled(canChangeReverser && i != reversers);
        }
        else
        {
            reverserSetFButton.setEnabled(false);
            reverserSetNButton.setEnabled(false);
            reverserSetBButton.setEnabled(false);
        }
    }

    public void reset()
    {
        tc.resetTrain();
        distanceSetText = "0";
        buttonCommand = null;
        buttonDo = -1;
        signal_0 = 0;
        signal_1 = 0;
        tn.reset();
    }

    public void running() throws IOException
    {
        System.out.println("client starting on port " + PORT + "...");

        tc.refreshTimer();
        reset();

        try {
            SwingUtilities.invokeAndWait(this::setupUI);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }

        networkManager.startHeartbeat(
            3000,  // intervalMs
            7000,  // timeoutMs
            () -> {
                System.err.println("Peer dead. Exiting...");
                try { networkManager.clientClose(); } catch (Exception ignored) {}
                try { networkManager.serverClose(); } catch (Exception ignored) {}
                System.exit(0);
            }
        );

        new Thread(() ->
        {
            String fetchData = null;

            while(true)
            {
                fetchData = networkManager.clientReciveString();

                if(fetchData != null)
                {
                    System.out.println(fetchData);

                    try
                    {
                        JSONObject jsonObj = new JSONObject(fetchData);
                        switch (jsonObj.getString("type"))
                        {
                            case "send":
                                // 受信
                                tc.setPrevNotch(tc.getNotch());
                                tc.setId(jsonObj.getInt("id"));
                                tc.setSpeed(jsonObj.getFloat("speed"));
                                tc.setNotch(jsonObj.getInt("notch"));
                                tc.setDoor(jsonObj.getInt("door"));
                                tc.setBc(jsonObj.getInt("bc"));
                                tc.setMr(jsonObj.getInt("mr"));
                                tc.setMove(jsonObj.getFloat("move"));
                                tc.setMoveTo(jsonObj.getInt("moveTo"));
                                tc.setReverser(jsonObj.getInt("reverser"));
                                tc.setLimit(jsonObj.getInt("speedLimit"));
                                tc.setTASCEnable(jsonObj.getBoolean("isTASCEnable"));
                                tc.setTASCBraking(jsonObj.getBoolean("isTASCBraking"));
                                tc.setCars(jsonObj.getInt("formation"));

                                tc.doorOpen_Close();
                                tc.handleTEunlock();
                                tc.handleEBunlock();
                                tc.handleEB();
                                tc.handleRunningOpen();
                                tc.handleATSNW();
                                tc.handleArrivingStation();
                                // GUI更新
                                onEdt(() -> {
                                    if (!refreshTimer.isRunning()) {
                                        refreshTimer.start();
                                    }
                                });

                                break;

                            case "kill":
                                networkManager.clientClose();
                                System.exit(0);
                                break;

                            case "notRidingTrain":
                                // 列車に載ってない
                                onEdt(() -> 
                                {
                                    speedLabel.setText(NA);
                                    bcLabel.setText(NA);
                                    mrLabel.setText(NA);
                                    distanceLabel.setText(NA);
                                    notchPosLabel.setText(NA);
                                    reverserSetFButton.setEnabled(false);
                                    reverserSetNButton.setEnabled(false);
                                    reverserSetBButton.setEnabled(false);
                                    infoTrainLabel.setText("");
                                    infoAtsLabel.setText("");
                                    infoTascLabel.setText("");
                                    infoTrainExLabel.setText("");
                                });

                                if (refreshTimer.isRunning()) refreshTimer.stop();

                                break;

                            case "beacon":
                                signal_0 = jsonObj.getInt("signal_0");
                                signal_1 = jsonObj.getInt("signal_1");

                                switch (signal_0)
                                {
                                    case 1:
                                        // TIMS駅情報更新
                                        break;

                                    case 2: 
                                        // 次駅接近報知
                                        // signal_1 = 1...セット
                                        // signal_1 = 2...リセット
                                        tc.setArraivingStation(signal_1);
                                        break;

                                    case 3:
                                        break;
                                
                                    default:
                                        break;
                                }
                                break;

                            case "start":
                                break;
                            
                            default:
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("JSON parse error: " + e.getMessage());
                    }
                }
            }
        }).start();
    }
}