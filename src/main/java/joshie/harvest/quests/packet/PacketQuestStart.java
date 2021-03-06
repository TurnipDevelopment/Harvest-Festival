package joshie.harvest.quests.packet;

import io.netty.buffer.ByteBuf;
import joshie.harvest.HarvestFestival;
import joshie.harvest.api.quests.Quest;
import joshie.harvest.core.handlers.GuiHandler;
import joshie.harvest.core.helpers.MCServerHelper;
import joshie.harvest.core.network.Packet;
import joshie.harvest.core.network.Packet.Side;
import joshie.harvest.quests.data.QuestData;
import joshie.harvest.town.TownHelper;
import joshie.harvest.town.data.TownDataServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import static joshie.harvest.town.TownHelper.getClosestTownToEntity;

@Packet(Side.SERVER)
public class PacketQuestStart extends PacketSyncData {
    private BlockPos pos;

    @SuppressWarnings("unused")
    public PacketQuestStart() {}
    public PacketQuestStart(BlockPos pos, Quest quest) {
        super(quest, null);
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void handlePacket(EntityPlayer player) {
        TownDataServer town = TownHelper.getClosestTownToEntity(player, false);
        QuestData data = town.getQuests();
        Quest quest = getClosestTownToEntity(player, false).getDailyQuest();
        if (quest != null && !data.getCurrent().contains(quest)) {
            data.startQuest(quest, true, town.getDailyQuest().writeToNBT(new NBTTagCompound()));
            player.openGui(HarvestFestival.instance, GuiHandler.QUEST_BOARD, player.world, pos.getX(), pos.getY(), pos.getZ());
            town.clearDailyQuest(player.world);
            MCServerHelper.markTileForUpdate(player.world, pos);
        }
    }
}