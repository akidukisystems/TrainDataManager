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
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader;

public class UD01AC extends BaseController 
{
    @FXML private Button btnS00AB;
    @FXML private Button btnD00AA;
    @FXML private Button btnD01AA;

    @FXML private Button btnX1;
    @FXML private Button btnX2;
    @FXML private Button btnX3;
    @FXML private Button btnX4;
    @FXML private Button btnX5;
    @FXML private Button btnX6;
    @FXML private Button btnX7;
    @FXML private Button btnX8;
    @FXML private Button btnX9;
    @FXML private Button btnX10;

    @FXML private Label textX1;
    @FXML private Label textX2;
    @FXML private Label textX3;
    @FXML private Label textX4;
    @FXML private Label textX5;
    @FXML private Label textX6;
    @FXML private Label textX7;
    @FXML private Label textX8;
    @FXML private Label textX9;
    @FXML private Label textX10;

    @FXML private Button btnNext;

    @FXML private Label title;
    @FXML private Label result;
    
    Timeline timeline;
    Timeline blink;
    private Button[] trainButtons;
    private Label[] trainTexts;
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
                int trainNumberEntry = Integer.parseInt(selectedButton.getText());

                om.setTrainNumber(repo.getTrainNumber(trainNumberEntry));
                om.setDirection(repo.getDirectionOfTimeTable(trainNumberEntry));
                om.setTimeTable(repo.getTimeTable(repo.getTrainNumber(trainNumberEntry).timeTableId));

                tn.setAlphabet(repo.getTrainNumber(trainNumberEntry).numberStr);
                tn.setNumber(""+ repo.getTrainNumber(trainNumberEntry).numberInt);
                tn.setHalf(tn.getNumber() + tn.getAlphabet());

                goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D13AA.fxml");
            }
        });
        title.getTransforms().add(new Scale(2, 1, 0, 0));

        trainButtons = new Button[]
        {
            btnX1,
            btnX2,
            btnX3,
            btnX4,
            btnX5,
            btnX6,
            btnX7,
            btnX8,
            btnX9,
            btnX10
        };

        trainTexts = new Label[]
        {
            textX1,
            textX2,
            textX3,
            textX4,
            textX5,
            textX6,
            textX7,
            textX8,
            textX9,
            textX10
        };

        for (Button s : trainButtons)
        {
            setupToggle(s);
        }

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
        
        if(repo != null)
        {
            int i = 0;
            for (DutyCardReader.TrainNumber s : repo.getTrainNumbers())
            {
                if (i >= trainButtons.length) break; // ボタンが足りない場合は打ち切り
                trainButtons[i].setText(""+ (i+1));
                trainTexts[i].setText(s.numberInt +""+ s.numberStr +" "+ repo.getTrainType(s.trainTypeId).nameJa +" "+ repo.getFirstStationByTrainNumber(s).name +"→"+ repo.getLastStationByTrainNumber(s).name);
                i ++;
            }
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
