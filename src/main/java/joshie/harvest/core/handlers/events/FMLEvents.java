package joshie.harvest.core.handlers.events;

import joshie.harvest.core.config.Calendar;
import joshie.harvest.core.handlers.HFTracker;
import joshie.harvest.core.helpers.CalendarHelper;
import joshie.harvest.core.network.PacketHandler;
import joshie.harvest.core.network.PacketSetCalendar;
import joshie.harvest.player.PlayerTracker;
import joshie.harvest.player.PlayerTrackerServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class FMLEvents {
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (player instanceof EntityPlayerMP) {
            HFTracker.getPlayerTracker(player).getStats().setBirthday();
            PacketHandler.sendToClient(new PacketSetCalendar(HFTracker.getCalendar().getDate()), (EntityPlayerMP) player);
            PlayerTracker data = HFTracker.getPlayerTracker(player);
            data.syncPlayerStats();
            data.getQuests().syncQuests();
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.phase != Phase.END) return;
        World world = MinecraftServer.getServer().getEntityWorld();
        if (world.getTotalWorldTime() % Calendar.TICKS_PER_DAY == 0) {
            newDay(false);
        }
    }

    public static void newDay(final boolean forced) {
        int daysPassed = CalendarHelper.getTotalDays(HFTracker.getCalendar().getDate());
        int serverDays = (int) Math.floor(DimensionManager.getWorld(0).getWorldTime() / Calendar.TICKS_PER_DAY);
        if (daysPassed <= serverDays || forced) {
            HFTracker.getCalendar().newDay();
        }
    }
}
