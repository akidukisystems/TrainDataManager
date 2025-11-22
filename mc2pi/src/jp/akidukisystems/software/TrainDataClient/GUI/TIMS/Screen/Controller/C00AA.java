package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

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

public class C00AA extends BaseController 
{
    @FXML private Button btnS00AB;
    @FXML private Button btnC01AA;
    @FXML private Label title;

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
        title.getTransforms().add(new Scale(2, 1, 0, 0));
    }

    private void update()
    {
        if (core != null && core.tc != null)
        {
            
        }
    }

    @Override
    protected void onReady()
    {
        tu.setCurrentController(this);
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
