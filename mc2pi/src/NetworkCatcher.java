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
    public static Socket clientSocket;
    Socket client;
    BufferedReader reader = null;
    PrintWriter writer = null;
    Socket c2s = null;
    Boolean isAlive = false;

    float speed = 0f;
    int notch = 0;
    int door = 0;
    int bc = 0;
    int mr = 0;
    float move = 0f;

    JLabel labelSpeed;
    JLabel labelBC;
    JLabel labelMR;
    JLabel labelDistance;
    JLabel labelNotchPos;

    public void main(String[] args) throws IOException {
        NetworkCatcher clientObject = new NetworkCatcher();
        clientObject.clientInit(PORT);
        isAlive = true;
        
        SwingUtilities.invokeLater(() -> {
            // フレーム（ウィンドウ）を作成
            JPanel p = new JPanel();
            p.setLayout(null);

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

            JButton buttonResetDistance = new JButton("キロ程リセット");
            buttonResetDistance.setBounds(0, 100, 150, 50);

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

            buttonTE.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        clientObject.clientSendString("{\"type\":\"send\",\"doAny\":\"notch\",\"notch\":-7}");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            buttonDoorOpenL.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        clientObject.clientSendString("{\"type\":\"send\",\"doAny\":\"door\",\"door\":2}");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            buttonDoorOpenR.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        clientObject.clientSendString("{\"type\":\"send\",\"doAny\":\"door\",\"door\":1}");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            buttonDoorClose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        clientObject.clientSendString("{\"type\":\"send\",\"doAny\":\"door\",\"door\":0}");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            buttonResetDistance.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        clientObject.clientSendString("{\"type\":\"send\",\"doAny\":\"move\",\"move\":0.0}");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });


            p.add(buttonTE);
            p.add(buttonDoorOpenL);
            p.add(buttonDoorOpenR);
            p.add(buttonDoorClose);
            p.add(buttonResetDistance);

            p.add(labelSpeed);
            p.add(labelBC);
            p.add(labelMR);
            p.add(labelDistance);
            p.add(labelNotchPos);

            frame.getContentPane().add(p, BorderLayout.CENTER);

            // 表示
            frame.setVisible(true);
        });

        System.out.println("client starting on port " + PORT + "...");
        String fetchData;

        while(true) {
            fetchData = clientObject.clientReciveString();

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

                        labelSpeed.setText(String.format("%dkm/h", (int) speed));
                        labelBC.setText(String.format("%dkpa", bc));
                        labelMR.setText(String.format("%dkpa", mr));
                        labelDistance.setText(String.format("%.1fkm", move / 1000f));
                        
                        if (notch == -8) {
                            labelNotchPos.setText("EB");
                        } else if (notch == 0) {
                            labelNotchPos.setText("N");
                        } else if (notch > 0) {
                            labelNotchPos.setText(String.format("P%d", notch));
                        } else {
                            labelNotchPos.setText(String.format("B%d", notch *-1));
                        }

                        System.out.println(String.format("speed:%.2fkm/h notch:%d door:%d bc:%d mr:%d move:%.2f", speed, notch, door, bc, mr, move));
                        break;

                    case "kill":
                        clientObject.clientClose();
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

                if(!isAlive) break;
            } else {
                clientObject.clientInit(PORT);
            }
        }

        System.out.println("end");
        while (true) {
            ;
        }
    }

    public void clientInit(int port) {
        while (true) {
            try {
                this.client = new Socket("localhost", port);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(this.client != null) break;
        }
        try {
            this.writer = new PrintWriter(this.client.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String clientReciveString() throws IOException {
        try {
            return this.reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clientSendString(String data) throws IOException {
        try {
            this.writer.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientClose() {
        try {
            if (this.reader != null) this.reader.close();
            if (this.writer != null) this.writer.close();
            if (this.c2s != null) this.c2s.close();
            if (this.client != null) this.client.close();
            isAlive = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}