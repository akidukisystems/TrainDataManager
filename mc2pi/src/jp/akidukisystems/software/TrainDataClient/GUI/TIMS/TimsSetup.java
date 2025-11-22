package jp.akidukisystems.software.TrainDataClient.GUI.TIMS;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.TimsUpdater;
import jp.akidukisystems.software.TrainDataClient.TrainControl;
import jp.akidukisystems.software.TrainDataClient.TrainNumber;
import jp.akidukisystems.software.TrainDataClient.duty.OperationManager;
import jp.akidukisystems.software.utilty.DutyCardRepository;
import jp.akidukisystems.software.utilty.NetworkManager;

public class TimsSetup extends Application
{
    private static TDCCore staticCore;

    private TDCCore core;
    @SuppressWarnings("unused")
    private TrainControl tc;
    @SuppressWarnings("unused")
    private DutyCardRepository repo;
    @SuppressWarnings("unused")
    private OperationManager om;
    @SuppressWarnings("unused")
    private TrainNumber tn;
    @SuppressWarnings("unused")
    private NetworkManager nm;
    @SuppressWarnings("unused")
    private TimsUpdater tu;

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
        this.tu = (core != null) ? core.timsUpdater : null;
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        // 親を作る
        StackPane root = new StackPane();
        root.setPrefSize(1280, 720);

        // コンテナつくって親にぶら下げる
        StackPane container = new StackPane();
        container.setPrefSize(960, 720);
        root.getChildren().add(container);

        FXMLLoader loader = new FXMLLoader
        (
            getClass().getResource
            (
                "/jp/akidukisystems/software/TrainDataClient/GUI/TIMS/Screen/View/S00AB.fxml"
            )
        );

        Parent child = loader.load();

        Object controller = loader.getController();
        if (controller instanceof BaseController c)
        {
            c.init(core);
            c.setContainer(container);
        }

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

        // コンテナの子にFXMLの中身ぶちまけ
        container.getChildren().setAll(child);
        container.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        Scene scene = new Scene(root, baseWidth, baseHeight);

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
