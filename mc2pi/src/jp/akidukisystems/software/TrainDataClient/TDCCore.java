package jp.akidukisystems.software.TrainDataClient;

/* 処理系 */
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONObject;

/* AWT系 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/* Swing系 */
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/* JavaFX */
import javafx.application.Application;

/* 子とか孫とかいろいろ */
import jp.akidukisystems.software.TrainDataClient.GUI.NetworkIndicator;
import jp.akidukisystems.software.TrainDataClient.GUI.NetworkIndicator.TRTYPE;
import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.TimsSetup;
import jp.akidukisystems.software.TrainDataClient.Protector.ATSPController;
import jp.akidukisystems.software.TrainDataClient.duty.TimsToolkit;
import jp.akidukisystems.software.utilty.DutyCardRepository;
import jp.akidukisystems.software.utilty.NetworkManager;
import jp.akidukisystems.software.utilty.WrapLayout;
import jp.akidukisystems.software.utilty.Serial;

public class TDCCore 
{  
    public static final int DOOR_CLOSE = 0;
    public static final int DOOR_RIGHT = 1;
    public static final int DOOR_LEFT = 2;
    public static final int DOOR_BOTH = 3;

    // 色
    private static final Color COLOR_DEFAULT = Color.WHITE;
    private static final Color COLOR_ACTIVE = Color.YELLOW;
    private static final Color COLOR_ALERT = Color.RED;

    // ボタンサイズ定義
    private static final Dimension BUTTON_NORMAL = new Dimension(128, 64);
    private static final Dimension BUTTON_LARGE = new Dimension(192, 64);
    private static final Dimension BUTTON_MIDLARGE = new Dimension(128, 96);
    private static final Dimension BUTTON_SMALL = new Dimension(64, 64);
    // private static final Dimension BUTTON_BIG = new Dimension(128, 64);

    private static final String NA = "N/A"; 

    String address = null;
    int port = 34575;

    String distanceSetText = "0";

    String buttonCommand = null;
    int buttonDo = -1;

    int signal_0;
    int signal_1;
    
    Timer epButtonTimer;

    Timer blinkTimer;
    Timer refreshTimer;
    Timer timsRefreshTimer;

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
    JButton doorCloseButtonL;
    JButton doorCloseButtonR;
    JButton doorReOpenButton;

    JButton reverserSetFButton;
    JButton reverserSetNButton;
    JButton reverserSetBButton;

    JButton distanceSetUPButton;
    JButton dictanceSetDOWNButton;

    JButton epButton;

    JButton ActionButton;

    NetworkIndicator indicator;

    Serial serial;

    public NetworkManager networkManager = null;   
    public TrainNumber tn = null;   
    public TrainControl tc = null;   
    public DutyCardRepository dcr = null;
    public TimsToolkit om = null;
    public TimsUpdater timsUpdater = null;

    private ATSPController atsp = null;
    private ConfigManager cfg = null;
    
    public static void main(String[] args) 
    {
        TDCCore clientObject = new TDCCore();

        // System.setProperty("awt.useSystemAAFontSettings", "off");
        // System.setProperty("swing.aatext", "false");

        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

        clientObject.running();
    }

    // EDTいじいじするのでEdtでよい　Editではない

