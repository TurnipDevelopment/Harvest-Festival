package joshie.harvest.animals;

import joshie.harvest.animals.ai.EntityAIEat;
import joshie.harvest.api.HFApi;
import joshie.harvest.api.animals.IAnimalData;
import joshie.harvest.api.animals.IAnimalTracked;
import joshie.harvest.api.animals.IAnimalType;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityHarvestCow extends EntityCow implements IAnimalTracked {
    private IAnimalData data;
    private IAnimalType type;

    public EntityHarvestCow(World world) {
        super(world);
        setSize(1.4F, 1.4F);
        data = HFApi.ANIMALS.newData(this);
        type = HFApi.ANIMALS.getType(this);
        tasks.addTask(3, new EntityAIEat(this));
    }

    @Override
    public IAnimalData getData() {
        return data;
    }

    @Override
    public IAnimalType getType() {
        return type;
    }
    
    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack held = player.getCurrentEquippedItem();
        if (held != null) {
            if (HFApi.ANIMALS.canEat(type.getFoodTypes(), held)) {
                if (!worldObj.isRemote) {
                    data.feed(player);
                }
                
                return true;
            }
        }
        
        return false;
    }

    @Override
    public EntityCow createChild(EntityAgeable ageable) {
        return new EntityHarvestCow(this.worldObj);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        data.readFromNBT(nbt);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        data.writeToNBT(nbt);
    }
}