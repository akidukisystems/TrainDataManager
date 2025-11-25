package jp.akidukisystems.software.TrainDataClient.GUI.TIMS.Screen.Controller;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.BaseController;
import jp.akidukisystems.software.TrainDataClient.TrainControl.formationInfo;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader.Direction;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader.DcrLine;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader.TimeTable;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader.TimeTableEntry;
import jp.akidukisystems.software.TrainDataClient.duty.DutyCardReader.Track;

public class D01AA extends BaseController 
{
    @FXML private Button btnS00AB;
    @FXML private Button btnD00AA;
    @FXML private Button btnD01AA;
    @FXML private Button btnB00AA;

    @FXML private Button btnNext;
    @FXML private Button btnSenbetsu;

    @FXML private GridPane gridPane;

    @FXML private HBox formationHBox;

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

    @FXML private Label labelNextStopStaName;
    @FXML private Label labelNextStopStaArrive;
    @FXML private Label labelNextStopStaArriveSec;
    
    Timeline timeline;
    Timeline blink;
    
    private Button selectedButton = null;

    private Label[] labelLine1;
    private Label[] labelLine2;
    private Label[] labelLine3;
    private Label[] labelLineNextStopSta;

    Rectangle[] rectDoorState;
    Label[] labelDoorState;
    Node[] nodeCarState;
    Label[] labelCarState;

