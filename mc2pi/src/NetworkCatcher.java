import java.io.*;
import java.net.*;
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    public static Socket clientSocket;
    Socket client;
    BufferedReader reader = null;
    PrintWriter writer = null;
    Socket c2s = null;

    float speed = 0f;
    int notch = 0;
    int door = 0;
    int bc = 0;
    int mr = 0;
    float move = 0f;

    String distanceSetText = "0";

    String buttonCommand = null;
    int buttonDo = -1;

    Timer blinkTimer;

    JLabel labelSpeed;
    JLabel labelBC;
    JLabel labelMR;
    JLabel labelDistance;
    JLabel labelNotchPos;

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

    public void running() throws IOException {
        this.clientInit(PORT);
        
        SwingUtilities.invokeLater(() -> {
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

            JButton buttonDoorOpenL = new JButton("左ドア開");
            buttonDoorOpenL.setBounds(0, 50, 100, 50);

            JButton buttonDoorOpenR = new JButton("右ドア開");
            buttonDoorOpenR.setBounds(100, 50, 100, 50);

            JButton buttonDoorClose = new JButton("ドア閉");
            buttonDoorClose.setBounds(100, 0, 100, 50);

            JButton buttonAction = new JButton("設定");
            buttonAction.setBounds(200, 0, 200, 100);

            JButton buttonResetDistance = new JButton("キロ程リセット");
            buttonResetDistance.setBounds(0, 100, 150, 50);

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

            labelSpeed = new JLabel("N/A");
            labelSpeed.setFont(new Font("Arial", Font.PLAIN, 20));
            labelSpeed.setForeground(Color.BLACK);
            labelSpeed.setBounds(0, 150, 100, 20);

            labelBC = new JLabel("N/A");
            labelBC.setFont(new Font("Arial", Font.PLAIN, 20));
            labelBC.setForeground(Color.BLACK);
            labelBC.setBounds(100, 150, 75, 20);

            labelMR = new JLabel("N/A");
            labelMR.setFont(new Font("Arial", Font.PLAIN, 20));
            labelMR.setForeground(Color.BLACK);
            labelMR.setBounds(175, 150, 75, 20);

            labelDistance = new JLabel("N/A");
            labelDistance.setFont(new Font("Arial", Font.PLAIN, 20));
            labelDistance.setForeground(Color.BLACK);
            labelDistance.setBounds(250, 150, 100, 20);

            labelNotchPos = new JLabel("N/A");
            labelNotchPos.setFont(new Font("Arial", Font.PLAIN, 20));
            labelNotchPos.setForeground(Color.BLACK);
            labelNotchPos.setBounds(350, 150, 50, 20);

            buttonTE.addActionListener(_ -> sendCommand("send", "notch", NOTCH_MAX));
            buttonDoorOpenL.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (buttonDo == DOOR_LEFT) {
                        buttonCommand = null;
                        buttonDo = -1;
                        blinkTimer.stop();
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
                    if (buttonDo == DOOR_RIGHT) {
                        buttonCommand = null;
                        buttonDo = -1;
                        blinkTimer.stop();
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
                    if (buttonDo == DOOR_CLOSE) {
                        buttonCommand = null;
                        buttonDo = -1;
                        blinkTimer.stop();
                    } else {
                        buttonCommand = "door";
                        buttonDo = DOOR_CLOSE;
                        blinkTimer.start();
                    }
                }
            });
            buttonResetDistance.addActionListener(_ -> distanceReset.setVisible(true));
            buttonAction.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println(buttonCommand);
                    System.out.println(buttonDo);
                    switch (buttonCommand) {
                        case "door":
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
            p.add(buttonResetDistance);
            p.add(buttonAction);

            p.add(labelSpeed);
            p.add(labelBC);
            p.add(labelMR);
            p.add(labelDistance);
            p.add(labelNotchPos);

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

            buttonAction.setBackground(Color.WHITE);
            buttonDoorOpenL.setBackground(Color.WHITE);
            buttonDoorOpenR.setBackground(Color.WHITE);
            buttonDoorClose.setBackground(Color.WHITE);

            frame.getContentPane().add(p, BorderLayout.CENTER);
            distanceReset.getContentPane().add(q, BorderLayout.CENTER);

            // 表示
            frame.setVisible(true);
            distanceReset.setVisible(false);
        });

        System.out.println("client starting on port " + PORT + "...");

        new Thread(() -> {
            while(true) {
                String fetchData = this.clientReciveString();

                if(fetchData != null) {
                    JSONObject jsonObj = new JSONObject(fetchData);
                    switch (jsonObj.getString("type")) {
                        case "send":
                            speed = jsonObj.getFloat("speed");
                            notch = jsonObj.getInt("notch");
                            door = jsonObj.getInt("door");
                            bc = jsonObj.getInt("bc");
                            mr = jsonObj.getInt("mr");
                            move = jsonObj.getFloat("move");

                            SwingUtilities.invokeLater(() -> {
                                labelSpeed.setText(String.format("%dkm/h", (int) speed));
                                labelBC.setText(String.format("%dkpa", bc));
                                labelMR.setText(String.format("%dkpa", mr));
                                labelDistance.setText(String.format("%.1fkm", move / 1000f));
                                
                                if (notch == NOTCH_EB) {
                                    labelNotchPos.setText("EB");
                                } else if (notch == NOTCH_N) {
                                    labelNotchPos.setText("N");
                                } else if (notch > NOTCH_N) {
                                    labelNotchPos.setText(String.format("P%d", notch));
                                } else {
                                    labelNotchPos.setText(String.format("B%d", notch *-1));
                                }
                            });

                            System.out.println(String.format("speed:%.2fkm/h notch:%d door:%d bc:%d mr:%d move:%.2f", speed, notch, door, bc, mr, move));
                            break;

                        case "kill":
                            this.clientClose();
                            System.exit(0);
                            break;

                        case "notRidingTrain":
                                labelSpeed.setText("N/A");
                                labelBC.setText("N/A");
                                labelMR.setText("N/A");
                                labelDistance.setText("N/A");
                                labelNotchPos.setText("N/A");
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