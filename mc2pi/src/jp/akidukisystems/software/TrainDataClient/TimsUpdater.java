package jp.akidukisystems.software.TrainDataClient;

import jp.akidukisystems.software.TrainDataClient.GUI.TIMS.BaseController;

public class TimsUpdater
{
    private BaseController current;
    private TrainControl tc;

    public void init(TrainControl tc)
    {
        this.tc = tc;
    }

    public void refresh()
    {
        BaseController bc = current;
        bc.onMessage("updateSpeed", tc.getSpeed());
        bc.onMessage("updateKiloPost", tc.getMove());
    }

    public void setCurrentController(BaseController c) {
        current = c;
    }

    public BaseController getCurrentController() {
        return current;
    }
}
