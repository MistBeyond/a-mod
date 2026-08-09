package com.mistbeyond.examplemod.block;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.block.entity.logistic.WireBlockEntity;
import com.mistbeyond.examplemod.core.logistic.energy.IEnergyComponent;
import com.mistbeyond.registry.RegisterBlock;
import com.mistbeyond.registry.SubscribeRegistration;
import com.mistbeyond.registry.impl.BlockRegistration;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@RegisterBlock
public class WireBlock extends BaseEntityBlock implements IConnectableBlock {
    private static final VoxelShape CORE_SHAPE = box(6, 6, 6, 10, 10, 10);
    private static final Map<Direction, VoxelShape> SEGMENT_SHAPES;
    private static final MapCodec<WireBlock> CODEC = simpleCodec(WireBlock::new);

    public WireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(initialConnectionState(this.stateDefinition.any()));
    }

    @SubscribeRegistration
    private static void registerWire(BlockRegistration registration) {
        registration.register(
                Ids.WIRE,
                WireBlock::new,
                p -> p.noOcclusion().dynamicShape().instabreak().sound(SoundType.NETHERITE_BLOCK)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new WireBlockEntity(worldPosition, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState blockState, BlockEntityType<T> type
    ) {
        return level instanceof ServerLevel serverLevel
                ? createTickerHelper(
                type,
                Init.REGISTRAR.<WireBlockEntity>blockEntityTyped(Ids.WIRE),
                (_, pos, state, entity) -> WireBlockEntity.serverTick(serverLevel, pos, state, entity)
        )
                : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getPlacementConnectionState(defaultBlockState(), context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        registerConnectionProperties(builder);
    }

    @Override
    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof WireBlock) {
            return true;
        }
        if (level instanceof Level l
                && l.getCapability(Capabilities.Energy.BLOCK, neighborPos, direction.getOpposite()) != null) {
            return true;
        }
        return level.getBlockEntity(neighborPos) instanceof IEnergyComponent component
                && component.isConnectTo(direction.getOpposite());
    }

    @Override
    public VoxelShape getCoreShape() {
        return CORE_SHAPE;
    }

    @Override
    public VoxelShape getSegmentShape(Direction direction) {
        return SEGMENT_SHAPES.get(direction);
    }

    @Override
    public boolean isConnectionDisabled(BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof WireBlockEntity wire && wire.isConnectionDisabled(direction);
    }

    @Override
    public void setConnectionDisabled(Level level, BlockPos pos, Direction direction, boolean disabled) {
        if (level.getBlockEntity(pos) instanceof WireBlockEntity wire) {
            wire.setConnectionDisabled(direction, disabled);
        }
    }

    @Override
    public void onConnectionsChanged(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof WireBlockEntity wire) {
            wire.onConnectionChanged();
        }
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (level instanceof ServerLevel
                && !oldState.equals(newState)
                && level.getBlockEntity(pos) instanceof WireBlockEntity wire) {
            wire.onConnectionChanged();
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        return updateConnectionState(state, level, pos, direction);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context
    ) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context
    ) {
        return shapeFor(state);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    static {
        EnumMap<Direction, VoxelShape> segments = new EnumMap<>(Direction.class);
        segments.put(Direction.NORTH, box(6, 6, 0, 10, 10, 6));
        segments.put(Direction.SOUTH, box(6, 6, 10, 10, 10, 16));
        segments.put(Direction.EAST, box(10, 6, 6, 16, 10, 10));
        segments.put(Direction.WEST, box(0, 6, 6, 6, 10, 10));
        segments.put(Direction.UP, box(6, 10, 6, 10, 16, 10));
        segments.put(Direction.DOWN, box(6, 0, 6, 10, 6, 10));
        SEGMENT_SHAPES = Map.copyOf(segments);
    }
}
