package jp.akidukisystems.traindatamanager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BlockNBTGetter {

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
                return "{\"type\":\"beacon\",\"signal_0\":"+ signal_0 +",\"signal_1\":"+ signal_1 +"}";
            }
        }
        return null;
    }
}