    private static void onEdt(Runnable r)
    {
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    

    private void resetBlinkState()
    {
        blinkTimer.stop();
        resetDoorButtonColors();
        buttonCommand = null;
        buttonDo = -1;
    }

    private void resetDoorButtonColors()
    {
        ActionButton.setBackground(COLOR_DEFAULT);
        doorOpenLButton.setBackground(COLOR_DEFAULT);
        doorOpenRButton.setBackground(COLOR_DEFAULT);
        doorCloseButtonL.setBackground(COLOR_DEFAULT);
        doorCloseButtonR.setBackground(COLOR_DEFAULT);
        doorReOpenButton.setBackground(COLOR_DEFAULT);
    }

    private void toggleBlink(JButton source, String command, int value)
    {
        if (command.equals(buttonCommand) && buttonDo == value)
        {
            resetBlinkState();
        }
        else
        {
            buttonCommand = command;
            buttonDo = value;
            blinkTimer.start();
            resetDoorButtonColors();
            source.setBackground(COLOR_ACTIVE);
        }
    }

    private void createNumberButtons(JPanel parent, int startX, int startY, int size, ActionListener listener)
    {
        for (int i = 0; i <= 9; i++)
        {
            JButton btn = new JButton(String.valueOf(i));
            int row = (i < 5) ? 0 : 1;
            int col = (i < 5) ? i : i - 5;
            btn.setBounds(startX + col * size, startY + row * size, size, size);
            btn.addActionListener(listener);
            parent.add(btn);
        }
    }

    private JLabel createLabel(String text)
    {
        JLabel label = new JLabel(text);
        label.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 18));
        label.setForeground(Color.BLACK);
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 10));
        return label;
    }

    private JFrame createFrame(String title, int w, int h, JPanel content)
    {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(w, h);
        frame.getContentPane().add(content, BorderLayout.CENTER);
        return frame;
    }

    private static void style(JButton b, Dimension size)
    {
        b.setPreferredSize(size);
        b.setMargin(new Insets(2, 10, 2, 10));
    }

    public void setupUI() 
    {
        JPanel root = new JPanel(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JPanel TEPanel       = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JPanel reverserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JPanel utilPanel     = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));

        JPanel doorMatrix = new JPanel(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(2, 2, 2, 2);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        FlowLayout tight = new FlowLayout(FlowLayout.LEFT, 12, 2);

        // 各行パネル
        JPanel meterPanel = new JPanel(tight); // 速度/圧/距離/ノッチ
        JPanel stateRow   = new JPanel(tight); // 状態:
        JPanel atsRow     = new JPanel(tight); // ATS:
        JPanel tascRow    = new JPanel(tight); // TASC:
        JPanel extraRow   = new JPanel(tight); // 情報2
        JPanel idRow      = new JPanel(tight); // 列車番号/編成

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
                    ef.printStackTrace();
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

        createNumberButtons(s, 0, 0, 50, e -> tn.setNumber(tn.getNumber() + ((JButton)e.getSource()).getText()));

        JTextField textTrainStr = new JTextField("");
        textTrainStr.setBounds(250, 50, 50, 50);

        JButton setTrainNumButton = new JButton("設定");
        setTrainNumButton.setBounds(300, 50, 100, 50);

        setTrainNumButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                tn.setAlphabet(textTrainStr.getText());
                tn.setHalf(tn.getNumber() + tn.getAlphabet());
                trainNumSetFrame.setVisible(false);
            }
        });

        // main

        JFrame frame = new JFrame("NetworkCatcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480, 640);

        JButton teButton = new JButton("TE装置");
        doorOpenLButton = new JButton("左ドア開");
        doorOpenRButton = new JButton("右ドア開");
        doorCloseButtonL = new JButton("ドア閉");
        doorCloseButtonR = new JButton("ドア閉");
        doorReOpenButton = new JButton("再開扉");
        ActionButton = new JButton("設定");

        JButton showResetDistanceWindowButon = new JButton("キロ程リセット");
        JButton showSetTrainNumWindowButton = new JButton("列車番号");
        JButton allResetButon = new JButton("リセット");
        JButton atspBrakeReleaseButton = new JButton("ATS-P緩解");

        reverserSetFButton = new JButton("前");
        reverserSetNButton = new JButton("中");
        reverserSetBButton = new JButton("後");

        style(teButton, BUTTON_NORMAL);

        style(doorOpenLButton,  BUTTON_MIDLARGE);
        style(ActionButton,     BUTTON_MIDLARGE);
        style(doorOpenRButton,  BUTTON_MIDLARGE);

        style(doorCloseButtonL, BUTTON_NORMAL);
        style(doorReOpenButton, BUTTON_NORMAL);        
        style(doorCloseButtonR, BUTTON_NORMAL);

        style(showResetDistanceWindowButon, BUTTON_LARGE);
        style(showSetTrainNumWindowButton,  BUTTON_NORMAL);
        style(allResetButon,                BUTTON_NORMAL);
        style(atspBrakeReleaseButton,       BUTTON_NORMAL);
        style(reverserSetFButton,           BUTTON_SMALL);
        style(reverserSetNButton,           BUTTON_SMALL);
        style(reverserSetBButton,           BUTTON_SMALL);

        speedLabel       = createLabel(NA);
        bcLabel          = createLabel(NA);
        mrLabel          = createLabel(NA);
        distanceLabel    = createLabel(NA);
        notchPosLabel    = createLabel(NA);
        infoTrainLabel   = createLabel("");
        infoAtsLabel     = createLabel("");
        infoTascLabel    = createLabel("");
        infoTrainExLabel = createLabel("");
        trainNumberLabel = createLabel("");
        formationLabel   = createLabel("");

        indicator.setPreferredSize(new Dimension(12, 12));

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
        doorCloseButtonL.addActionListener(e -> toggleBlink(doorCloseButtonL, "door", DOOR_CLOSE));
        doorCloseButtonR.addActionListener(e -> toggleBlink(doorCloseButtonR, "door", DOOR_CLOSE));

        doorReOpenButton.addActionListener(e ->
        {
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
                tn.setAlphabet("");
                tn.setNumber("");
                tn.setHalf("");
                tn.setFull("");
            }
        });

        ActionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (buttonCommand != null)
                {
                    if ("door".equals(buttonCommand))
                    {
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

        atspBrakeReleaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(!tc.isRunningTrain())
                    atsp.releaseATSPBrake();
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

        TEPanel.add(teButton);

        g.gridx = 0; g.gridy = 0; doorMatrix.add(doorOpenLButton, g);
        g.gridx = 1; g.gridy = 0; doorMatrix.add(ActionButton,    g);
        g.gridx = 2; g.gridy = 0; doorMatrix.add(doorOpenRButton, g);

        g.gridx = 0; g.gridy = 1; doorMatrix.add(doorCloseButtonL,g);
        g.gridx = 1; g.gridy = 1; doorMatrix.add(doorReOpenButton,g);
        g.gridx = 2; g.gridy = 1; doorMatrix.add(doorCloseButtonR,g);

        JPanel doorRow = new JPanel(new BorderLayout());
        doorRow.add(doorMatrix, BorderLayout.WEST);

        reverserPanel.add(reverserSetFButton);
        reverserPanel.add(reverserSetNButton);
        reverserPanel.add(reverserSetBButton);

        utilPanel.add(showResetDistanceWindowButon);
        utilPanel.add(showSetTrainNumWindowButton);
        utilPanel.add(allResetButon);
        utilPanel.add(atspBrakeReleaseButton);

        meterPanel.add(speedLabel);
        meterPanel.add(bcLabel);
        meterPanel.add(mrLabel);
        meterPanel.add(distanceLabel);
        meterPanel.add(notchPosLabel);

        stateRow.add(infoTrainLabel);
        atsRow.add(infoAtsLabel);
        tascRow.add(infoTascLabel);
        extraRow.add(infoTrainExLabel);

        idRow.add(trainNumberLabel);
        idRow.add(formationLabel);

        bottomPanel.add(indicator);

        controlPanel.add(TEPanel);
        controlPanel.add(doorRow);
        controlPanel.add(reverserPanel);
        controlPanel.add(utilPanel);

        infoPanel.add(meterPanel);

        for (JPanel p : new JPanel[]{meterPanel, stateRow, atsRow, tascRow, extraRow, idRow})
        {
            p.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            p.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            infoPanel.add(p);
        }

        JPanel infoContainer = new JPanel(new BorderLayout());
        infoContainer.add(infoPanel, BorderLayout.NORTH);

        root.add(controlPanel, BorderLayout.NORTH);
        root.add(infoContainer, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(root);

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
        doorCloseButtonL.setBackground(COLOR_DEFAULT);
        doorCloseButtonR.setBackground(COLOR_DEFAULT);

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
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_HIDE_EBBUZZER)) trainStatEX.append("EBﾀｲﾏ　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_EB)) trainStatEX.append("EBﾌﾞﾚｰｷ　");
        if(tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_STA)) trainStatEX.append("次駅接近　");
        infoTrainExLabel.setText(trainStatEX.toString());

        if (tn.getHalf() != null)
        {
            trainNumberLabel.setText("列車番号: "+ tn.getHalf());
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

        int sendNotch = TrainControl.NOTCH_NONE;

        if (tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING)) sendNotch = TrainControl.NOTCH_MAX;
        if (tc.getboolTrainStat(TrainControl.TRAINSTAT_EB)) sendNotch = TrainControl.NOTCH_EB;
        if (tc.getboolTrainStat(TrainControl.TRAINSTAT_DS_BRAKE)) sendNotch = TrainControl.NOTCH_EB;

        networkManager.sendCommand("send", "notch", sendNotch);
    }

    private void updateTrainState()
    {
        // 走行時戸開禁止
        boolean enableDoorButton = !tc.isRunningTrain();
        doorCloseButtonL.setEnabled(enableDoorButton);
        doorCloseButtonR.setEnabled(enableDoorButton);
        doorOpenLButton.setEnabled(enableDoorButton);
        doorOpenRButton.setEnabled(enableDoorButton);
        doorReOpenButton.setEnabled(enableDoorButton);
        if (!enableDoorButton) tc.setPrevDoor(-1);
                
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
        atsp = tc.getATSPController();
        atsp.resetATSPFromInterface();
        distanceSetText = "0";
        buttonCommand = null;
        buttonDo = -1;
        signal_0 = 0;
        signal_1 = 0;
        tn.reset();
    }

    public void readyTimsTimerRefresh()
    {
        timsRefreshTimer = new Timer(1000, e -> 
        {
            timsUpdater.refresh();
        });
    }



    // MARK: 
    public void running()
    {
        cfg = new ConfigManager();
        cfg.load();

        address = cfg.get("ipAddress", "localhost");
        port = Integer.parseInt(cfg.get("port", "34575"));

        tn = new TrainNumber();
        networkManager = new NetworkManager();
        networkManager.clientInit(address, port, 60000, 32768);

        serial = new Serial();
        serial.initialize("COM9");

        tc = new TrainControl(serial);
        tc.boolTrainStatInit(128);

        dcr = new DutyCardRepository();
        om = new TimsToolkit();

        timsUpdater = new TimsUpdater();
        timsUpdater.init(tc);

        indicator = new NetworkIndicator();
        networkManager.setOnSendCallback(() -> onEdt(() -> indicator.flash(TRTYPE.SEND)));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            if (networkManager != null)
            {
                networkManager.sendString("{\"type\":\"kill\"}");
                networkManager.clientClose();
            }
        }));

        System.out.println("client starting on "+ address +":"+ port +"...");

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
                // System.exit(0);
            }
        );

        new Thread(() -> {
            TimsSetup.setCore(this);
            Application.launch(TimsSetup.class);
        }).start();

        // new Thread(() -> {
        //     GaugeController.setCore(this);
        //     Application.launch(GaugeController.class);
        // }).start();

        new Thread(() ->
        {
            String fetchData = null;

            serial.sendRawJson("{\"type\":\"start\"}");

            while(true)
            {
                try {
                    fetchData = networkManager.clientReceiveString();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    try{
                        Thread.sleep(50);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }

                if(fetchData != null)
                {
                    onEdt(() -> indicator.flash(TRTYPE.RECIEVE));

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
                                tc.setTotalMove(jsonObj.getFloat("totalMove"));
                                tc.setReverser(jsonObj.getInt("reverser"));
                                tc.setLimit(jsonObj.getInt("speedLimit"));
                                tc.setTASCEnable(jsonObj.getBoolean("isTASCEnable"));
                                tc.setTASCBraking(jsonObj.getBoolean("isTASCBraking"));
                                tc.setCars(jsonObj.getInt("formation"));
                                tc.setSpeedState(tc.convertSpeedStateFromInt(jsonObj.getInt("speedState")));

                                tc.handleDoors();
                                tc.handleTEunlock();
                                tc.handleEBunlock();
                                tc.handleEB();
                                tc.handleRunningOpen();
                                atsp.handleATSNW();
                                tc.handleArrivingStation();
                                tc.handleEvent();
                                tc.setFormation(tc.getCars(), null);
                       
                                // GUI更新
                                onEdt(() ->
                                {
                                    if (!refreshTimer.isRunning())
                                    {
                                        refreshTimer.start();
                                    }
                                });

                                if(timsRefreshTimer != null && !timsRefreshTimer.isRunning())
                                {
                                    timsRefreshTimer.start();
                                }

                                break;

                            case "kill":
                                networkManager.clientClose();
                                JSONObject json = new JSONObject();
                                json.put("type", "send");
                                json.put("message", "door");
                                json.put("door", buttonDo);

                                serial.send(json);

                                serial.close();
                                // System.exit(0);
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

                                if(refreshTimer.isRunning()) refreshTimer.stop();
                                if(timsRefreshTimer.isRunning()) timsRefreshTimer.stop();

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

                                    case 10:
                                        // ATS-P停止パターン生成
                                        // signal_1 = 0...リセット
                                        // それ以外：*10した値が停止限界までの距離
                                        //
                                        atsp.setStopPattern(signal_1 *10);
                                        break;

                                    case 21:
                                        // ATS-P制限速度パターン 制限開始距離セット
                                        atsp.setDistance(signal_1 *10);
                                        break;

                                    case 22:
                                        // ATS-P制限速度パターン 制限速度セット
                                        atsp.setTargetSpeed(signal_1);
                                        break;

                                    case 23:
                                        // ATS-P制限速度パターン 制限開始
                                        atsp.setDecelPattern();
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
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}