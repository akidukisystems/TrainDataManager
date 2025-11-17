package jp.akidukisystems.software.TrainDataClient.GUI.TIMS;

import jp.akidukisystems.software.TrainDataClient.TDCCore;
import jp.akidukisystems.software.TrainDataClient.TrainControl;

public abstract class BaseController {
    protected TDCCore core;
    protected TrainControl tc;

    public void init(TDCCore core) {
        this.core = core;
        this.tc = core.tc;
        onReady();   // initialize の後に呼ばれる
    }

    protected void onReady() { }
}
