package joshie.harvest.animals.tile;

import joshie.harvest.animals.HFAnimals;
import joshie.harvest.animals.entity.EntityHarvestChicken;
import joshie.harvest.api.HFApi;
import joshie.harvest.api.animals.AnimalStats;
import joshie.harvest.api.core.Size;
import joshie.harvest.api.ticking.DailyTickableBlock;
import joshie.harvest.api.ticking.DailyTickableBlock.Phases;
import joshie.harvest.core.base.tile.TileFillableSizedFaceable;
import joshie.harvest.core.helpers.EntityHelper;
import joshie.harvest.tools.ToolHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class TileIncubator extends TileFillableSizedFaceable {
    private static final int MAX_FILL = 7;
    private static final DailyTickableBlock TICKABLE = new DailyTickableBlock(Phases.POST) {
        @Override
        public boolean isStateCorrect(World world, BlockPos pos, IBlockState state) {
            return state.getBlock() == HFAnimals.SIZED;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void newDay(World world, BlockPos pos, IBlockState state) {
            TileIncubator incubator = (TileIncubator) world.getTileEntity(pos);
            if (incubator.activated && incubator.fillAmount > 0) {
                incubator.fillAmount--;
                if (incubator.fillAmount == 0) {
                    for (int i = 0; i < incubator.getBabyAmount(); i++) {
                        EntityHarvestChicken baby = new EntityHarvestChicken(world);
                        baby.setPositionAndUpdate(pos.getX(), pos.up().getY() + world.rand.nextDouble(), pos.getZ());
                        baby.setGrowingAge(-(24000 * HFAnimals.AGING_TIMER));
                        AnimalStats stats = EntityHelper.getStats(baby);
                        if (stats != null) {
                            stats.copyHappiness(incubator.relationship, 50D);
                        }

                        world.spawnEntity(baby);
                    }
                }

                incubator.saveAndRefresh();
            }
        }
    };

    private boolean activated;
    private int relationship;

    @Override
    public DailyTickableBlock getTickableForTile() {
        return TICKABLE;
    }

    @Override
    public boolean onActivated(EntityPlayer player, ItemStack held) {
        if (ToolHelper.isEgg(held)) {
            if (fillAmount == 0) {
                setFilled(HFApi.sizeable.getSize(held), MAX_FILL);
                NBTTagCompound tag = held.getOrCreateSubCompound("Data");
                if (tag.hasKey("Relationship")) {
                    relationship = tag.getInteger("Relationship");
                } else relationship = 0;

                activated = true;
                held.splitStack(1);
                return true;
            }
        }

        return false;
    }

    private int getBabyAmount() {
        int amount = 1;
        if (size == Size.MEDIUM && world.rand.nextInt(20) == 0) amount++;
        if (size == Size.LARGE && world.rand.nextInt(10) == 0) amount++;
        if (size == Size.LARGE && world.rand.nextInt(50) == 0) amount++;
        return amount;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        relationship = nbt.getInteger("Relationship");
        activated = nbt.getBoolean("Activated");
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Relationship", relationship);
        nbt.setBoolean("Activated", activated);
        return super.writeToNBT(nbt);
    }
}