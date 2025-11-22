package jp.akidukisystems.software.utilty;

import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;


public class ScreenManager{
	private int sizex;
	private int sizey;

	public static void main(String[] args) throws InterruptedException {
		ScreenManager sm = new ScreenManager();
		sm.init(1280, 720);
	}

	public void init(int p1, int p2)
	{
		JFrame f = new JFrame();

		sizex = p1;
		sizey = p2;

		//サイズ指定
		f.setSize(sizex,sizey);

		//ウィンドウ枠を削除
		f.setUndecorated(true);

		//画面表示
		f.setVisible(true);

		//レイアウト指定
		f.setLayout(new FlowLayout());

		//文字列表示
		JLabel label = new JLabel("ゲームタイトルだよ!!\n(何かキーを押すとプログラム終了!!)");
		label.setFont(new Font(Font.MONOSPACED,Font.BOLD, 20));
		f.add(label);

		//何かキーを押すと終了
		f.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				System.exit(0);
			}
		});

		//一応閉じるで終了も入れとく
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//画面デバイスを取得
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		
		//JFrameをフルスクリーンに
		gd.setFullScreenWindow(f);

		//設定できる解像度オブジェクトを全取得
		DisplayMode[] modeList = gd.getDisplayModes();

		//ウィンドウサイズと同じ解像度モードを取得
		DisplayMode activeMode = null;
		for ( DisplayMode mode : modeList ) {
			System.out.println(mode);
			if ( mode.getWidth() == sizex && mode.getHeight() == sizey &&
					((activeMode == null)
					|| activeMode.getBitDepth() < mode.getBitDepth()
					|| activeMode.getBitDepth() == mode.getBitDepth() && activeMode.getRefreshRate() <= mode.getRefreshRate())) {
				activeMode = mode;
			}
		}

		//解像度の変更
		if ( activeMode!=null ) {
			gd.setDisplayMode(activeMode);
		} else {
			System.out.println("解像度変更に失敗しました。。。\n他のサイズなら成功するかもしれません。");
		}
	}
}