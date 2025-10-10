package jp.akidukisystems.traindatamanager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BlockNBTGetter
{

    static float prevDist = 0f;
    static float distance = 0f;

    public static TileEntity getNBT(EntityPlayer player)
    {
        BlockPos below2 = player.getPosition().down(2);
        return player.world.getTileEntity(below2);
    }

    public static String getNBTAtPlayerFoot(EntityPlayer player)
    {
        // プレイヤーのワールドを取得
        TileEntity tileEntity = getNBT(player);

        if (tileEntity != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tileEntity.writeToNBT(tag);  // NBTにTileEntityの内容を書き出す

            System.out.println(tag.toString());

            String BlockName = tag.getString("id");
            int signal_0 = tag.getInteger("signal_0");
            int signal_1 = tag.getInteger("signal_1");

            if ("minecraft:tesc_rsin".equals(BlockName))
            {
                // signal0
                /// 1...TIMS情報更新
                /// 2...次駅接近
                switch (signal_0)
                {
                    default:
                        return "{\"type\":\"beacon\",\"signal_0\":"+ signal_0 +",\"signal_1\":"+ signal_1 +"}";
                }
            }
        }
        return null;
    }
}
