package jp.akidukisystems.software.TrainDataClient;

public class TrainNumber
{
    // TIMS画面に大きく表示される完全な内容の列車番号
    // 臨9001M　回1001M
    private String full;

    // TIMS画面に小さく表示される一般的な列車番号
    // 9001M 1001M
    private String half;

    // 数字のみ
    // 9001 1001
    private String number;

    // 末尾アルファベットのみ
    private String alphabet;

    // 頭につく文字
    private String moji;

    public String getMoji() {
        return moji;
    }

    public void setMoji(String moji) {
        this.moji = moji;
    }

    public void reset()
    {
        setAlphabet(setNumber(setHalf(setFull(null))));
    }

    public String getFull() {
        return full;
        
    }

    public String setFull(String full) {
        this.full = full;
        
        return full;
    }

    public String getHalf() {
        return half;
        
    }

    public String setHalf(String half) {
        this.half = half;
        
        return half;
    }

    public String getNumber() {
        return number;
        
    }

    public String setNumber(String number) {
        this.number = number;
        
        return number;
    }

    public String getAlphabet() {
        return alphabet;
        
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
        
    }
}
