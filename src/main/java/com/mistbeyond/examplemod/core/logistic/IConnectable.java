package com.mistbeyond.examplemod.core.logistic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import java.util.EnumSet;

/**
 * Pure connection contract shared by connectable blocks and connectable network
 * components. Implementations provide their own {@link #connections()} and
 * {@link #canConnectTo(BlockGetter, BlockPos, Direction)} rules.
 *
 * <p>Read {@code docs/features/connectable-blocks.md} before extending this interface.
 * Any change to this code must be reflected in that document.
 */
public interface IConnectable {
    /**
     * @param level     the level containing the block
     * @param pos       the position of this connectable
     * @param direction the direction towards the candidate neighbour
     * @return {@code true} when this connectable can connect in that direction
     */
    boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction);

    default EnumSet<Direction> connections() {
        return EnumSet.noneOf(Direction.class);
    }

    default boolean isConnectTo(Direction direction) {
        return connections().contains(direction);
    }

    /**
     * {@code a.isConnectWith(b) <=> b.isConnectWith(a)}
     */
    default boolean isConnectWith(IConnectable other, BlockPos thisPos, BlockPos otherPos) {
        if (!thisPos.closerThan(otherPos, 1.01)) {
            return false;
        }
        for (Direction direction : connections()) {
            if (other.isConnectTo(direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }
}
