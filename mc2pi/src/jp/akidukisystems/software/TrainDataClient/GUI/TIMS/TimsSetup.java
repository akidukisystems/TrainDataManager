package jp.akidukisystems.software.TrainDataClient.GUI.TIMS;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TimsSetup extends Application
{
    public void init()
    {
        
    }

    /* テスト用 */
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource
            (
                "/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/S00AB.fxml"
            )
        );

        Scene scene = new Scene(root, 1600, 900); // ← TIMSサイズ
        stage.setScene(scene);
        scene.getStylesheets().add(
            getClass().getResource("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/style.css").toExternalForm()
        );
        stage.setTitle("TIMS");
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}
