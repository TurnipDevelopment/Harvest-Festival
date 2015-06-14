package joshie.harvest.animals;

import java.util.HashMap;

import joshie.harvest.api.animals.IAnimalData;
import joshie.harvest.core.helpers.UUIDHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AnimalTrackerClient extends AnimalTracker {
    private HashMap<IAnimalData, Boolean> canProduce = new HashMap();

    @Override
    public boolean canProduceProduct(IAnimalData animal) {
        Boolean can = canProduce.get(animal);
        if (can == null) {
            canProduce.put(animal, true);
        }

        return can == null ? true : can;
    }

    @Override
    public void setCanProduceProduct(IAnimalData animal, boolean value) {
        canProduce.put(animal, value);
    }

    @Override
    public void onDeath(IAnimalData animal) {
        canProduce.remove(UUIDHelper.getEntityUUID(animal.getAnimal()));
    }
}