package jp.akidukisystems.traindatamanager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import jp.akidukisystems.traindatamanager.TrainLogger;

@Mod(modid = TDMCore.MODID, name = TDMCore.NAME, version = TDMCore.VERSION)
public class TDMCore {
    public static final String MODID = "traindatamanager";
    public static final String NAME = "Train Data Manager";
    public static final String VERSION = "1.0";
    private static final TrainLogger LOGGER_INSTANCE = new TrainLogger();

    // ロガー
    private static Logger logger;

    // Modインスタンス
    @Mod.Instance(MODID)
    public static TDMCore INSTANCE;

    public static Logger getLogger() {
        return logger;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ConfigManager.init(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // イベントバス登録
        MinecraftForge.EVENT_BUS.register(LOGGER_INSTANCE);
    }
}
