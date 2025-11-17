package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class UA00AA
{
    @FXML private Button btnS00AB;
    @FXML private Label title;

    @FXML
    public void initialize() {
        btnS00AB.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/S00AB.fxml"));
        btnS00AB.setText("初期\n選択");
        title.getTransforms().add(new Scale(2, 1, 0, 0));
    }

    private void goNext(String fxml) {
        try {
            Stage stage = (Stage) btnS00AB.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
