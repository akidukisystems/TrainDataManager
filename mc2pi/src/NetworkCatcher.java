import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.json.JSONObject;

public class NetworkCatcher {

    private static final int PORT = 34565;
    public static final int DOOR_CLOSE = 0;
    public static final int DOOR_RIGHT = 1;
    public static final int DOOR_LEFT = 2;
    public static final int DOOR_BOTH = 3;
    public static final int NOTCH_EB = -8;
    public static final int NOTCH_MAX = -7;
    public static final int NOTCH_N = 0;

    public static final int ATS_OPERATING = 0;
    public static final int ATS_POWER = 1;
    public static final int ATS_P_ERROR = 2;
    public static final int ATS_P_ACTIVE = 3;
    public static final int ATS_P_BRAKE_RELEASE = 4;
    public static final int ATS_P_BRAKE_OPERATING = 5;
    public static final int ATS_P_NEAR_PATTERN = 6;
    public static final int ATS_P_POWER = 7;
    public static final int ATS_P_BRAKE_OPERATING_EB = 8;

    public static final int TRAINSTAT_PARKING = 0;
    public static final int TRAINSTAT_CONSTANT_SPEED = 1;
    public static final int TRAINSTAT_DS_BRAKE = 2;
    public static final int TRAINSTAT_SNOW_BRAKE = 3;
    public static final int TRAINSTAT_EMERG_SHORT = 4;
    public static final int TRAINSTAT_THREE_PHASE = 5;
    public static final int TRAINSTAT_EB = 6;

    public static final int TASC_POWER = 0;
    public static final int TASC_PATTERN_ACTIVE = 1;
    public static final int TASC_BRAKE = 2;
    public static final int TASC_OFF = 3;
    public static final int TASC_ERROR = 4;

    public static final int TRAINSTAT_EX_DOOR_CLOSE = 0;
    public static final int TRAINSTAT_EX_EB = 1;
    public static final int TRAINSTAT_EX_STA = 2;

    public static Socket clientSocket;
    Socket client;
    BufferedReader reader = null;
    PrintWriter writer = null;
    Socket c2s = null;

    int id;
    int prevId;

    float speed = 0f;
    int notch = 0;
    int door = 0;
    int bc = 0;
    int mr = 0;
    float move = 0f;
    int moveTo = 1;
    int reverser = 0;
    int limit = Integer.MAX_VALUE;

    boolean isTASCEnable = true;
    boolean isTASCBraking = false;

    boolean isTE = false;
    boolean isEB = false;
    boolean isATSPBrakeWorking = true;

    String distanceSetText = "0";

    String buttonCommand = null;
    int buttonDo = -1;

    int prevDoor;
    int prevNotch;

    boolean isDoorClose = true;
    boolean isRunningDoorOpen = false;

    int doorCloseCount = 0;
    int ATSPBrakeNWC = 0;
    public static final int DOOR_CLOSE_TIME = 8;
    public static final int ATSP_BRAKE_NWC_TIME = 10;

    int signal_0;
    int signal_1;
    boolean isArrivingStation = false;
    float beaconGetedPos = 0;

    Timer blinkTimer;
    Timer refreshTimer;
    Timer ebTimer;
    Timer ebActiveTimer;

    JLabel labelSpeed;
    JLabel labelBC;
    JLabel labelMR;
    JLabel labelDistance;
    JLabel labelNotchPos;

    JLabel labelTrainStat;
    JLabel labelATS;
    JLabel labelTASC;
    JLabel labelTrainStatEx;
    
    boolean[] boolTrainStat = {false, false, false, false, false, false, false};
    boolean[] boolATS = {false, true, false, false, false, false, false, true, false};
    boolean[] boolTASC = {true, false, false, false, false};
    boolean[] boolTrainStatEx = {false, false, false};

    String txtTrainStat = "";
    String txtATS = "";
    String txtTASC = "";
    String txtTrainStatEx = "";

    JButton buttonDoorOpenL;
    JButton buttonDoorOpenR;
    JButton buttonDoorClose;
    JButton buttonDoorReOpen;

    JButton buttonReverserSetF;
    JButton buttonReverserSetN;
    JButton buttonReverserSetB;

