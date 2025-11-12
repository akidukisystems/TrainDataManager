package jp.akidukisystems.software.MasconBridge.GUI;

import javax.swing.*;
import java.awt.*;

public class Indicator extends JPanel
{
    private int notch = 0; // 現在ノッチ値 (-8～5想定)

    public void setNotch(int notch)
    {
        this.notch = notch;
        repaint();
    }

    public void increaseNotch()
    {
        if(notch < 5)
            notch ++;
        repaint();
    }

    public void decreaseNotch()
    {
        if(notch > -8)
            notch --;
        repaint();
    }

    public void repaintHandler()
    {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 背景
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);

        g2.setFont(new Font("Arial", Font.BOLD, 14));

        // 段数（-8～5）
        int min = -8;
        int max = 5;
        int total = max - min + 1;
        int step = h / total;

        // 目盛り
        for (int i = min; i <= max; i++)
        {
            int y = (int) ((i - min) * step + step / 2.0);

            if(i == notch)
                g2.setStroke(new BasicStroke(3));
            else
                g2.setStroke(new BasicStroke(1));

            switch (i)
            {
                case 0:
                    g2.setColor(Color.BLACK);
                    break;

                case -6:
                    g2.setColor(Color.ORANGE.darker());
                    break;

                case -8:
                    g2.setColor(Color.RED);
                    break;
            
                default:
                    if(i > 0)
                        g2.setColor(Color.GREEN.darker());
                    else
                        g2.setColor(Color.YELLOW.darker());
                    break;
            }
            g2.drawLine(0, y, w, y);

            if(i == notch)
            {
                // 現在ノッチ表示
                int ny = (int) ((notch - min) * step);
                g2.fillRoundRect(w / 2 - 20, ny + step / 4, 40, step / 2, 8, 8);
            }
            
            switch (i)
            {               
                case 0:
                    g2.drawString("N", 5, y - 2);
                    break;

                case -8:
                    g2.drawString("EB", 5, y - 2);
                    break;
            
                default:
                    if(i > 0)
                        g2.drawString("P"+ String.valueOf(i), 5, y - 2);
                    else
                        g2.drawString("B"+ String.valueOf(i *-1), 5, y - 2);
                    break;
            }
        }
    }
}
