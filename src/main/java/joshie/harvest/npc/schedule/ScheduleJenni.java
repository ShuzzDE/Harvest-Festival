package joshie.harvest.npc.schedule;

import joshie.harvest.api.buildings.BuildingLocation;
import joshie.harvest.api.calendar.Season;
import joshie.harvest.api.calendar.Weekday;
import joshie.harvest.api.npc.INPC;
import joshie.harvest.api.npc.ISchedule;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import static joshie.harvest.town.BuildingLocations.*;

@SuppressWarnings("unused")
public class ScheduleJenni implements ISchedule {
    @Override
    public BuildingLocation getTarget(World world, EntityLiving entity, INPC npc, Season season, Weekday day, long time) {
        if (time >= 9000L && time < 19000L) return GENERALGARDEN;
        else if (time >= 19000L && time < 22000L) return CAFEKITCHEN;
        else if (time >= 22000L && time < 24000L) return PONDBACK;
        else return GENERALBED;
    }
}
