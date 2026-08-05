package com.mistbeyond.examplemod.block;

import com.mistbeyond.examplemod.core.logistic.IConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Block-layer extension of {@link IConnectable}. Owns the six-face connection state,
 * placement/update refresh rules, and the dynamic collision/volume contract.
 *
 * <p>Read {@code docs/features/connectable-blocks.md} before modifying connectable block logic.
 * Any change to this code must also update that document.
 */
public interface IConnectableBlock extends IConnectable {
    BooleanProperty NORTH = BooleanProperty.create("north");
    BooleanProperty SOUTH = BooleanProperty.create("south");
    BooleanProperty EAST = BooleanProperty.create("east");
    BooleanProperty WEST = BooleanProperty.create("west");
    BooleanProperty UP = BooleanProperty.create("up");
    BooleanProperty DOWN = BooleanProperty.create("down");
    Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = createConnectionProperties();

    private static Map<Direction, BooleanProperty> createConnectionProperties() {
        EnumMap<Direction, BooleanProperty> properties = new EnumMap<>(Direction.class);
        properties.put(Direction.NORTH, NORTH);
        properties.put(Direction.SOUTH, SOUTH);
        properties.put(Direction.EAST, EAST);
        properties.put(Direction.WEST, WEST);
        properties.put(Direction.UP, UP);
        properties.put(Direction.DOWN, DOWN);
        return Map.copyOf(properties);
    }

    VoxelShape getCoreShape();

    VoxelShape getSegmentShape(Direction direction);

    /**
     * Whether automatic connection is currently allowed in that direction, taking manual
     * disconnects into account.
     */
    default boolean canAutoConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        return canConnectTo(level, pos, direction) && !isConnectionDisabled(level, pos, direction);
    }

    default boolean isConnectionDisabled(BlockGetter level, BlockPos pos, Direction direction) {
        return false;
    }

    default void setConnectionDisabled(Level level, BlockPos pos, Direction direction, boolean disabled) {
    }

    /**
     * Called after the connection state has been updated in the level.
     */
    default void onConnectionsChanged(Level level, BlockPos pos) {
    }

    /**
     * Computes the state used when this block is placed. When the placement face touches an
     * allowed connectable, the contact face is connected; any neighbour that already connects
     * towards this position is also connected.
     */
    default BlockState getPlacementConnectionState(BlockState state, BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockPos newPos = context.getClickedPos();
        Direction directionToClicked = clickedFace.getOpposite();
        BlockPos clickedPos = newPos.relative(clickedFace.getOpposite());
        BlockState result = initialConnectionState(state);
        boolean clickedDisabled = context.getLevel().getBlockEntity(clickedPos) instanceof IConnectableBlock clicked
                && clicked.isConnectionDisabled(context.getLevel(), clickedPos, clickedFace);
        if (!clickedDisabled && canAutoConnectTo(context.getLevel(), newPos, directionToClicked)) {
            result = result.setValue(PROPERTY_BY_DIRECTION.get(directionToClicked), true);
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = newPos.relative(direction);
            BlockState neighborState = context.getLevel().getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof IConnectableBlock neighbor
                    && neighborState.getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))
                    && canAutoConnectTo(context.getLevel(), newPos, direction)) {
                result = result.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
            }
        }
        return result;
    }

    /**
     * Connects the contact face when the neighbour connects towards this block. Never disconnects
     * an existing connection automatically.
     */
    default BlockState updateConnectionState(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof IConnectableBlock neighbor
                && neighborState.getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))
                && canAutoConnectTo(level, pos, direction)) {
            return state.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
        }
        return state;
    }

    default VoxelShape shapeFor(BlockState state) {
        VoxelShape shape = getCoreShape();
        for (Direction direction : Direction.values()) {
            if (state.getValue(PROPERTY_BY_DIRECTION.get(direction))) {
                shape = Shapes.or(shape, getSegmentShape(direction));
            }
        }
        return shape;
    }

    default BlockState initialConnectionState(BlockState state) {
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            result = result.setValue(PROPERTY_BY_DIRECTION.get(direction), false);
        }
        return result;
    }

    default void registerConnectionProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }
}
