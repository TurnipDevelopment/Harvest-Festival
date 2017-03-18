package joshie.harvest.npcs.gift;

import joshie.harvest.api.npc.gift.GiftCategory;
import joshie.harvest.api.npc.gift.IGiftRegistry;
import joshie.harvest.core.util.holders.HolderRegistry;
import joshie.harvest.core.util.holders.HolderRegistrySet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GiftRegistry implements IGiftRegistry {
    private final HolderRegistrySet blacklist = new HolderRegistrySet();
    private final HolderRegistry<GiftCategory[]> registry = new HolderRegistry<GiftCategory[]>() {
        @Override
        public boolean matches(GiftCategory[] external, GiftCategory[] internal) {
            for (GiftCategory e: external) {
                for (GiftCategory i: internal) {
                    if (e == i) return true;
                }
            }

            return false;
        }
    };

    public HolderRegistry<GiftCategory[]> getRegistry() {
        return registry;
    }

    public boolean isBlacklisted(World world, EntityPlayer player, ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            return block.hasTileEntity(block.getStateForPlacement(world, BlockPos.ORIGIN, EnumFacing.DOWN, 0F, 0F, 0F, stack.getItemDamage(), player, stack));
        }

        return registry.getValueOf(stack) == null && (stack.getItem().isDamageable() || blacklist.contains(stack));
    }

    @Override
    public void addToBlacklist(Object... objects) {
        for (Object object: objects) blacklist.register(object);
    }

    @Override
    public void setCategories(Object object, GiftCategory... categories) {
        registry.register(object, categories);
    }

    @Override
    public boolean isGiftType(ItemStack stack, GiftCategory... categories) {
        return registry.matches(stack, categories);
    }
}
