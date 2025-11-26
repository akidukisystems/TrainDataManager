package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.BaseController;
import jp.akidukisystems.software.TrainDataClient.TrainControl.formationInfo;

public class A06AA extends BaseController 
{
    @FXML private HBox formationHBox;

    @FXML private Button btnD00AA;
    @FXML private Label title;
    @FXML private Label message;

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
        btnD00AA.setOnAction(e -> goNext("/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/D00AA.fxml"));
        btnD00AA.setText("運転士\nメニュー");
        message.setText("ＡＴＳ－Ｐ\n非常ブレーキ動作");
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

        int n = tc.getCars();

        for(int i = 0; i < n; i++)
        {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            box.setSpacing(1);

            // ----------- パンタ -----------
            HBox pantagraph = new HBox();
            pantagraph.setAlignment(Pos.CENTER);
            pantagraph.setSpacing(25);

            // 左グループ
            Group g1 = new Group();

            Line g1_l1 = new Line(3, 0, 0, 3);
            g1_l1.setStrokeWidth(1);

            Line g1_l2 = new Line(0, 3, 3, 6); 
            g1_l2.setStrokeWidth(1);

            g1.getChildren().addAll(g1_l1, g1_l2);

            // 右グループ
            Group g2 = new Group();

            Line g2_l1 = new Line(0, 0, 3, 3);
            g2_l1.setStrokeWidth(1);

            Line g2_l2 = new Line(3, 3, 0, 6);
            g2_l2.setStrokeWidth(1);

            g2.getChildren().addAll(g2_l1, g2_l2);

            pantagraph.getChildren().addAll(g1, g2);

            // ----------- 下の編成 -----------
            StackPane sp2 = new StackPane();

            Label l2 = new Label(om.toZenkaku(Integer.toString(i + 1)));
            l2.getStyleClass().add("plane-text-no-textFill");
            l2.setTextFill(Color.WHITE);

            // めっちゃがんばって先頭車両つくってる
            if(List.of(formationInfo.Mc, formationInfo.Tc).contains(tc.getFormationFromCar(i)))
            {
                Polygon poly;
                if(i == 0)
                {
                    poly = new Polygon
                    (
                            50.0, 0.0,
                            15.0, 0.0,
                            0.0, 5.0,
                            0.0, 20.0,
                            50.0, 20.0
                    );
                }
                else
                {
                    poly = new Polygon
                    (
                            0.0, 0.0,
                            35.0, 0.0,
                            50.0, 5.0,
                            50.0, 20.0,
                            0.0, 20.0
                    );
                }
 
                poly.setStroke(Color.WHITE);
                poly.setFill(Color.web("#2f3e56"));

                sp2.getChildren().addAll(poly, l2);
            }
            else
            {
                Rectangle r2 = new Rectangle(50, 20);
                r2.setStroke(Color.WHITE);
                r2.setFill(Color.web("#2f3e56"));

                sp2.getChildren().addAll(r2, l2);
            }   

            // ----------- 丸2つ -----------
            HBox h = new HBox();
            h.setAlignment(Pos.CENTER);
            h.setSpacing(25);

            Circle c1 = new Circle(4);
            Circle c2 = new Circle(4);

            switch (tc.getFormationFromCar(i)) {
                case Mc:
                    c1.setStroke(Color.GRAY);
                    c1.setFill(Color.WHITE);
                    c2.setStroke(Color.GRAY);
                    c2.setFill(Color.WHITE);

                    if(i == 0)
                    {
                        g1_l1.setStroke(Color.BLACK);
                        g1_l2.setStroke(Color.BLACK);
                        g2_l1.setStroke(Color.YELLOW);
                        g2_l2.setStroke(Color.YELLOW);
                    }
                    else
                    {
                        g1_l1.setStroke(Color.YELLOW);
                        g1_l2.setStroke(Color.YELLOW);
                        g2_l1.setStroke(Color.BLACK);
                        g2_l2.setStroke(Color.BLACK);
                    }
                    break;

                case M:
                    c1.setStroke(Color.GRAY);
                    c1.setFill(Color.WHITE);
                    c2.setStroke(Color.GRAY);
                    c2.setFill(Color.WHITE);
                    g1_l1.setStroke(Color.YELLOW);
                    g1_l2.setStroke(Color.YELLOW);
                    g2_l1.setStroke(Color.YELLOW);
                    g2_l2.setStroke(Color.YELLOW);
                    break;

                case Tm:
                    c1.setStroke(Color.GRAY);
                    c1.setFill(Color.WHITE);
                    c2.setStroke(Color.GRAY);
                    c2.setFill(Color.WHITE);
                    g1_l1.setStroke(Color.BLACK);
                    g1_l2.setStroke(Color.BLACK);
                    g2_l1.setStroke(Color.BLACK);
                    g2_l2.setStroke(Color.BLACK);
                    break;
            
                default:
                    c1.setStroke(Color.GRAY);
                    c1.setFill(Color.BLACK);
                    c2.setStroke(Color.GRAY);
                    c2.setFill(Color.BLACK);
                    g1_l1.setStroke(Color.BLACK);
                    g1_l2.setStroke(Color.BLACK);
                    g2_l1.setStroke(Color.BLACK);
                    g2_l2.setStroke(Color.BLACK);
                    break;
            } 

            h.getChildren().addAll(c1, c2);

            // ----------- まとめて VBoxへ -----------
            box.getChildren().addAll(pantagraph, sp2, h);

            formationHBox.getChildren().add(box);
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