    @Override
    public void init(TDCCore core)
    {
        super.init(core);

        timeline = new Timeline
        (
            new KeyFrame
            (
                Duration.millis(100),   // 更新間隔（ms）
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
            labelLine1ArriveSec,
            labelLine1Depart,
            labelLine1DepartSec
        };

        labelLine2 = new Label[]
        {
            labelLine2Sta,
            labelLine2Arrive,
            labelLine2ArriveSec,
            labelLine2Depart,
            labelLine2DepartSec
        };

        labelLine3 = new Label[]
        {
            labelLine3Sta,
            labelLine3Arrive,
            labelLine3ArriveSec,
            labelLine3Depart,
            labelLine3DepartSec
        };

        labelLineNextStopSta = new Label[]
        {
            labelNextStopStaName,
            labelNextStopStaArrive,
            labelNextStopStaArriveSec
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

        for(Label s : labelLineNextStopSta)
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

        refreshTimeTable();

        int n = tc.getCars();

        // rectCarStateはM, Tmしか入らないし、polyに関してはMcしか入らないので、正直こんなに配列いらない。
        rectDoorState  = new Rectangle[n];
        labelDoorState = new Label[n];
        nodeCarState   = new Node[n];
        labelCarState  = new Label[n];

        for(int i = 0; i < n; i++)
        {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            box.setSpacing(1);

            // ----------- 上のダァ -----------
            StackPane sp1 = new StackPane();

            Rectangle r1 = new Rectangle(40, 20);
            r1.setStroke(Color.WHITE);
            r1.setFill(Color.BLACK);
            rectDoorState[i] = r1;

            Label l1 = new Label("閉");
            l1.getStyleClass().add("plane-text-ff");
            labelDoorState[i] = l1;

            sp1.getChildren().addAll(r1, l1);

            // ----------- 空白 -----------
            Region gap = new Region();
            gap.setPrefHeight(25);

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

                if(List.of(formationInfo.Mc).contains(tc.getFormationFromCar(i)))
                {
                    nodeCarState[i] = poly;
                    labelCarState[i] = l2;
                }
            }
            else
            {
                Rectangle r2 = new Rectangle(50, 20);
                r2.setStroke(Color.WHITE);
                r2.setFill(Color.web("#2f3e56"));

                sp2.getChildren().addAll(r2, l2);

                if(List.of(formationInfo.M, formationInfo.Tm).contains(tc.getFormationFromCar(i)))
                {
                    nodeCarState[i] = r2;
                    labelCarState[i] = l2;
                }
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
            box.getChildren().addAll(sp1, gap, pantagraph, sp2, h);

            formationHBox.getChildren().add(box);
        }


    
    }

    private void refreshTimeTable()
    {
        if(om.getTrainNumber() != null && om.getLine() != null)
        {
            DcrLine line = om.getLine();
            Direction direction = om.getDirection();
            float kilopost = tc.getMove();
            TimeTable timeTable = om.getTimeTable();

            // わからん！！！！！！！！！

            // Line, Direction, Kilopost, Timetableから、「前の駅」「次の駅」「その次の駅」を取得したいな
            // 駅の表からじゃなくて、時刻表エントリから
            // 1. 各駅のキロポストと所属路線を取得
            // 2. 自車の現在地点（キロポスト）と現在路線から、「前の駅」を探す
            // 3.  このとき、directionがUPの場合、上り方面なので、
            //     例  A駅(1.4km) - (←自車←) - B駅(2.1km)
            //     とあった場合、B駅を取得する
            // 4. B駅の次の駅(A駅)、その次の駅を「時刻表をベースとして」取得する
            // 5. なぜ時刻表ベースなのかというと、駅一覧ベースで探索して、途中で分岐して支線に入る場合、その支線の駅にたどり着けないから（別の路線所属）
            // 6. ポイント
            //     - 前の駅が存在しない場合（つまり始発）、次の駅、その次の駅、そのまた次の駅を返す
            //     - ある駅と現在地点が同じキロポストの場合、その駅を「次の駅」とする
            //     - 次の次の駅が存在しない場合、nullとする

            // 前の駅、次の駅、その次の駅を取得
            // 停車場に停車中の場合、停車中の駅、次の駅、その次の駅となる
            TimeTableEntry[] sta = repo.getSurroundingStations(kilopost /1000f, line, direction, timeTable);

            String timeHolder = "";

            // 前の駅　または　停車中の駅
            // これ多分メソッドにしたほうがいい 3駅分処理するので
            if(sta[0] != null)
            {
                System.out.println(repo.getStation(sta[0].stationId).name);

                // 駅名をラベルに書き込む
                // 4文字未満でも4文字分になるようにしてる
                labelLine1Sta.setText(om.formatString(repo.getStation(sta[0].stationId).name));

                // 到着時刻と出発時刻
                String[] ArriveTime = om.splitTime(sta[0].arrive);
                String[] DepartTime = om.splitTime(sta[0].depart);

                if(ArriveTime != null)
                {
                    // 到着時刻がレ つまり、通過
                    if(ArriveTime[0].equals("レ"))
                    {
                        // ほんとうは0.5文字分のスペースが追加でほしい。
                        labelLine1Arrive.setText("    ⇩");
                        labelLine1ArriveSec.setText("");
                    }
                    else
                    {
                        // 時刻はhh:mm:ssとなって格納されているので、予め分割してもらい、hh:mmとssでわけて表示
                        labelLine1Arrive.setText(om.toZenkaku(ArriveTime[0] +":"+ ArriveTime[1]));
                        labelLine1ArriveSec.setText(ArriveTime[2]);
                    }
                }
                else
                {
                    labelLine1Arrive.setText("");
                    labelLine1ArriveSec.setText("");
                }

                if(DepartTime != null)
                {
                    if(timeHolder.equals(DepartTime[0]))
                    {
                        labelLine1Depart.setText(om.toZenkaku("      "+ DepartTime[1]));
                        labelLine1DepartSec.setText(DepartTime[2]);
                    }
                    else if(DepartTime[0].equals("="))
                    {
                        labelLine1Depart.setText("    ＝");
                        labelLine1DepartSec.setText("");
                    }
                    else
                    {
                        labelLine1Depart.setText(om.toZenkaku(DepartTime[0] +":"+ DepartTime[1]));
                        labelLine1DepartSec.setText(DepartTime[2]);
                        timeHolder = DepartTime[0];
                    }
                }
                else
                {
                    labelLine1Depart.setText("");
                    labelLine1DepartSec.setText("");
                }

                // 到着番線
                // nullのときがある
                Track track = repo.getTrack(sta[0].trackId);
                if(track != null)
                {
                    labelLine1Track.setText(om.toZenkaku(track.name));
                }
                else
                {
                    labelLine1Track.setText("");
                }

                // 速度制限
                // int型なのでnullではなく-1のときがある
                // 文字列に変換し、-1のものは""にしてもらう
                String enterLimit = om.formatSpeedLimit(sta[0].enterLimit);
                String exitLimit  = om.formatSpeedLimit(sta[0].exitLimit);

                // なにかしら制限あるなら書いとく
                if(enterLimit != "" || exitLimit != "")
                {
                    labelLine1Limit.setText(String.format("%2s / %2s", enterLimit, exitLimit));
                }
                else
                {
                    labelLine1Limit.setText("");
                }
            }
            else
            {
                // えっヌルですか？！
                System.out.println("null");
                labelLine1Sta.setText("");
                labelLine1Arrive.setText("");
                labelLine1ArriveSec.setText("");
                labelLine1Depart.setText("");
                labelLine1DepartSec.setText("");
                labelLine1Track.setText("");
                labelLine1Limit.setText("");
            }

            //あとはおなじ

            if(sta[1] != null)
            {
                System.out.println(repo.getStation(sta[1].stationId).name);
                labelLine2Sta.setText(om.formatString(repo.getStation(sta[1].stationId).name));

                String[] ArriveTime = om.splitTime(sta[1].arrive);
                String[] DepartTime = om.splitTime(sta[1].depart);

                if(ArriveTime != null)
                {
                    if(ArriveTime[0].equals("レ"))
                    {
                        labelLine2Arrive.setText("    ⇩");
                        labelLine2ArriveSec.setText("");
                    }
                    else
                    {
                        if(timeHolder.equals(ArriveTime[0]))
                        {
                            labelLine2Arrive.setText(om.toZenkaku("      "+ ArriveTime[1]));
                        }
                        else
                        {
                            labelLine2Arrive.setText(om.toZenkaku(ArriveTime[0] +":"+ ArriveTime[1]));
                        }
                        
                        labelLine2ArriveSec.setText(ArriveTime[2]);
                    }
                }
                else
                {
                    labelLine2Arrive.setText("");
                    labelLine2ArriveSec.setText("");
                }

                if(DepartTime != null)
                {
                    if(timeHolder.equals(DepartTime[0]))
                    {
                        labelLine2Depart.setText(om.toZenkaku("      "+ DepartTime[1]));
                        labelLine2DepartSec.setText(DepartTime[2]);
                    }
                    else if(DepartTime[0].equals("="))
                    {
                        labelLine2Depart.setText("    ＝");
                        labelLine2DepartSec.setText("");
                    }
                    else
                    {
                        labelLine2Depart.setText(om.toZenkaku(DepartTime[0] +":"+ DepartTime[1]));
                        labelLine2DepartSec.setText(DepartTime[2]);
                        timeHolder = DepartTime[0];
                    }
                }
                else
                {
                    labelLine2Depart.setText("");
                    labelLine2DepartSec.setText("");
                }

                Track track = repo.getTrack(sta[1].trackId);
                if(track != null)
                {
                    labelLine2Track.setText(om.toZenkaku(track.name));
                }
                else
                {
                    labelLine2Track.setText("");
                }

                String enterLimit = om.formatSpeedLimit(sta[1].enterLimit);
                String exitLimit  = om.formatSpeedLimit(sta[1].exitLimit);

                if(enterLimit != "" || exitLimit != "")
                {
                    labelLine2Limit.setText(String.format("%2s / %2s", enterLimit, exitLimit));
                }
                else
                {
                    labelLine2Limit.setText("");
                }
            }
            else
            {
                System.out.println("null");
                labelLine2Sta.setText("");
                labelLine2Arrive.setText("");
                labelLine2ArriveSec.setText("");
                labelLine2Depart.setText("");
                labelLine2DepartSec.setText("");
                labelLine2Track.setText("");
                labelLine2Limit.setText("");
            }

            if(sta[2] != null)
            {
                System.out.println(repo.getStation(sta[2].stationId).name);
                labelLine3Sta.setText(om.formatString(repo.getStation(sta[2].stationId).name));

                String[] ArriveTime = om.splitTime(sta[2].arrive);
                String[] DepartTime = om.splitTime(sta[2].depart);

                if(ArriveTime != null)
                {
                    if(ArriveTime[0].equals("レ"))
                    {
                        labelLine3Arrive.setText("    ⇩");
                        labelLine3ArriveSec.setText("");
                    }
                    else
                    {
                        if(timeHolder.equals(ArriveTime[0]))
                        {
                            labelLine3Arrive.setText(om.toZenkaku("      "+ ArriveTime[1]));
                        }
                        else
                        {
                            labelLine3Arrive.setText(om.toZenkaku(ArriveTime[0] +":"+ ArriveTime[1]));
                        }

                        labelLine3ArriveSec.setText(ArriveTime[2]);
                    }
                }
                else
                {
                    labelLine3Arrive.setText("");
                    labelLine3ArriveSec.setText("");
                }

                if(DepartTime != null)
                {
                    if(timeHolder.equals(DepartTime[0]))
                    {
                        labelLine3Depart.setText(om.toZenkaku("      "+ DepartTime[1]));
                        labelLine3DepartSec.setText(DepartTime[2]);
                    }
                    else if(DepartTime[0].equals("="))
                    {
                        labelLine3Depart.setText("    ＝");
                        labelLine3DepartSec.setText("");
                    }
                    else
                    {
                        labelLine3Depart.setText(om.toZenkaku(DepartTime[0] +":"+ DepartTime[1]));
                        labelLine3DepartSec.setText(DepartTime[2]);
                    }
                }
                else
                {
                    labelLine3Depart.setText("");
                    labelLine3DepartSec.setText("");
                }

                Track track = repo.getTrack(sta[2].trackId);
                if(track != null)
                {
                    labelLine3Track.setText(om.toZenkaku(track.name));
                }
                else
                {
                    labelLine3Track.setText("");
                }

                String enterLimit = om.formatSpeedLimit(sta[2].enterLimit);
                String exitLimit  = om.formatSpeedLimit(sta[2].exitLimit);

                if(enterLimit != "" || exitLimit != "")
                {
                    labelLine3Limit.setText(String.format("%2s / %2s", enterLimit, exitLimit));
                }
                else
                {
                    labelLine3Limit.setText("");
                }
            }
            else
            {
                System.out.println("null");
                labelLine3Sta.setText("");
                labelLine3Arrive.setText("");
                labelLine3ArriveSec.setText("");
                labelLine3Depart.setText("");
                labelLine3DepartSec.setText("");
                labelLine3Track.setText("");
                labelLine3Limit.setText("");
            }

            TimeTableEntry nextStopStationEntry = repo.getNextStopStation(kilopost /1000f, line, direction, timeTable);

            if(nextStopStationEntry != null)
            {
                System.out.println(repo.getStation(nextStopStationEntry.stationId).name);

                labelNextStopStaName.setText(om.formatString(repo.getStation(nextStopStationEntry.stationId).name));

                String[] ArriveTime = om.splitTime(nextStopStationEntry.arrive);

                if(ArriveTime != null)
                {
                    if(ArriveTime[0].equals("レ"))
                    {
                        labelNextStopStaArrive.setText("    ⇩");
                        labelNextStopStaArriveSec.setText("");
                    }
                    else
                    {
                        if(timeHolder.equals(ArriveTime[0]))
                        {
                            labelNextStopStaArrive.setText(om.toZenkaku("      "+ ArriveTime[1]));
                        }
                        else
                        {
                            labelNextStopStaArrive.setText(om.toZenkaku(ArriveTime[0] +":"+ ArriveTime[1]));
                        }

                        labelNextStopStaArriveSec.setText(ArriveTime[2]);
                    }
                }
                else
                {
                    labelNextStopStaArrive.setText("");
                    labelNextStopStaArriveSec.setText("");
                }
            }
            else
            {
                System.out.println("null");
                labelNextStopStaName.setText("");
                labelNextStopStaArrive.setText("");
                labelNextStopStaArriveSec.setText("");
            }
                
        }
    }

    private void update()
    {
        if (core != null && core.tc != null)
        {
            if(om.getTrainNumber() != null && om.getLine() != null)
            {
                if(tc.ew.isArrivedStation() || tc.ew.isPassedStation())
                {
                    DcrLine line = om.getLine();
                    Direction direction = om.getDirection();
                    float kilopost = tc.getMove();
                    TimeTable timeTable = om.getTimeTable();

                    // 次の駅ってどれ？
                    TimeTableEntry sta = repo.getNextStation(kilopost /1000f, line, direction, timeTable);

                    // 次駅更新
                    if(sta != null)
                    {
                        om.setStation(repo.getStation(sta.stationId));
                        om.setLine(repo.getLine(repo.getStation(sta.stationId).lineId));

                        nm.sendCommand("send", "move", 1000f * repo.getStation(sta.stationId).linePost);

                        // すぐ更新すると変わらないので2秒まつ！
                        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
                        exec.schedule(() ->
                        {
                            safeUpdate(() ->
                            {
                                refreshTimeTable();
                            });
                        }, 2, TimeUnit.SECONDS);
                    }

                    tc.ew.resetArriveEvent();
                    tc.ew.resetPassingEvent();
                }
            }

            if(labelDoorState != null && rectDoorState != null && nodeCarState != null)
            {
                switch (tc.getSpeedState()) {
                    case Up:
                        for(Node n: nodeCarState)
                        {
                            if((n != null) && (n instanceof Rectangle s))
                                s.setFill(Color.web("#80FFE8"));

                            if((n != null) && (n instanceof Polygon p))
                                p.setFill(Color.web("#80FFE8"));
                        }

                        for(Label s: labelCarState)
                        {
                            if(s != null)
                                s.setTextFill(Color.BLACK);
                        }
                        break;

                    case Down:
                        for(Node n: nodeCarState)
                        {
                            if((n != null) && (n instanceof Rectangle s))
                                s.setFill(Color.YELLOW);

                            if((n != null) && (n instanceof Polygon p))
                                p.setFill(Color.YELLOW);
                        }

                        for(Label s: labelCarState)
                        {
                            if(s != null)
                                s.setTextFill(Color.BLACK);
                        }
                        break;
                
                    default:
                        for(Node n: nodeCarState)
                        {
                            if((n != null) && (n instanceof Rectangle s))
                                s.setFill(Color.web("#2f3e56"));

                            if((n != null) && (n instanceof Polygon p))
                                p.setFill(Color.web("#2f3e56"));
                        }

                        for(Label s: labelCarState)
                        {
                            if(s != null)
                                s.setTextFill(Color.WHITE);
                        }
                        break;
                }
            }
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
                        String.format("%.1f", (float)value / 1000f) // ()だけだから ; つけるな！！
                    ) +"km" // 単位ここで付けとく 全角にしたあとね
                );
            });
        }
    }
}
