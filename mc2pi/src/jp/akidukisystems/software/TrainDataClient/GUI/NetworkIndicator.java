package jp.akidukisystems.software.TrainDataClient.GUI;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.Timer;

public class NetworkIndicator extends JPanel
{
    private static final int FLASH_DURATION = 100; // ms

    private Color color = Color.DARK_GRAY;
    private long flashUntil = 0;

    public enum TRTYPE {SEND, RECIEVE};

    public NetworkIndicator()
    {
        new Timer(20, e -> update()).start();
    }

    public void flash(TRTYPE type)
    {
        switch (type) {
            case TRTYPE.SEND:
                color = Color.GREEN;
                break;

            case TRTYPE.RECIEVE:
                color = Color.ORANGE;
                break;
        
            default:
                break;
        }
        flashUntil = System.currentTimeMillis() + FLASH_DURATION;
        repaint();
    }

    private void update()
    {
        long now = System.currentTimeMillis();
        if (now > flashUntil)
        {
            if (color != Color.DARK_GRAY)
            {
                color = Color.DARK_GRAY;
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(color);
        g.fillOval(0, 0, getWidth(), getHeight());
    }
}
