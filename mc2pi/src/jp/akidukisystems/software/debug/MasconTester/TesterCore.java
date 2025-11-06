package jp.akidukisystems.software.debug.MasconTester;

import jp.akidukisystems.software.utilty.MasConReader;

public class TesterCore {
    
    private static MasConReader reader;
    public static void main(String[] args) {
        TesterCore object = new TesterCore();
        reader = new MasConReader();
        object.running();
    }

    public void running()
    {
        new Thread(() ->
        {
            if(!reader.isRunning()) reader.start();

            while (true) {
                System.out.println(MasConReader.mapYtoNotch(reader.getValue("y")));
            }
        }).start();;
    }
}
