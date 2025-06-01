package jp.akidukisystems.software.traindataclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.json.JSONObject;

public class TDCCore {

    private static final int PORT = 34565;
    public static final int DOOR_CLOSE = 0;
    public static final int DOOR_RIGHT = 1;
    public static final int DOOR_LEFT = 2;
    public static final int DOOR_BOTH = 3;

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

    private static TrainNumber tn;
    private static TrainControl tc;

    public static void main(String[] args) throws IOException {
        TDCCore clientObject = new TDCCore();
        tn = new TrainNumber();
        networkManager = new NetworkManager();
        networkManager.clientInit(PORT);
        tc = new TrainControl();
        tc.boolTrainStatInit(128);

        clientObject.running();
    }

    

    public void setupUI() {
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

            JFrame distanceResetFrame = new JFrame("キロ程リセット");
            distanceResetFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            distanceResetFrame.setSize(640, 480);

            JButton setDistance0Button = new JButton("0");
            setDistance0Button.setBounds(0, 0, 50, 50);
            JButton setDistance1Button = new JButton("1");
            setDistance1Button.setBounds(50, 0, 50, 50);
            JButton setDistance2Button = new JButton("2");
            setDistance2Button.setBounds(100, 0, 50, 50);
            JButton setDistance3Button = new JButton("3");
            setDistance3Button.setBounds(150, 0, 50, 50);
            JButton setDistance4Button = new JButton("4");
            setDistance4Button.setBounds(200, 0, 50, 50);
            JButton setDistance5Button = new JButton("5");
            setDistance5Button.setBounds(0, 50, 50, 50);
            JButton setDistance6Button = new JButton("6");
            setDistance6Button.setBounds(50, 50, 50, 50);
            JButton setDistance7Button = new JButton("7");
            setDistance7Button.setBounds(100, 50, 50, 50);
            JButton setDistance8Button = new JButton("8");
            setDistance8Button.setBounds(150, 50, 50, 50);
            JButton setDistance9Button = new JButton("9");
            setDistance9Button.setBounds(200, 50, 50, 50);
            JButton setDistancePeriodButton = new JButton(".");
            setDistancePeriodButton.setBounds(250, 50, 50, 50);
            JButton setDistanceButton = new JButton("設定");
            setDistanceButton.setBounds(300, 50, 100, 50);
            distanceSetUPButton = new JButton("上り");
            distanceSetUPButton.setBounds(0, 100, 100, 50);
            dictanceSetDOWNButton = new JButton("下り");
            dictanceSetDOWNButton.setBounds(100, 100, 100, 50);

            setDistance0Button.addActionListener(_ -> distanceSetText += "0");
            setDistance1Button.addActionListener(_ -> distanceSetText += "1");
            setDistance2Button.addActionListener(_ -> distanceSetText += "2");
            setDistance3Button.addActionListener(_ -> distanceSetText += "3");
            setDistance4Button.addActionListener(_ -> distanceSetText += "4");
            setDistance5Button.addActionListener(_ -> distanceSetText += "5");
            setDistance6Button.addActionListener(_ -> distanceSetText += "6");
            setDistance7Button.addActionListener(_ -> distanceSetText += "7");
            setDistance8Button.addActionListener(_ -> distanceSetText += "8");
            setDistance9Button.addActionListener(_ -> distanceSetText += "9");
            setDistancePeriodButton.addActionListener(_ -> distanceSetText += ".");
            distanceSetUPButton.addActionListener(_ -> networkManager.sendCommand("send", "moveTo", 0));
            dictanceSetDOWNButton.addActionListener(_ -> networkManager.sendCommand("send", "moveTo", 1));

            setDistanceButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        float newDistance = Float.parseFloat(distanceSetText);
                       networkManager.sendCommand("send", "move", newDistance *1000f);
                    } catch (NumberFormatException ef) {
                        System.out.println(ef);
                    }
                    distanceSetText = "0";
                    distanceResetFrame.setVisible(false);
                }
            });

        // bg

            JFrame epFrame = new JFrame("防護無線");
            epFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            epFrame.setSize(480, 300);

            epButton = new JButton("発報");
            epButton.setBounds(140, 75, 200, 150);
            epButton.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));

            epButtonTimer = new Timer(200, new ActionListener() {
                private boolean on = true;
                @Override
                public void actionPerformed(ActionEvent e) {
                    r.setBackground(on ? Color.RED : Color.WHITE);
                    on = !on;
                } 
            });

            epButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!tc.isRaisingEP()) {
                        epButtonTimer.start();
                        epButton.setText("復位");
                    } else {
                        epButtonTimer.stop();
                        r.setBackground(Color.WHITE);
                        epButton.setText("発報");
                    }
                    tc.setRaisingEP(!tc.isRaisingEP());
                }
            });

        // trainnum

            JFrame trainNumSetFrame = new JFrame("列車番号");
            trainNumSetFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            trainNumSetFrame.setSize(640, 480);

            JButton setTrainNum0Button = new JButton("0");
            setTrainNum0Button.setBounds(0, 0, 50, 50);
            JButton setTrainNum1Button = new JButton("1");
            setTrainNum1Button.setBounds(50, 0, 50, 50);
            JButton setTrainNum2Button = new JButton("2");
            setTrainNum2Button.setBounds(100, 0, 50, 50);
            JButton setTrainNum3Button = new JButton("3");
            setTrainNum3Button.setBounds(150, 0, 50, 50);
            JButton setTrainNum4Button = new JButton("4");
            setTrainNum4Button.setBounds(200, 0, 50, 50);
            JButton setTrainNum5Button = new JButton("5");
            setTrainNum5Button.setBounds(0, 50, 50, 50);
            JButton setTrainNum6Button = new JButton("6");
            setTrainNum6Button.setBounds(50, 50, 50, 50);
            JButton setTrainNum7Button = new JButton("7");
            setTrainNum7Button.setBounds(100, 50, 50, 50);
            JButton setTrainNum8Button = new JButton("8");
            setTrainNum8Button.setBounds(150, 50, 50, 50);
            JButton setTrainNum9Button = new JButton("9");
            setTrainNum9Button.setBounds(200, 50, 50, 50);

            JTextField textTrainStr = new JTextField("");
            textTrainStr.setBounds(250, 50, 100, 50);

            JButton setTrainNumButton = new JButton("設定");
            setTrainNumButton.setBounds(300, 50, 100, 50);

            setTrainNum0Button.addActionListener(_ -> tn.number += "0");
            setTrainNum1Button.addActionListener(_ -> tn.number += "1");
            setTrainNum2Button.addActionListener(_ -> tn.number += "2");
            setTrainNum3Button.addActionListener(_ -> tn.number += "3");
            setTrainNum4Button.addActionListener(_ -> tn.number += "4");
            setTrainNum5Button.addActionListener(_ -> tn.number += "5");
            setTrainNum6Button.addActionListener(_ -> tn.number += "6");
            setTrainNum7Button.addActionListener(_ -> tn.number += "7");
            setTrainNum8Button.addActionListener(_ -> tn.number += "8");
            setTrainNum9Button.addActionListener(_ -> tn.number += "9");

            setTrainNumButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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

            JButton ActionButton = new JButton("設定");
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

            speedLabel = new JLabel("N/A");
            speedLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            speedLabel.setForeground(Color.BLACK);
            speedLabel.setBounds(0, 200, 100, 20);

            bcLabel = new JLabel("N/A");
            bcLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            bcLabel.setForeground(Color.BLACK);
            bcLabel.setBounds(100, 200, 75, 20);

            mrLabel = new JLabel("N/A");
            mrLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            mrLabel.setForeground(Color.BLACK);
            mrLabel.setBounds(175, 200, 75, 20);

            distanceLabel = new JLabel("N/A");
            distanceLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            distanceLabel.setForeground(Color.BLACK);
            distanceLabel.setBounds(250, 200, 100, 20);

            notchPosLabel = new JLabel("N/A");
            notchPosLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            notchPosLabel.setForeground(Color.BLACK);
            notchPosLabel.setBounds(350, 200, 50, 20);

            infoTrainLabel = new JLabel("");
            infoTrainLabel.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
            infoTrainLabel.setForeground(Color.BLACK);
            infoTrainLabel.setBounds(0, 250, 600, 20);

            infoAtsLabel = new JLabel("");
            infoAtsLabel.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
            infoAtsLabel.setForeground(Color.BLACK);
            infoAtsLabel.setBounds(0, 300, 600, 20);

            infoTascLabel = new JLabel("");
            infoTascLabel.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
            infoTascLabel.setForeground(Color.BLACK);
            infoTascLabel.setBounds(0, 350, 600, 20);

            infoTrainExLabel = new JLabel("");
            infoTrainExLabel.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
            infoTrainExLabel.setForeground(Color.BLACK);
            infoTrainExLabel.setBounds(0, 400, 600, 20);

            trainNumberLabel = new JLabel("");
            trainNumberLabel.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
            trainNumberLabel.setForeground(Color.BLACK);
            trainNumberLabel.setBounds(0, 450, 400, 20);

            teButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tc.setTE(true);
                }
            });

            doorOpenLButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (buttonDo != -1) {
                        buttonCommand = null;
                        ActionButton.setBackground(Color.WHITE);
                        doorOpenLButton.setBackground(Color.WHITE);
                        doorOpenRButton.setBackground(Color.WHITE);
                        doorCloseButton.setBackground(Color.WHITE);
                        doorReOpenButton.setBackground(Color.WHITE);
                        
                        if (buttonDo != DOOR_LEFT) {
                            buttonCommand = "door";
                            buttonDo = DOOR_LEFT;
                            blinkTimer.start();
                            doorOpenLButton.setBackground(Color.YELLOW);
                        } else {
                            buttonDo = -1;
                            blinkTimer.stop();
                        }
                    } else {
                        buttonCommand = "door";
                        buttonDo = DOOR_LEFT;
                        blinkTimer.start();
                        doorOpenLButton.setBackground(Color.YELLOW);
                    }
                }
            });
            doorOpenRButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (buttonDo != -1) {
                        buttonCommand = null;
                        ActionButton.setBackground(Color.WHITE);
                        doorOpenLButton.setBackground(Color.WHITE);
                        doorOpenRButton.setBackground(Color.WHITE);
                        doorCloseButton.setBackground(Color.WHITE);
                        doorReOpenButton.setBackground(Color.WHITE);

                        if (buttonDo != DOOR_RIGHT) {
                            buttonCommand = "door";
                            buttonDo = DOOR_RIGHT;
                            blinkTimer.start();
                            doorOpenRButton.setBackground(Color.YELLOW);
                        } else {
                            buttonDo = -1;
                            blinkTimer.stop();
                        }
                    } else {
                        buttonCommand = "door";
                        buttonDo = DOOR_RIGHT;
                        blinkTimer.start();
                        doorOpenRButton.setBackground(Color.YELLOW);
                    }
                }
            });
            doorCloseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (buttonDo != -1) {
                        buttonCommand = null;
                        ActionButton.setBackground(Color.WHITE);
                        doorOpenLButton.setBackground(Color.WHITE);
                        doorOpenRButton.setBackground(Color.WHITE);
                        doorCloseButton.setBackground(Color.WHITE);
                        doorReOpenButton.setBackground(Color.WHITE);
                        
                        if (buttonDo != DOOR_CLOSE) {
                            buttonCommand = "door";
                            buttonDo = DOOR_CLOSE;
                            blinkTimer.start();
                            doorCloseButton.setBackground(Color.YELLOW);
                        } else {
                            buttonDo = -1;
                            blinkTimer.stop();
                        }
                    } else {
                        buttonCommand = "door";
                        buttonDo = DOOR_CLOSE;
                        blinkTimer.start();
                        doorCloseButton.setBackground(Color.YELLOW);
                    }
                }
            });
            doorReOpenButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    buttonCommand = null;
                    buttonDo = -1;
                    blinkTimer.stop();
                    ActionButton.setBackground(Color.WHITE);
                    doorOpenLButton.setBackground(Color.WHITE);
                    doorOpenRButton.setBackground(Color.WHITE);
                    doorCloseButton.setBackground(Color.WHITE);
                    doorReOpenButton.setBackground(Color.WHITE);
                    if (tc.getPrevDoor() != -1 )networkManager.sendCommand("send", "door", tc.getPrevDoor());
                }
            });

            showResetDistanceWindowButon.addActionListener(_ -> distanceResetFrame.setVisible(true));
            showSetTrainNumWindowButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    trainNumSetFrame.setVisible(true);
                    tn.alphabet = "";
                    tn.number = "";
                    tn.half = "";
                    tn.full = "";
                }
            });

            ActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switch (buttonCommand) {
                        case "door":
                            if (buttonDo == 0) {
                                tc.setPrevDoor(tc.getDoor());
                            }
                           networkManager.sendCommand("send", buttonCommand, buttonDo);
                            break;
                    
                        default:
                            break;
                    }

                    buttonCommand = null;
                    buttonDo = -1;
                    blinkTimer.stop();
                    ActionButton.setBackground(Color.WHITE);
                    doorOpenLButton.setBackground(Color.WHITE);
                    doorOpenRButton.setBackground(Color.WHITE);
                    doorCloseButton.setBackground(Color.WHITE);
                }
            });

            allResetButon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reset();
                }
            });

            reverserSetFButton.addActionListener(_ ->networkManager.sendCommand("send", "reverser", 0));
            reverserSetNButton.addActionListener(_ ->networkManager.sendCommand("send", "reverser", 1));
            reverserSetBButton.addActionListener(_ ->networkManager.sendCommand("send", "reverser", 2));

            blinkTimer = new Timer(500, new ActionListener() {
                private boolean on = true;

                @Override
                public void actionPerformed(ActionEvent e) {
                    switch (buttonCommand) {
                        case "door":
                            ActionButton.setBackground(on ? Color.YELLOW : Color.WHITE);
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

        p.add(reverserSetFButton);
        p.add(reverserSetNButton);
        p.add(reverserSetBButton);

        q.add(setDistance0Button);
        q.add(setDistance1Button);
        q.add(setDistance2Button);
        q.add(setDistance3Button);
        q.add(setDistance4Button);
        q.add(setDistance5Button);
        q.add(setDistance6Button);
        q.add(setDistance7Button);
        q.add(setDistance8Button);
        q.add(setDistance9Button);
        q.add(setDistancePeriodButton);
        q.add(setDistanceButton);
        q.add(distanceSetUPButton);
        q.add(dictanceSetDOWNButton);

        r.add(epButton);

        s.add(setTrainNumButton);
        s.add(setTrainNum0Button);
        s.add(setTrainNum1Button);
        s.add(setTrainNum2Button);
        s.add(setTrainNum3Button);
        s.add(setTrainNum4Button);
        s.add(setTrainNum5Button);
        s.add(setTrainNum6Button);
        s.add(setTrainNum7Button);
        s.add(setTrainNum8Button);
        s.add(setTrainNum9Button);
        s.add(textTrainStr);

        ActionButton.setBackground(Color.WHITE);
        doorOpenLButton.setBackground(Color.WHITE);
        doorOpenRButton.setBackground(Color.WHITE);
        doorCloseButton.setBackground(Color.WHITE);

        frame.getContentPane().add(p, BorderLayout.CENTER);
        distanceResetFrame.getContentPane().add(q, BorderLayout.CENTER);
        epFrame.getContentPane().add(r, BorderLayout.CENTER);
        trainNumSetFrame.getContentPane().add(s, BorderLayout.CENTER);


        reverserSetFButton.setEnabled(false);
        reverserSetNButton.setEnabled(false);
        reverserSetBButton.setEnabled(false);

        epButton.setBackground(Color.WHITE);

        // 表示
        frame.setVisible(true);
        distanceResetFrame.setVisible(false);
        epFrame.setVisible(true);
        trainNumSetFrame.setVisible(false);

        refreshTimer = new Timer(250, _ -> {
            speedLabel.setText(String.format("%dkm/h", (int) tc.getSpeed()));
            bcLabel.setText(String.format("%dkpa", tc.getBc()));
            mrLabel.setText(String.format("%dkpa", tc.getMr()));
            distanceLabel.setText(String.format("%.1fkm", tc.getMove() / 1000f));

            // 走行時戸開禁止
            if (tc.isRunningTrain()) {
                doorCloseButton.setEnabled(false);
                doorOpenLButton.setEnabled(false);
                doorOpenRButton.setEnabled(false);
                doorReOpenButton.setEnabled(false);
                tc.setPrevDoor(-1);
            } else {
                doorCloseButton.setEnabled(true);
                doorOpenLButton.setEnabled(true);
                doorOpenRButton.setEnabled(true);
                doorReOpenButton.setEnabled(true);
            }
            
            // ノッチ位置表示
            boolean canChangeReverser = false;
            if (tc.getNotch() == TrainControl.NOTCH_EB) {
                notchPosLabel.setText("EB");
                canChangeReverser = true;
            } else if (tc.getNotch() == TrainControl.NOTCH_N) {
                notchPosLabel.setText("N");
            } else if (tc.getNotch() > TrainControl.NOTCH_N) {
                notchPosLabel.setText(String.format("P%d", tc.getNotch()));
            } else {
                notchPosLabel.setText(String.format("B%d", tc.getNotch() *-1));
            }

            // キロ程演算方向
            if (tc.getMoveTo() == 0) {
                distanceSetUPButton.setEnabled(false);
                dictanceSetDOWNButton.setEnabled(true);
            } else {
                distanceSetUPButton.setEnabled(true);
                dictanceSetDOWNButton.setEnabled(false);
            }

            // レバーサ
            // ノッチがEB位置じゃないとレバーサいじれないよう変更
            if (canChangeReverser) {
                switch (tc.getReverser()) {
                    case 0:
                        reverserSetFButton.setEnabled(false);
                        reverserSetNButton.setEnabled(true);
                        reverserSetBButton.setEnabled(true);
                        break;

                    case 1:
                        reverserSetFButton.setEnabled(true);
                        reverserSetNButton.setEnabled(false);
                        reverserSetBButton.setEnabled(true);
                        break;

                    case 2:
                        reverserSetFButton.setEnabled(true);
                        reverserSetNButton.setEnabled(true);
                        reverserSetBButton.setEnabled(false);
                        break;
                
                    default:
                        break;
                }
            } else {
                reverserSetFButton.setEnabled(false);
                reverserSetNButton.setEnabled(false);
                reverserSetBButton.setEnabled(false);
            }

            // モニタ類

            // ATS-P
            tc.refreshTrainStat();

            if (tc.getboolTrainStat(TrainControl.TRAINSTAT_EB)) networkManager.sendCommand("send", "notch", TrainControl.NOTCH_EB);
            if (tc.getboolTrainStat(TrainControl.TRAINSTAT_DS_BRAKE)) networkManager.sendCommand("send", "notch", TrainControl.NOTCH_EB);


            tc.txtTrainStat = "状態:";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_PARKING) ? "駐車　" : "";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_CONSTANT_SPEED) ? "定速　" : "";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_DS_BRAKE) ? "保B　" : "";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_SNOW_BRAKE) ? "雪B　" : "";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_EMERG_SHORT) ? "非短　" : "";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_THREE_PHASE) ? "三相　" : "";
            tc.txtTrainStat += tc.getboolTrainStat(TrainControl.TRAINSTAT_EB) ? "非常　" : "";
            infoTrainLabel.setText(tc.txtTrainStat);

            tc.txtATS = "ATS:";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_OPERATING) ? "ATS動作　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_POWER) ? "ATS電源　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_ERROR) ? "故障　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_ACTIVE) ? "ATS-P　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_RELEASE) ? "B解放　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING) ? "B　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_NEAR_PATTERN) ? "ﾊﾟﾀﾝ接近　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_POWER) ? "P電源　" : "";
            tc.txtATS += tc.getboolTrainStat(TrainControl.ATS_P_BRAKE_OPERATING_EB) ? "非常　" : "";
            infoAtsLabel.setText(tc.txtATS);

            tc.txtTASC = "TASC:";
            tc.txtTASC += tc.getboolTrainStat(TrainControl.TASC_POWER) ? "電源　" : "";
            tc.txtTASC += tc.getboolTrainStat(TrainControl.TASC_PATTERN_ACTIVE) ? "ﾊﾟﾀﾝ　" : "";
            tc.txtTASC += tc.getboolTrainStat(TrainControl.TASC_BRAKE) ? "B　" : "";
            tc.txtTASC += tc.getboolTrainStat(TrainControl.TASC_OFF) ? "切　" : "";
            tc.txtTASC += tc.getboolTrainStat(TrainControl.TASC_ERROR) ? "故障　" : "";
            infoTascLabel.setText(tc.txtTASC);

            tc.txtTrainStatEx = "";
            tc.txtTrainStatEx += tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_DOOR_CLOSE) ? "戸閉　" : "";
            tc.txtTrainStatEx += tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_EB) ? "EB装置　" : "";
            tc.txtTrainStatEx += tc.getboolTrainStat(TrainControl.TRAINSTAT_EX_STA) ? "次駅接近　" : "";
            infoTrainExLabel.setText(tc.txtTrainStatEx);

            if (tn.half != null)
            {
                trainNumberLabel.setText("列車番号: "+ tn.half);
            }
            else
            {
                trainNumberLabel.setText("列車番号を設定してください");
            }
        });
    }

    public void reset()
    {
        tc.resetTrain();
        distanceSetText = "0";
        buttonCommand = null;
        buttonDo = -1;
        signal_0 = 0;
        signal_1 = 0;
        tn.alphabet = null;
        tn.number = null;
        tn.half = null;
        tn.full = null;
    }

    public void running() throws IOException {
        SwingUtilities.invokeLater(this::setupUI);

        System.out.println("client starting on port " + PORT + "...");

        tc.refreshTimer();
        reset();

        new Thread(() -> {
            while(true) {
                String fetchData = networkManager.clientReciveString();

                if(fetchData != null) {
                    System.out.println(fetchData);
                    JSONObject jsonObj = new JSONObject(fetchData);
                    switch (jsonObj.getString("type")) {
                        case "send":
                            // 受信
                            tc.setPrevId(tc.getId());
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

                            // tc.refreshTrainId();
                            tc.doorOpen_Close();
                            tc.handleTEunlock();
                            tc.handleEBunlock();
                            tc.handleEB();
                            tc.handleRunningOpen();
                            tc.handleATSNW();
                            tc.handleArrivingStation();

                            // GUI更新
                            if (!refreshTimer.isRunning()) refreshTimer.start();

                            //System.out.println(String.format("speed:%.2fkm/h notch:%d door:%d bc:%d mr:%d move:%.2f", speed, notch, door, bc, mr, move));
                            break;

                        case "kill":
                            networkManager.clientClose();
                            System.exit(0);
                            break;

                        case "notRidingTrain":
                            // 列車に載ってない
                            speedLabel.setText("N/A");
                            bcLabel.setText("N/A");
                            mrLabel.setText("N/A");
                            distanceLabel.setText("N/A");
                            notchPosLabel.setText("N/A");
                            reverserSetFButton.setEnabled(false);
                            reverserSetNButton.setEnabled(false);
                            reverserSetBButton.setEnabled(false);
                            infoTrainLabel.setText("");
                            infoAtsLabel.setText("");
                            infoTascLabel.setText("");
                            infoTrainExLabel.setText("");
                            if (refreshTimer.isRunning()) refreshTimer.stop();
                            break;

                        case "beacon":
                            signal_0 = jsonObj.getInt("signal_0");
                            signal_1 = jsonObj.getInt("signal_1");

                            switch (signal_0) {
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
                } else {
                    networkManager.clientInit(PORT);
                }
            }
        }).start();
    }
}