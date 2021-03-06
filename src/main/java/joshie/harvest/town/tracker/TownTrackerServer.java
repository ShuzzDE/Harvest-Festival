package joshie.harvest.town.tracker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import joshie.harvest.api.HFApi;
import joshie.harvest.api.calendar.CalendarDate;
import joshie.harvest.core.network.PacketHandler;
import joshie.harvest.town.data.TownData;
import joshie.harvest.town.data.TownDataServer;
import joshie.harvest.town.data.TownSavedData;
import joshie.harvest.town.packet.PacketNewTown;
import joshie.harvest.town.packet.PacketSyncTowns;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import static joshie.harvest.api.calendar.Season.SPRING;
import static joshie.harvest.town.BuildingLocations.MINE_ENTRANCE;

public class TownTrackerServer extends TownTracker<TownDataServer> {
    private static final CalendarDate FUTURE = new CalendarDate(0, SPRING, 999);
    public static final TownDataServer NULL_TOWN = new TownDataServer() {
        @Override
        public CalendarDate getBirthday() {
            return FUTURE;
        }
    };

    private TownSavedData data;
    private BiMap<UUID, Integer> townIDs = HashBiMap.create();

    @Override
    public TownDataServer getNullTown() {
        return NULL_TOWN;
    }

    public void setWorld(TownSavedData data, World world) {
        super.setWorld(world);
        this.data = data;
    }

    public void newDay(CalendarDate yesterday, CalendarDate today) {
        Cache<BlockPos, Boolean> isFar = CacheBuilder.newBuilder().build();
        for (TownDataServer town: townData) {
            town.newDay(getWorld(), isFar, yesterday, today);
        }
    }

    public void syncToPlayer(EntityPlayerMP player) {
        PacketHandler.sendToClient(new PacketSyncTowns(townData), player);
        for (TownDataServer town: townData) {
            town.getQuests().sync(player);
        }
    }

    @Override
    public BlockPos getCoordinatesForOverworldMine(@Nullable Entity entity, int mineID) {
        BlockPos default_ = super.getCoordinatesForOverworldMine(entity, mineID);
        UUID uuid = townIDs.inverse().get(mineID);
        if (uuid == null) return default_;
        TownData data = uuidMap.get(uuid);
        if (data == null) return default_;
        if (!data.hasBuilding(MINE_ENTRANCE.getBuilding())) return data.getTownCentre();
        BlockPos location = data.getCoordinatesFor(MINE_ENTRANCE);
        if (location != null) {
            Rotation rotation = getMineOrientation(mineID);
            if (rotation == Rotation.NONE) {
                return location.west(12).down(3);
            } else if (rotation == Rotation.CLOCKWISE_90) {
                return location.north(12).down(3);
            } else if (rotation == Rotation.CLOCKWISE_180) {
                return location.east(12).down(3);
            } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
                return location.south(12).down(3);
            }
        }

        return data.getTownCentre();
    }

    @Override
    public Rotation getMineOrientation(int mineID) {
        UUID uuid = townIDs.inverse().get(mineID);
        if (uuid == null) return Rotation.NONE;
        TownDataServer data = uuidMap.get(uuid);
        if (data == null || !data.hasBuilding(MINE_ENTRANCE.getBuilding())) return Rotation.NONE;
        return data.getFacingFor(MINE_ENTRANCE.getBuilding().getResource());
    }

    @Override
    public int getMineIDFromCoordinates(@Nonnull BlockPos pos) {
        TownData data = getClosestTownToBlockPos(pos, false);
        if (!data.hasBuilding(MINE_ENTRANCE.getBuilding())) return -1;
        if (townIDs.containsKey(data.getID())) {
            return townIDs.get(data.getID());
        } else return matchUUIDWithMineID(data.getID());
    }

    private int matchUUIDWithMineID(UUID uuid) {
        for (int i = 0; i < 32000; i++) { //Add a mineid to uuid entry
            if (!townIDs.inverse().containsKey(i)) {
                townIDs.put(uuid, i);
                markDirty();
                return i;
            }
        }

        return 0;
    }

    @Override
    public TownDataServer createNewTown(BlockPos pos) {
        TownDataServer data = new TownDataServer(getDimension(), pos, HFApi.calendar.getDate(getWorld()));
        townData.add(data);
        uuidMap.put(data.getID(), data);
        matchUUIDWithMineID(data.getID());
        PacketHandler.sendToDimension(getDimension(), new PacketNewTown(data)); //Sync to everyone on this dimension
        data.getQuests().sync(null);
        markDirty();
        return data;
    }

    /* ############# Saving ################*/
    public void markDirty() {
        data.markDirty();
    }

    public void readFromNBT(NBTTagCompound nbt) {
        townData = new HashSet<>(); //Reset the data
        NBTTagList dimensionTowns = nbt.getTagList("Towns", 10);
        for (int j = 0; j < dimensionTowns.tagCount(); j++) {
            NBTTagCompound tag = dimensionTowns.getCompoundTagAt(j);
            TownDataServer theData = new TownDataServer();
            theData.readFromNBT(tag);

            if (theData.getTownCentre().getY() > 0) {
                uuidMap.put(theData.getID(), theData);
                townData.add(theData);
            }
        }

        townIDs = HashBiMap.create();
        NBTTagList ids = nbt.getTagList("IDs", 10);
        for (int j = 0; j < ids.tagCount(); j++) {
            NBTTagCompound tag = ids.getCompoundTagAt(j);
            int id = tag.getInteger("ID");
            UUID uuid = UUID.fromString(tag.getString("UUID"));
            townIDs.put(uuid, id);
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList town_list = new NBTTagList();
        for (TownData data: townData) {
            NBTTagCompound townData = new NBTTagCompound();
            data.writeToNBT(townData);
            town_list.appendTag(townData);
        }

        nbt.setTag("Towns", town_list);

        //Ids
        NBTTagList ids = new NBTTagList();
        for (Entry<UUID, Integer> entry: townIDs.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("ID", entry.getValue());
            tag.setString("UUID", entry.getKey().toString());
            ids.appendTag(tag);
        }

        nbt.setTag("IDs", ids);
        return nbt;
    }
}
