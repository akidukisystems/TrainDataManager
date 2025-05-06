import java.io.*;
import java.net.*;
import javax.swing.*;

import java.awt.BorderLayout;
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
            frame.setSize(300, 300);

            JButton buttonTE = new JButton("TE装置");
            buttonTE.setBounds(0, 0, 100, 50);

            JButton buttonDoorOpenL = new JButton("左ドア開");
            buttonDoorOpenL.setBounds(0, 50, 100, 50);

            JButton buttonDoorOpenR = new JButton("右ドア開");
            buttonDoorOpenR.setBounds(100, 50, 100, 50);

            JButton buttonDoorClose = new JButton("ドア閉");
            buttonDoorClose.setBounds(100, 0, 100, 50);

            JButton buttonResetDistance = new JButton("キロ程リセット");
            buttonResetDistance.setBounds(0, 100, 100, 50);

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
                        clientObject.clientSendString("{\"type\":\"send\",\"doAny\":\"distance\",\"move\":0.0}");
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
                        System.out.println(String.format("speed:%.2fkm/h notch:%d door:%d bc:%d mr:%d move:%.2f", jsonObj.getFloat("speed"), jsonObj.getInt("notch"), jsonObj.getInt("door"), jsonObj.getInt("bc"), jsonObj.getInt("mr"), jsonObj.getFloat("move")));
                        break;

                    case "kill":
                        clientObject.clientClose();
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
        try {
            this.client = new Socket("localhost", port);
            writer = new PrintWriter(client.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String clientReciveString() throws IOException {
        try {
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clientSendString(String data) throws IOException {
        try {
            writer.println(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientClose() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (c2s != null) c2s.close();
            if (client != null) client.close();
            isAlive = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}