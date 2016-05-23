package joshie.harvest.core.network;

import joshie.harvest.core.helpers.generic.MCClientHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class PacketRenderUpdate extends AbstractPacketLocation {
    public PacketRenderUpdate() {}

    public PacketRenderUpdate(int dim, BlockPos pos) {
        super(dim, pos);
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        MCClientHelper.updateRender(pos);
    }
}