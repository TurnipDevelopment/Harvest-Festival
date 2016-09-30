package joshie.harvest.buildings.placeable.blocks;

import joshie.harvest.buildings.placeable.Placeable;
import joshie.harvest.core.util.Direction;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumMap;

public class PlaceableBlock extends Placeable {
    protected transient EnumMap<Direction, IBlockState> states = new EnumMap<>(Direction.class);
    protected IBlockState state;

    public PlaceableBlock() {}
    public PlaceableBlock(BlockPos pos, PlaceableBlock block) {
        state = block.state;
        for (Direction direction: Direction.values()) {
            states.put(direction, direction.withDirection(state));
        }
    }

    public PlaceableBlock(IBlockState state, int x, int y, int z) {
        this.state = state;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PlaceableBlock(IBlockState state, BlockPos pos) {
        this.state = state;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    @Override
    public PlaceableBlock init() {
        if (states == null) states = new EnumMap<>(Direction.class);
        for (Direction direction: Direction.values()) {
            states.put(direction, direction.withDirection(state));
        }

        return this;
    }

    public Block getBlock() {
        return state.getBlock();
    }

    public IBlockState getState() {
        return state;
    }

    public IBlockState getTransformedState(Direction direction) {
        return direction.withDirection(state);
    }

    public PlaceableBlock copyWithOffset(BlockPos pos, Direction direction) {
        return new PlaceableBlock(getTransformedPosition(pos, direction), this);
    }

    @Override
    public boolean canPlace(ConstructionStage stage) {
        return stage == ConstructionStage.BUILD;
    }

    public boolean prePlace (World world, BlockPos pos, Direction direction) {
        return world.getBlockState(pos).getBlockHardness(world, pos) != -1.0F;
    }

    @Override
    public final boolean place (World world, BlockPos pos, Direction direction, boolean playSound) {
        if (!prePlace(world, pos, direction)) return false;
        IBlockState state = getTransformedState(direction);
        if (world.getBlockState(pos) == state) return false;
        if(playSound) world.playEvent(null, 2001, pos, Block.getStateId(state));
        boolean result = world.setBlockState(pos, state, 2);
        if (result) {
            postPlace(world, pos, direction);
        }

        return result;
    }

    public void postPlace (World world, BlockPos pos, Direction direction) {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if ((!(obj instanceof PlaceableBlock))) return false;
        PlaceableBlock other = (PlaceableBlock) obj;
        if (getX() != other.getX()) return false;
        if (getY() != other.getY()) return false;
        return (getZ() == other.getZ());
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + getX();
        result = prime * result + getY();
        result = prime * result + getZ();
        return result;
    }
}