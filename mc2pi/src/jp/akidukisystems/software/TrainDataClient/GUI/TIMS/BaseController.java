package jp.akidukisystems.software.TrainDataClient.GUI.TIMS;

import javafx.scene.Parent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.TimsUpdater;
import jp.akidukisystems.software.TrainDataClient.TrainControl;
import jp.akidukisystems.software.TrainDataClient.TrainNumber;
import jp.akidukisystems.software.TrainDataClient.duty.OperationManager;
import jp.akidukisystems.software.utilty.DutyCardRepository;
import jp.akidukisystems.software.utilty.NetworkManager;

public abstract class BaseController {
    protected TDCCore core;
    protected TrainControl tc;
    protected DutyCardRepository repo;
    protected OperationManager om;
    protected TrainNumber tn;
    protected NetworkManager nm;
    protected TimsUpdater tu;

    protected StackPane container;

    public void setContainer(StackPane container) {
        this.container = container;
    }

    public void init(TDCCore core) {
        this.core = core;
        this.tc = core.tc;
        this.repo = core.dcr;
        this.om = core.om;
        this.tn = core.tn;
        this.nm = core.networkManager;
        this.tu = core.timsUpdater;

        onReady();
    }

    protected void onReady() { }

    protected void setScreen(Parent child) {
        // child は 960x720 に固定
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

        // 親の container StackPane に差し替える
        // container は TimsSetup で作った中央固定用 StackPane
        container.getChildren().setAll(child);
    }

    // 使用時は継承先でオーバーライドすること。
    public void onMessage(String key, Object value)
    {

    }
}
