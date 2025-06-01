package jp.akidukisystems.traindatamanager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BlockNBTGetter {

    static float prevDist = 0f;
    static float distance = 0f;

    public static String getNBTAtPlayerFoot(EntityPlayer player) {
        // プレイヤーのワールドを取得
        BlockPos below2 = player.getPosition().down(2);
        TileEntity tileEntity = player.world.getTileEntity(below2);

        if (tileEntity != null) {

            NBTTagCompound tag = new NBTTagCompound();
            tileEntity.writeToNBT(tag);  // NBTにTileEntityの内容を書き出す

            System.out.println(tag.toString());

            String BlockName = tag.getString("id");
            int signal_0 = tag.getInteger("signal_0");
            int signal_1 = tag.getInteger("signal_1");

            if ("minecraft:tesc_rsin".equals(BlockName)) {
                // signal0
                /// 1...TIMS情報更新
                /// 2...次駅接近
                /// 3...停止位置情報
                ///     signal1
                ///     0...解除
                ///     30～127...(A)式によって得られた値
                ///     (A)式
                ///         (Signal_1 - 27) / 10
                ///         早見表
                ///         (距離)  (Signal_1)
                ///         0.8m    35
                ///         0.9m    36
                ///         1.0m    37
                ///         1.1m    38
                ///         1.2m    39
                ///         1.5m    42
                ///         2.0m    47
                ///         2.5m    52
                ///         3.0m    57
                ///         5.0m    77
                ///         10.0m   127
                switch (signal_0)
                {
                    case 3:
                        switch (signal_1)
                        {
                            case 0:
                                prevDist = 0f;
                                return "{\"type\":\"stoppos\",\"signal_0\":"+ signal_0 +",\"signal_1\":"+ signal_1 +",\"distance\":-1}";
                            default:
                                if (signal_1 >= 30 && signal_1 <= 127)
                                {
                                    prevDist = distance;
                                    distance = ((float)signal_1 - 27f) / 10f;
                                    if (prevDist != distance)
                                        return "{\"type\":\"stoppos\",\"signal_0\":"+ signal_0 +",\"signal_1\":"+ signal_1 +",\"distance\":"+ distance +"}";
                                }
                        }
                    default:
                        return "{\"type\":\"beacon\",\"signal_0\":"+ signal_0 +",\"signal_1\":"+ signal_1 +"}";
                }
            }
        }
        return null;
    }
}