    JButton buttonSetDistanceUp;
    JButton buttonSetDistanceDown;

    public static void main(String[] args) throws IOException {
        NetworkCatcher clientObject = new NetworkCatcher();
        clientObject.running();
    }

    public void clientInit(int port) {
        while (true) {
            try {
                this.client = new Socket("localhost", port);
            } catch (UnknownHostException e) {
                // e.printStackTrace();
            } catch (IOException e) {
                // e.printStackTrace();
            }
            if(this.client != null) break;

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            this.writer = new PrintWriter(this.client.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String clientReciveString() {
        try {
            return this.reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clientSendString(String data) {
        this.writer.println(data);
    }

    public void clientClose() {
        System.out.println("exit");
        try {
            if (this.writer != null) this.writer.close();
            if (this.reader != null) this.reader.close();
            if (this.c2s != null) this.c2s.close();
            if (this.client != null) this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String type, String doAny, Object value) {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("doAny", doAny);
        obj.put(doAny, value);
        clientSendString(obj.toString());
    }

    public boolean isRunningTrain() {
        return (speed > 5);
    }

    public void setupUI() {
        // フレーム（ウィンドウ）を作成
        JPanel p = new JPanel();
        p.setLayout(null);

        JPanel q = new JPanel();
        q.setLayout(null);

        JFrame frame = new JFrame("NetworkCatcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);

        JButton buttonTE = new JButton("TE装置");
        buttonTE.setBounds(0, 0, 100, 50);

        buttonDoorOpenL = new JButton("左ドア開");
        buttonDoorOpenL.setBounds(0, 50, 100, 50);

        buttonDoorOpenR = new JButton("右ドア開");
        buttonDoorOpenR.setBounds(100, 50, 100, 50);

        buttonDoorClose = new JButton("ドア閉");
        buttonDoorClose.setBounds(0, 100, 100, 50);

        buttonDoorReOpen = new JButton("再開扉");
        buttonDoorReOpen.setBounds(100, 100, 100, 50);

        JButton buttonAction = new JButton("設定");
        buttonAction.setBounds(200, 50, 200, 100);

        JButton buttonResetDistance = new JButton("キロ程リセット");
        buttonResetDistance.setBounds(0, 150, 150, 50);

        buttonReverserSetF = new JButton("前");
        buttonReverserSetF.setBounds(150, 150, 50, 50);
        buttonReverserSetN = new JButton("中");
        buttonReverserSetN.setBounds(200, 150, 50, 50);
        buttonReverserSetB = new JButton("後");
        buttonReverserSetB.setBounds(250, 150, 50, 50);

        JFrame distanceReset = new JFrame("キロ程リセット");
        distanceReset.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        distanceReset.setSize(640, 480);

        JButton buttonSetDistance0 = new JButton("0");
        buttonSetDistance0.setBounds(0, 0, 50, 50);
        JButton buttonSetDistance1 = new JButton("1");
        buttonSetDistance1.setBounds(50, 0, 50, 50);
        JButton buttonSetDistance2 = new JButton("2");
        buttonSetDistance2.setBounds(100, 0, 50, 50);
        JButton buttonSetDistance3 = new JButton("3");
        buttonSetDistance3.setBounds(150, 0, 50, 50);
        JButton buttonSetDistance4 = new JButton("4");
        buttonSetDistance4.setBounds(200, 0, 50, 50);
        JButton buttonSetDistance5 = new JButton("5");
        buttonSetDistance5.setBounds(0, 50, 50, 50);
        JButton buttonSetDistance6 = new JButton("6");
        buttonSetDistance6.setBounds(50, 50, 50, 50);
        JButton buttonSetDistance7 = new JButton("7");
        buttonSetDistance7.setBounds(100, 50, 50, 50);
        JButton buttonSetDistance8 = new JButton("8");
        buttonSetDistance8.setBounds(150, 50, 50, 50);
        JButton buttonSetDistance9 = new JButton("9");
        buttonSetDistance9.setBounds(200, 50, 50, 50);
        JButton buttonSetDistancePeriod = new JButton(".");
        buttonSetDistancePeriod.setBounds(250, 50, 50, 50);
        JButton buttonSetDistance = new JButton("設定");
        buttonSetDistance.setBounds(300, 50, 100, 50);
        buttonSetDistanceUp = new JButton("上り");
        buttonSetDistanceUp.setBounds(0, 100, 100, 50);
        buttonSetDistanceDown = new JButton("下り");
        buttonSetDistanceDown.setBounds(100, 100, 100, 50);

        labelSpeed = new JLabel("N/A");
        labelSpeed.setFont(new Font("Arial", Font.PLAIN, 20));
        labelSpeed.setForeground(Color.BLACK);
        labelSpeed.setBounds(0, 200, 100, 20);

        labelBC = new JLabel("N/A");
        labelBC.setFont(new Font("Arial", Font.PLAIN, 20));
        labelBC.setForeground(Color.BLACK);
        labelBC.setBounds(100, 200, 75, 20);

        labelMR = new JLabel("N/A");
        labelMR.setFont(new Font("Arial", Font.PLAIN, 20));
        labelMR.setForeground(Color.BLACK);
        labelMR.setBounds(175, 200, 75, 20);

        labelDistance = new JLabel("N/A");
        labelDistance.setFont(new Font("Arial", Font.PLAIN, 20));
        labelDistance.setForeground(Color.BLACK);
        labelDistance.setBounds(250, 200, 100, 20);

        labelNotchPos = new JLabel("N/A");
        labelNotchPos.setFont(new Font("Arial", Font.PLAIN, 20));
        labelNotchPos.setForeground(Color.BLACK);
        labelNotchPos.setBounds(350, 200, 50, 20);

        labelTrainStat = new JLabel("");
        labelTrainStat.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
        labelTrainStat.setForeground(Color.BLACK);
        labelTrainStat.setBounds(0, 250, 600, 20);

        labelATS = new JLabel("");
        labelATS.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
        labelATS.setForeground(Color.BLACK);
        labelATS.setBounds(0, 300, 600, 20);

        labelTASC = new JLabel("");
        labelTASC.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
        labelTASC.setForeground(Color.BLACK);
        labelTASC.setBounds(0, 350, 600, 20);

        labelTrainStatEx = new JLabel("");
        labelTrainStatEx.setFont(new Font("ＭＳ　ゴシック", Font.PLAIN, 20));
        labelTrainStatEx.setForeground(Color.BLACK);
        labelTrainStatEx.setBounds(0, 400, 600, 20);

        buttonTE.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isTE = true;
            }
        });

        buttonDoorOpenL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buttonDo != -1) {
                    buttonCommand = null;
                    buttonAction.setBackground(Color.WHITE);
                    buttonDoorOpenL.setBackground(Color.WHITE);
                    buttonDoorOpenR.setBackground(Color.WHITE);
                    buttonDoorClose.setBackground(Color.WHITE);
                    buttonDoorReOpen.setBackground(Color.WHITE);
                    
                    if (buttonDo != DOOR_LEFT) {
                        buttonCommand = "door";
                        buttonDo = DOOR_LEFT;
                        blinkTimer.start();
                    } else {
                        buttonDo = -1;
                        blinkTimer.stop();
                    }
                } else {
                    buttonCommand = "door";
                    buttonDo = DOOR_LEFT;
                    blinkTimer.start();
                }
            }
        });
        buttonDoorOpenR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buttonDo != -1) {
                    buttonCommand = null;
                    buttonAction.setBackground(Color.WHITE);
                    buttonDoorOpenL.setBackground(Color.WHITE);
                    buttonDoorOpenR.setBackground(Color.WHITE);
                    buttonDoorClose.setBackground(Color.WHITE);
                    buttonDoorReOpen.setBackground(Color.WHITE);

                    if (buttonDo != DOOR_RIGHT) {
                        buttonCommand = "door";
                        buttonDo = DOOR_RIGHT;
                        blinkTimer.start();
                    } else {
                        buttonDo = -1;
                        blinkTimer.stop();
                    }
                } else {
                    buttonCommand = "door";
                    buttonDo = DOOR_RIGHT;
                    blinkTimer.start();
                }
            }
        });
        buttonDoorClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buttonDo != -1) {
                    buttonCommand = null;
                    buttonAction.setBackground(Color.WHITE);
                    buttonDoorOpenL.setBackground(Color.WHITE);
                    buttonDoorOpenR.setBackground(Color.WHITE);
                    buttonDoorClose.setBackground(Color.WHITE);
                    buttonDoorReOpen.setBackground(Color.WHITE);
                    
                    if (buttonDo != DOOR_CLOSE) {
                        buttonCommand = "door";
                        buttonDo = DOOR_CLOSE;
                        blinkTimer.start();
                    } else {
                        buttonDo = -1;
                        blinkTimer.stop();
                    }
                } else {
                    buttonCommand = "door";
                    buttonDo = DOOR_CLOSE;
                    blinkTimer.start();
                }
            }
        });
        buttonDoorReOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonCommand = null;
                buttonDo = -1;
                blinkTimer.stop();
                buttonAction.setBackground(Color.WHITE);
                buttonDoorOpenL.setBackground(Color.WHITE);
                buttonDoorOpenR.setBackground(Color.WHITE);
                buttonDoorClose.setBackground(Color.WHITE);
                buttonDoorReOpen.setBackground(Color.WHITE);
                sendCommand("send", "door", prevDoor);
            }
        });

        buttonResetDistance.addActionListener(_ -> distanceReset.setVisible(true));
        buttonAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (buttonCommand) {
                    case "door":
                        if (buttonDo == 0) {
                            prevDoor = door;
                        }
                        sendCommand("send", buttonCommand, buttonDo);
                        break;
                
                    default:
                        break;
                }

                buttonCommand = null;
                buttonDo = -1;
                blinkTimer.stop();
                buttonAction.setBackground(Color.WHITE);
                buttonDoorOpenL.setBackground(Color.WHITE);
                buttonDoorOpenR.setBackground(Color.WHITE);
                buttonDoorClose.setBackground(Color.WHITE);
            }
        });
        buttonReverserSetF.addActionListener(_ -> sendCommand("send", "reverser", 0));
        buttonReverserSetN.addActionListener(_ -> sendCommand("send", "reverser", 1));
        buttonReverserSetB.addActionListener(_ -> sendCommand("send", "reverser", 2));

        buttonSetDistance0.addActionListener(_ -> distanceSetText += "0");
        buttonSetDistance1.addActionListener(_ -> distanceSetText += "1");
        buttonSetDistance2.addActionListener(_ -> distanceSetText += "2");
        buttonSetDistance3.addActionListener(_ -> distanceSetText += "3");
        buttonSetDistance4.addActionListener(_ -> distanceSetText += "4");
        buttonSetDistance5.addActionListener(_ -> distanceSetText += "5");
        buttonSetDistance6.addActionListener(_ -> distanceSetText += "6");
        buttonSetDistance7.addActionListener(_ -> distanceSetText += "7");
        buttonSetDistance8.addActionListener(_ -> distanceSetText += "8");
        buttonSetDistance9.addActionListener(_ -> distanceSetText += "9");
        buttonSetDistancePeriod.addActionListener(_ -> distanceSetText += ".");
        buttonSetDistanceUp.addActionListener(_ -> sendCommand("send", "moveTo", 0));
        buttonSetDistanceDown.addActionListener(_ -> sendCommand("send", "moveTo", 1));

        buttonSetDistance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float newDistance = Float.parseFloat(distanceSetText);
                    sendCommand("send", "move", newDistance *1000f);
                } catch (NumberFormatException ef) {
                    System.out.println(ef);
                }
                distanceSetText = "0";
                distanceReset.setVisible(false);
            }
        });

        blinkTimer = new Timer(500, new ActionListener() {
            private boolean on = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (buttonCommand) {
                    case "door":
                        buttonAction.setBackground(on ? Color.YELLOW : Color.WHITE);
                        switch (buttonDo) {
                            case DOOR_LEFT:
                                buttonDoorOpenL.setBackground(on ? Color.YELLOW : Color.WHITE);
                                break;

                            case DOOR_RIGHT:
                                buttonDoorOpenR.setBackground(on ? Color.YELLOW : Color.WHITE);
                                break;

                            case DOOR_CLOSE:
                                buttonDoorClose.setBackground(on ? Color.YELLOW : Color.WHITE);
                                break;
                        
                            default:
                                break;
                        }
                        break;
                
                    default:
                        break;
                }
                on = !on;
            }
        });


        p.add(buttonTE);
        p.add(buttonDoorOpenL);
        p.add(buttonDoorOpenR);
        p.add(buttonDoorClose);
        p.add(buttonDoorReOpen);
        p.add(buttonResetDistance);
        p.add(buttonAction);

        p.add(labelSpeed);
        p.add(labelBC);
        p.add(labelMR);
        p.add(labelDistance);
        p.add(labelNotchPos);

        p.add(labelTrainStat);
        p.add(labelATS);
        p.add(labelTASC);
        p.add(labelTrainStatEx);

        p.add(buttonReverserSetF);
        p.add(buttonReverserSetN);
        p.add(buttonReverserSetB);

        q.add(buttonSetDistance0);
        q.add(buttonSetDistance1);
        q.add(buttonSetDistance2);
        q.add(buttonSetDistance3);
        q.add(buttonSetDistance4);
        q.add(buttonSetDistance5);
        q.add(buttonSetDistance6);
        q.add(buttonSetDistance7);
        q.add(buttonSetDistance8);
        q.add(buttonSetDistance9);
        q.add(buttonSetDistancePeriod);
        q.add(buttonSetDistance);
        q.add(buttonSetDistanceUp);
        q.add(buttonSetDistanceDown);

        buttonAction.setBackground(Color.WHITE);
        buttonDoorOpenL.setBackground(Color.WHITE);
        buttonDoorOpenR.setBackground(Color.WHITE);
        buttonDoorClose.setBackground(Color.WHITE);

        frame.getContentPane().add(p, BorderLayout.CENTER);
        distanceReset.getContentPane().add(q, BorderLayout.CENTER);

        buttonReverserSetF.setEnabled(false);
        buttonReverserSetN.setEnabled(false);
        buttonReverserSetB.setEnabled(false);

        // 表示
        frame.setVisible(true);
        distanceReset.setVisible(false);

        refreshTimer = new Timer(250, _ -> {
            labelSpeed.setText(String.format("%dkm/h", (int) speed));
            labelBC.setText(String.format("%dkpa", bc));
            labelMR.setText(String.format("%dkpa", mr));
            labelDistance.setText(String.format("%.1fkm", move / 1000f));

            // 走行時戸開禁止
            if (isRunningTrain()) {
                buttonDoorClose.setEnabled(false);
                buttonDoorOpenL.setEnabled(false);
                buttonDoorOpenR.setEnabled(false);
                buttonDoorReOpen.setEnabled(false);
                prevDoor = 0;
            } else {
                buttonDoorClose.setEnabled(true);
                buttonDoorOpenL.setEnabled(true);
                buttonDoorOpenR.setEnabled(true);
                buttonDoorReOpen.setEnabled(true);
            }
            
            // ノッチ位置表示
            boolean canChangeReverser = false;
            if (notch == NOTCH_EB) {
                labelNotchPos.setText("EB");
                canChangeReverser = true;
            } else if (notch == NOTCH_N) {
                labelNotchPos.setText("N");
            } else if (notch > NOTCH_N) {
                labelNotchPos.setText(String.format("P%d", notch));
            } else {
                labelNotchPos.setText(String.format("B%d", notch *-1));
            }

            // キロ程演算方向
            if (moveTo == 0) {
                buttonSetDistanceUp.setEnabled(false);
                buttonSetDistanceDown.setEnabled(true);
            } else {
                buttonSetDistanceUp.setEnabled(true);
                buttonSetDistanceDown.setEnabled(false);
            }

            // レバーサ
            // ノッチがEB位置じゃないとレバーサいじれないよう変更
            if (canChangeReverser) {
                switch (reverser) {
                    case 0:
                        buttonReverserSetF.setEnabled(false);
                        buttonReverserSetN.setEnabled(true);
                        buttonReverserSetB.setEnabled(true);
                        break;

                    case 1:
                        buttonReverserSetF.setEnabled(true);
                        buttonReverserSetN.setEnabled(false);
                        buttonReverserSetB.setEnabled(true);
                        break;

                    case 2:
                        buttonReverserSetF.setEnabled(true);
                        buttonReverserSetN.setEnabled(true);
                        buttonReverserSetB.setEnabled(false);
                        break;
                
                    default:
                        break;
                }
            } else {
                buttonReverserSetF.setEnabled(false);
                buttonReverserSetN.setEnabled(false);
                buttonReverserSetB.setEnabled(false);
            }

            // モニタ類

            // ATS-P
            boolATS[ATS_P_ACTIVE] = limit != Integer.MAX_VALUE ? true : false;
            
            boolATS[ATS_P_NEAR_PATTERN] = false;
            if ((limit -5) < speed) {
                if( boolATS[ATS_P_ACTIVE] == true)
                    boolATS[ATS_P_NEAR_PATTERN] = true;
            }

            boolATS[ATS_P_BRAKE_OPERATING] = false;
            if (limit < speed) {
                if( boolATS[ATS_P_ACTIVE] == true)
                    boolATS[ATS_P_BRAKE_OPERATING] = true;
            }

            boolATS[ATS_P_BRAKE_OPERATING_EB] = false;
            if (!isATSPBrakeWorking) boolATS[ATS_P_BRAKE_OPERATING_EB] = true;

            // EB EB EB
            boolTrainStat[TRAINSTAT_EB] = false;
            if (isTE) boolTrainStat[TRAINSTAT_EB] = true;
            if (isRunningDoorOpen) boolTrainStat[TRAINSTAT_EB] = true;
            if (isEB) boolTrainStat[TRAINSTAT_EB] = true;
            if (!boolATS[ATS_POWER]) boolTrainStat[TRAINSTAT_EB] = true;
            if (reverser != 0) boolTrainStat[TRAINSTAT_EB] = true;
            if (boolATS[ATS_OPERATING]) boolTrainStat[TRAINSTAT_EB] = true;
            if (boolATS[ATS_P_BRAKE_OPERATING_EB]) boolTrainStat[TRAINSTAT_EB] = true;
            if (boolATS[ATS_P_ERROR]) boolTrainStat[TRAINSTAT_EB] = true;

            if (boolTrainStat[TRAINSTAT_EB]) sendCommand("send", "notch", NOTCH_EB);

            // 保安ブレーキ
            boolTrainStat[TRAINSTAT_DS_BRAKE] = false;
            if (mr < 700) boolTrainStat[TRAINSTAT_DS_BRAKE] = true;

            if (boolTrainStat[TRAINSTAT_DS_BRAKE]) sendCommand("send", "notch", NOTCH_EB);
            
            // TASC
            boolTASC[TASC_POWER] = isTASCEnable;
            
            boolTASC[TASC_PATTERN_ACTIVE] = false;
            boolTASC[TASC_BRAKE] = false;
            if (isTASCBraking) {
                boolTASC[TASC_POWER] = true;
                boolTASC[TASC_PATTERN_ACTIVE] = true;
                boolTASC[TASC_BRAKE] = true;
            }

            // ドア
            boolTrainStatEx[TRAINSTAT_EX_DOOR_CLOSE] = isDoorClose;

            boolTrainStatEx[TRAINSTAT_EX_STA] = isArrivingStation;

            txtTrainStat = "状態:";
            txtTrainStat += boolTrainStat[TRAINSTAT_PARKING] ? "駐車　" : "";
            txtTrainStat += boolTrainStat[TRAINSTAT_CONSTANT_SPEED] ? "定速　" : "";
            txtTrainStat += boolTrainStat[TRAINSTAT_DS_BRAKE] ? "保B　" : "";
            txtTrainStat += boolTrainStat[TRAINSTAT_SNOW_BRAKE] ? "雪B　" : "";
            txtTrainStat += boolTrainStat[TRAINSTAT_EMERG_SHORT] ? "非短　" : "";
            txtTrainStat += boolTrainStat[TRAINSTAT_THREE_PHASE] ? "三相　" : "";
            txtTrainStat += boolTrainStat[TRAINSTAT_EB] ? "非常　" : "";
            labelTrainStat.setText(txtTrainStat);

            txtATS = "ATS:";
            txtATS += boolATS[ATS_OPERATING] ? "ATS動作　" : "";
            txtATS += boolATS[ATS_POWER] ? "ATS電源　" : "";
            txtATS += boolATS[ATS_P_ERROR] ? "故障　" : "";
            txtATS += boolATS[ATS_P_ACTIVE] ? "ATS-P　" : "";
            txtATS += boolATS[ATS_P_BRAKE_RELEASE] ? "B解放　" : "";
            txtATS += boolATS[ATS_P_BRAKE_OPERATING] ? "B　" : "";
            txtATS += boolATS[ATS_P_NEAR_PATTERN] ? "ﾊﾟﾀﾝ接近　" : "";
            txtATS += boolATS[ATS_P_POWER] ? "P電源　" : "";
            txtATS += boolATS[ATS_P_BRAKE_OPERATING_EB] ? "非常　" : "";
            labelATS.setText(txtATS);

            txtTASC = "TASC:";
            txtTASC += boolTASC[TASC_POWER] ? "電源　" : "";
            txtTASC += boolTASC[TASC_PATTERN_ACTIVE] ? "ﾊﾟﾀﾝ　" : "";
            txtTASC += boolTASC[TASC_BRAKE] ? "B　" : "";
            txtTASC += boolTASC[TASC_OFF] ? "切　" : "";
            txtTASC += boolTASC[TASC_ERROR] ? "故障　" : "";
            labelTASC.setText(txtTASC);

            txtTrainStatEx = "";
            txtTrainStatEx += boolTrainStatEx[TRAINSTAT_EX_DOOR_CLOSE] ? "戸閉　" : "";
            txtTrainStatEx += boolTrainStatEx[TRAINSTAT_EX_EB] ? "EB装置　" : "";
            txtTrainStatEx += boolTrainStatEx[TRAINSTAT_EX_STA] ? "次駅接近　" : "";
            labelTrainStatEx.setText(txtTrainStatEx);
        });
    }

    private void handleArrivingStation() {
        // 次駅まで300m未満
        if (beaconGetedPos != 0f) {
            System.out.println(beaconGetedPos + 300f - move);
            System.out.println(speed * 3);

            // 次駅接近報知
            if (!isArrivingStation) {
                // 今の速度から駅までの予想停車距離を計算し、駅までの距離を上回ったら報知
                if ((speed * 3) > (beaconGetedPos + 300f - move)) {
                    isArrivingStation = true;
                }
            }
        }

        // 所定停目付近に停車か、100m以上通過した場合解除
        if ((0 >= (beaconGetedPos + 300f - move + 100f)) || (!isRunningTrain() && (5f >= (beaconGetedPos + 300f - move)))) {
            isArrivingStation = false;
            beaconGetedPos = 0f;
        }
    }

    private void handleSafety() {
        // TE装置解除用
        if ((isTE) && (notch != NOTCH_EB)) {
            isTE = false;
        }

        // EB装置解除用
        if ((isEB) && (notch != NOTCH_EB)) {
            boolTrainStatEx[TRAINSTAT_EX_EB] = false;
            isEB = false;
            ebTimer.stop();
            ebActiveTimer.stop();
        }

        // EB装置
        if (isRunningTrain()) {
            if (prevNotch == notch) {
                if (!ebTimer.isRunning()) ebTimer.start();
            } else {
                if (ebActiveTimer.isRunning()) {
                    boolTrainStatEx[TRAINSTAT_EX_EB] = false;
                }
                ebTimer.stop();
                ebActiveTimer.stop();
                isEB = false;
            }
        } else {
            if (ebActiveTimer.isRunning()) {
                boolTrainStatEx[TRAINSTAT_EX_EB] = false;
            }
            ebTimer.stop();
            ebActiveTimer.stop();
            isEB = false;
        }

        // 走行中戸開時 
        if (isRunningTrain() && !isDoorClose) {
            isRunningDoorOpen = true;
        } else {
            isRunningDoorOpen = false;
        }

        if (boolATS[ATS_P_BRAKE_OPERATING] && (bc < 200)) {
            if (isATSPBrakeWorking) {
                ATSPBrakeNWC ++;
                if (ATSPBrakeNWC > ATSP_BRAKE_NWC_TIME) {
                    isATSPBrakeWorking = false;
                }
            }
        } else {
            ATSPBrakeNWC = 0;
            isATSPBrakeWorking = true;
        }
    }

    public void running() throws IOException {
        this.clientInit(PORT);
        SwingUtilities.invokeLater(this::setupUI);

        System.out.println("client starting on port " + PORT + "...");

        // EB装置関連
        ebTimer = new Timer(60000, _ -> {
            boolTrainStatEx[TRAINSTAT_EX_EB] = true;
            ebActiveTimer.start();
            ebTimer.stop();
        });

        ebActiveTimer = new Timer(5000, _ -> {
            boolTrainStatEx[TRAINSTAT_EX_EB] = true;
            isEB = true;
            ebTimer.stop();
            ebActiveTimer.stop();
        });

        new Thread(() -> {
            while(true) {
                String fetchData = this.clientReciveString();

                if(fetchData != null) {
                    JSONObject jsonObj = new JSONObject(fetchData);
                    switch (jsonObj.getString("type")) {
                        case "send":
                            // 受信
                            prevId = id;
                            prevNotch = notch;
                            id = jsonObj.getInt("id");
                            speed = jsonObj.getFloat("speed");
                            notch = jsonObj.getInt("notch");
                            door = jsonObj.getInt("door");
                            bc = jsonObj.getInt("bc");
                            mr = jsonObj.getInt("mr");
                            move = jsonObj.getFloat("move");
                            moveTo = jsonObj.getInt("moveTo");
                            reverser = jsonObj.getInt("reverser");
                            limit = jsonObj.getInt("speedLimit");
                            isTASCEnable = jsonObj.getBoolean("isTASCEnable");
                            isTASCBraking = jsonObj.getBoolean("isTASCBraking");

                            if (prevId != id) {
                                boolTrainStat = new boolean[]{false, false, false, false, false, false, false};
                                boolATS = new boolean[]{false, true, false, false, false, false, false, true, false};
                                boolTASC = new boolean[]{true, false, false, false, false};
                                boolTrainStatEx = new boolean[]{false, false, false};
                            }

                            // ドア閉めるとき時間差で表示
                            if (door == 0) {
                                if (!isDoorClose) {
                                    doorCloseCount ++;
                                    if (doorCloseCount > DOOR_CLOSE_TIME) {
                                        isDoorClose = true;
                                    }
                                }
                            } else {
                                doorCloseCount = 0;
                                isDoorClose = false;
                            }

                            handleSafety();
                            handleArrivingStation();

                            // GUI更新
                            if (!refreshTimer.isRunning()) refreshTimer.start();

                            System.out.println(String.format("speed:%.2fkm/h notch:%d door:%d bc:%d mr:%d move:%.2f", speed, notch, door, bc, mr, move));
                            break;

                        case "kill":
                            this.clientClose();
                            System.exit(0);
                            break;

                        case "notRidingTrain":
                            // 列車に載ってない
                            labelSpeed.setText("N/A");
                            labelBC.setText("N/A");
                            labelMR.setText("N/A");
                            labelDistance.setText("N/A");
                            labelNotchPos.setText("N/A");
                            buttonReverserSetF.setEnabled(false);
                            buttonReverserSetN.setEnabled(false);
                            buttonReverserSetB.setEnabled(false);
                            labelTrainStat.setText("");
                            labelATS.setText("");
                            labelTASC.setText("");
                            labelTrainStatEx.setText("");
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
                                    if (signal_1 == 1) beaconGetedPos = move;
                                    if (signal_1 == 2) {
                                        beaconGetedPos = 0f;
                                        isArrivingStation = false;
                                    }
                                    break;
                            
                                default:
                                    break;
                            }
                            break;

                        case "start":
                            System.out.println(fetchData);
                            break;
                        
                        default:
                            break;
                    }
                } else {
                    this.clientInit(PORT);
                }
            }
        }).start();
    }
}