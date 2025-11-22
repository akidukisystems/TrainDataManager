package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.transform.Scale;

public class S00AB extends BaseController
{
    @FXML private Button btnD00AA;
    @FXML private Button btnC00AA;
    @FXML private Button btnUA00AA;
    @FXML private Label title;

     @Override
    public void init(TDCCore core)
    {
        super.init(core);
    }

    @FXML
    public void initialize()
    {
        btnD00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D00AA.fxml"));
        btnC00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/C00AA.fxml"));
        btnUA00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/UA00AA.fxml"));
        title.getTransforms().add(new Scale(2, 1, 0, 0));
    }

    @Override
    protected void onReady()
    {
        tu.setCurrentController(this);
    }

    private void goNext(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent child = loader.load();

            Object obj = loader.getController();
            if (obj instanceof BaseController bc) {
                bc.init(core);
                bc.setContainer(this.container);
            }

            // Stage の Scene に直接 setRoot する代わりに
            setScreen(child);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
