package jp.akidukisystems.software.TrainDataClient.GUI.TIMS;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.TrainControl;

public class TimsSetup extends Application
{
    private static TDCCore staticCore;   // ★ 追加

    private TDCCore core;
    private TrainControl tc;

    public static void setCore(TDCCore core)
    {
        staticCore = core;
    }

    @Override
    public void init()
    {
        this.core = staticCore;
        this.tc = (core != null) ? core.tc : null;
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader
        (
            getClass().getResource
            (
                "/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/S00AB.fxml"
            )
        );

        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof BaseController c)
        {
            c.init(core);  // ← もう null じゃない
        }

        Scene scene = new Scene(root, 1600, 900);
        scene.getStylesheets().add
        (
            getClass().getResource
            (
                "/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/style.css"
            ).toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle("TIMS");
        stage.show();
    }
}
