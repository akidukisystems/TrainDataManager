package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.transform.Scale;

public class S00AB
{
    @FXML private Button btnD00AA;
    @FXML private Button btnC00AA;
    @FXML private Button btnUA00AA;
    @FXML private Label title;

    @FXML
    public void initialize() {
        btnD00AA.setOnAction(e -> goNext("driver.fxml"));
        btnC00AA.setOnAction(e -> goNext("attendant.fxml"));
        btnUA00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/UA00AA.fxml"));
        title.getTransforms().add(new Scale(2, 1, 0, 0));
    }

    private void goNext(String fxml) {
        try {
            Stage stage = (Stage) btnD00AA.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
