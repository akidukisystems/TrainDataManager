package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

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

public class D01AA extends BaseController 
{
    @FXML private Button btnS00AB;
    @FXML private Button btnD00AA;
    @FXML private Button btnD01AA;
    @FXML private Button btnB00AA;

    @FXML private Button btnNext;
    @FXML private Button btnSenbetsu;

    @FXML private GridPane gridPane;

    @FXML private Label labelTitle;
    @FXML private Label labelTrainNumber;

    @FXML private Label labelPpassing;
    @FXML private Label labelPtrainNumberPassing;
    @FXML private Label labelPtrainNumber;
    @FXML private Label labelMusenCh;

    @FXML private Label labelTime;
    @FXML private Label labelSpeed;
    @FXML private Label labelKiloPost;

    @FXML private Label labelLine1Sta;
    @FXML private Label labelLine1Arrive;
    @FXML private Label labelLine1ArriveSec;
    @FXML private Label labelLine1Depart;
    @FXML private Label labelLine1DepartSec;
    @FXML private Label labelLine1Track;
    @FXML private Label labelLine1Limit;

    @FXML private Label labelLine2Sta;
    @FXML private Label labelLine2Arrive;
    @FXML private Label labelLine2ArriveSec;
    @FXML private Label labelLine2Depart;
    @FXML private Label labelLine2DepartSec;
    @FXML private Label labelLine2Track;
    @FXML private Label labelLine2Limit;

    @FXML private Label labelLine3Sta;
    @FXML private Label labelLine3Arrive;
    @FXML private Label labelLine3ArriveSec;
    @FXML private Label labelLine3Depart;
    @FXML private Label labelLine3DepartSec;
    @FXML private Label labelLine3Track;
    @FXML private Label labelLine3Limit;
    
    Timeline timeline;
    Timeline blink;
    
    private Button selectedButton = null;

    private Label[] labelLine1;
    private Label[] labelLine2;
    private Label[] labelLine3;

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
        labelLine1 = new Label[]
        {
            labelLine1Sta,
            labelLine1Arrive,
            labelLine1Depart
        };

        labelLine2 = new Label[]
        {
            labelLine2Sta,
            labelLine2Arrive,
            labelLine2Depart
        };

        labelLine3 = new Label[]
        {
            labelLine3Sta,
            labelLine3Arrive,
            labelLine3Depart
        };

        btnS00AB.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/S00AB.fxml"));
        btnS00AB.setText("初期\n選択");
        btnD00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D00AA.fxml"));
        btnD00AA.setText("運転士\nメニュー");
        btnD01AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D01AA.fxml"));
        btnD01AA.setText("運転情報\n画面");
        btnB00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/B00AA.fxml"));
        btnB00AA.setText("応急マニ\nュアル");
        btnNext.setOnAction(e ->
        {
            if(selectedButton != null)
            {
                selectedButton.getStyleClass().remove("selected-button");
                selectedButton = null;
                
                om.setPassing(!om.isPassing());
                if(om.isPassing())
                {
                    labelPpassing.setText(" 通過設定 ");
                    labelPpassing.getStyleClass().add("P-passing");
                    labelPtrainNumberPassing.setText("通");
                    labelPtrainNumberPassing.getStyleClass().add("plane-text");
                }
                else
                {
                    labelPpassing.setText("");
                    labelPpassing.getStyleClass().remove("P-passing");
                    labelPtrainNumberPassing.setText("");
                    labelPtrainNumberPassing.getStyleClass().remove("plane-text");
                }
            }
        });

        labelTitle.getTransforms().add(new Scale(2, 1, 0, 0));
        labelTrainNumber.getTransforms().add(new Scale(2, 1, 0, 0));

        for(Label s : labelLine1)
        {
            s.getTransforms().add(new Scale(2, 1, 0, 0));
        }

        for(Label s : labelLine2)
        {
            s.getTransforms().add(new Scale(2, 1, 0, 0));
        }


        for(Label s : labelLine3)
        {
            s.getTransforms().add(new Scale(2, 1, 0, 0));
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
        
        setupToggle(btnSenbetsu);

        String trainNumberFull = tn.getFull();
        String trainNumber = tn.getHalf();

        if(trainNumberFull == null)
        {
            trainNumberFull = trainNumber;
        }

        if(tn.getHalf() != null)
        {
            labelTrainNumber.setText(om.toZenkaku(trainNumberFull));
            labelPtrainNumber.setText(om.toZenkaku(trainNumber));
        }
        else
        {
            labelTrainNumber.setText("");
            labelPtrainNumber.setText("");
        }

        if(om.isPassing())
        {
            labelPpassing.setText(" 通過設定 ");
            labelPpassing.getStyleClass().add("P-passing");
            labelPtrainNumberPassing.setText("通");
            labelPtrainNumberPassing.getStyleClass().add("plane-text");
        }
        else
        {
            labelPpassing.setText("");
            labelPpassing.getStyleClass().remove("P-passing");
            labelPtrainNumberPassing.setText("");
            labelPtrainNumberPassing.getStyleClass().remove("plane-text");
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

    // スレッド外でも安全動かしラッパー君Ver.1
    private void safeUpdate(Runnable r)
    {
        if (javafx.application.Platform.isFxApplicationThread())
        {
            r.run();
        }
        else
        {
            javafx.application.Platform.runLater(r);
        }
    }

    @Override
    public void onMessage(String key, Object value)
    {
        if ("updateSpeed".equals(key))
        {
            // めっちゃややこいので解説
            // 下から（ネスト深い方から）読むとわかりやすいよ～

            // 6. そのままだとエラー吐かれるので大人しくFXのスレで動かす
            safeUpdate(() -> 
            {
                // 5. labelSpeedラベルの文字を変えたい！！
                labelSpeed.setText
                (
                    // 4. 全角にする
                    om.toZenkaku
                    (
                        // 3. 全角にするメソッドはStringしかだめなので
                        //    Stringにする
                        Integer.toString
                        (
                            // 1. valueの型は不定なので、無理やりfloatにする
                            // 2. floatにしたら四捨五入
                            Math.round((float)value) // ()だけだから ; つけるな！！
                        )
                    ) +"km/h" // 単位ここで付けとく 全角にしたあとね
                );
            });
        }

        if ("updateKiloPost".equals(key))
        {
            // めっちゃややこいので解説
            // 下から（ネスト深い方から）読むとわかりやすいよ～

            // 6. そのままだとエラー吐かれるので大人しくFXのスレで動かす
            safeUpdate(() -> 
            {
                // 5. labelSpeedラベルの文字を変えたい！！
                labelKiloPost.setText
                (
                    // 4. 全角にする
                    om.toZenkaku
                    (
                        // 3. Stringにする formatで
                        String.format("%.1f", value) // ()だけだから ; つけるな！！
                    ) +"km" // 単位ここで付けとく 全角にしたあとね
                );
            });
        }
    }
}
