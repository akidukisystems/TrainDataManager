package jp.akidukisystems.software.TrainDataClient.GUI.TIMS;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.TrainControl;
import jp.akidukisystems.software.TrainDataClient.TrainNumber;
import jp.akidukisystems.software.TrainDataClient.duty.TimsToolkit;
import jp.akidukisystems.software.utilty.DutyCardRepository;
import jp.akidukisystems.software.utilty.NetworkManager;

public class GaugeController extends Application {

    private static TDCCore staticCore;

    private TDCCore core;
    @SuppressWarnings("unused")
    private TrainControl tc;
    @SuppressWarnings("unused")
    private DutyCardRepository repo;
    @SuppressWarnings("unused")
    private TimsToolkit om;
    @SuppressWarnings("unused")
    private TrainNumber tn;
    @SuppressWarnings("unused")
    private NetworkManager nm;

    private final double baseWidth = 1280;
    private final double baseHeight = 720;

    public static void setCore(TDCCore core)
    {
        staticCore = core;
    }

    @Override
    public void init()
    {
        this.core = staticCore;
        this.tc = (core != null) ? core.tc : null;
        this.repo = (core != null) ? core.dcr : null;
        this.om = (core != null) ? core.om : null;
        this.tn = (core != null) ? core.tn : null;
        this.nm = (core != null) ? core.networkManager : null;
    }

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        root.setPrefSize(1280, 720);

        // コンテナつくって親にぶら下げる
        StackPane container = new StackPane();
        container.setPrefSize(960, 720);
        root.getChildren().add(container);

        Parent child = createChildContent();



        if (child instanceof Region r) {
            r.setPrefSize(960, 720);
            r.setMaxSize(960, 720);
            r.setMinSize(960, 720);
            r.setBorder(new Border(new BorderStroke(
                Color.GRAY,
                BorderStrokeStyle.SOLID,
                new CornerRadii(0),
                new BorderWidths(2)
            )));
        }

        container.getChildren().setAll(child);
        container.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        Scene scene = new Scene(root, baseWidth, baseHeight);

        stage.setScene(scene);
        stage.setTitle("TIMS");
        stage.show();
    }

    private Parent createChildContent() {

        StackPane root = new StackPane();
        root.setPrefSize(960, 720);

        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
