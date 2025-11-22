package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.BaseController;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader;

public class D13AA extends BaseController 
{
    @FXML private Button btnS00AB;
    @FXML private Button btnD00AA;
    @FXML private Button btnD01AA;

    @FXML private Button btnNext;

    @FXML private GridPane gridPane;

    @FXML private Label title;
    @FXML private Label result;
    
    Timeline timeline;
    Timeline blink;
    
    List<Button> trainButtons;
    private Button selectedButton = null;

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
        btnNext.setOnAction(e -> {
            if(selectedButton != null) {
                // 列車番号を更新したりする　これで上手く行くかは不明
                String stationEntry = om.unformatStationName(selectedButton.getText());

                om.setStation(repo.getStationByName(stationEntry));
                om.setLine(repo.getLine(repo.getStationByName(stationEntry).lineId));

                nm.sendCommand("send", "move", 1000f * repo.getStationByName(stationEntry).linePost);
                
                switch (om.getDirection()) {
                    case UP:
                        nm.sendCommand("send", "moveTo", 0);
                        break;
                    
                    case DOWN:
                        nm.sendCommand("send", "moveTo", 1);
                        break;
                
                    default:
                        break;
                }



                goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D00AA.fxml");
            }
        });

        title.getTransforms().add(new Scale(2, 1, 0, 0));

        trainButtons = new ArrayList<>();

        blink = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            if (selectedButton != null) {
                if (btnNext.getStyleClass().contains("blink")) {
                    btnNext.getStyleClass().remove("blink");
                } else {
                    btnNext.getStyleClass().add("blink");
                }
            } else {
                btnNext.getStyleClass().remove("blink");
            }
        }));
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();
    }

    
    @Override
    protected void onReady()
    {
        tu.setCurrentController(this);
        
        int r = 0;
        int c = 0;
        int maxRows = 5; // 行数

        for (DutyCardReader.Station station : repo.getStations())
        {
            Button b = new Button(om.formatStationName(station.name));
            b.getStyleClass().add("normal-button");
            b.getStyleClass().add("tims-button");
            b.setPrefWidth(150);
            b.setPrefHeight(50);

            trainButtons.add(b);

            GridPane.setRowIndex(b, r);
            GridPane.setColumnIndex(b, c);
            gridPane.getChildren().add(b);

            // 次のセルに移動
            r++;
            if (r >= maxRows) {
                r = 0;
                c++;
            }
        }

        for (Button s : trainButtons)
        {
            setupToggle(s);
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

    private void setupToggle(Button btn) {
        btn.setOnAction(event -> {
            // 同じボタンが押された → 解除
            if (selectedButton == btn) {
                btn.getStyleClass().remove("selected-button");
                selectedButton = null;
                return;
            }

            // ほかのボタンが選択されていた → そっちを解除
            if (selectedButton != null) {
                selectedButton.getStyleClass().remove("selected-button");
            }

            // 今押したボタンを選択状態に
            btn.getStyleClass().add("selected-button");
            selectedButton = btn;
        });
    }
}
