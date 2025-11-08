package jp.akidukisystems.software.TrainDataClient;

public class TrainNumber
{
    // TIMS画面に大きく表示される完全な内容の列車番号
    // 臨9001M　回1001M
    String full;

    // TIMS画面に小さく表示される一般的な列車番号
    // 9001M 1001M
    String half;

    // 数字のみ
    // 9001 1001
    String number;

    // 末尾アルファベットのみ
    // A,B,C,D,E,F,G,H,K,M,S,T,Y
    String alphabet;

    public void reset()
    {
        alphabet = number = half = full = null;
    }
}
