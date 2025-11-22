package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.BaseController;

public class UD01AB extends BaseController 
{
    @FXML private Button btnS00AB;
    @FXML private Button btnD00AA;
    @FXML private Button btnD01AA;
    @FXML private Button btnX1;
    @FXML private Label title;
    @FXML private Label result;
    
    Timeline timeline;

    @Override
    public void init(TDCCore core)
    {
        super.init(core);

        timeline = new Timeline
        (
            new KeyFrame
            (
                Duration.millis(200),   // 更新間隔（ms）
                e -> update()
            )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        update();
    }
    
    @FXML
    public void initialize()
    {
        btnS00AB.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/S00AB.fxml"));
        btnS00AB.setText("初期\n選択");
        btnD00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D00AA.fxml"));
        btnD00AA.setText("運転士\nメニュー");
        btnD01AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D01AA.fxml"));
        btnD01AA.setText("運転情報\n画面");
        title.getTransforms().add(new Scale(2, 1, 0, 0));
    }

    
    @Override
    protected void onReady()
    {
        tu.setCurrentController(this);

        Path path = Paths.get("card.csv");
        Charset cs = Charset.forName("UTF-8");
        try {
            repo.load(path, cs);
            result.setText("読込みました。");
            btnX1.setText("行路選択");
            btnX1.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/UD01AC.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            result.setText("読込みに失敗しました。");
        }
    }

    private void update()
    {
        if (core != null && core.tc != null)
        {
            
        }
    }

    private void goNext(String fxml)
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent child = loader.load();

            Object obj = loader.getController();
            if (obj instanceof BaseController bc)
            {
                bc.init(core);
                bc.setContainer(this.container);
            }

            if (timeline != null)
            {
                timeline.stop();
            }

            setScreen(child);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
