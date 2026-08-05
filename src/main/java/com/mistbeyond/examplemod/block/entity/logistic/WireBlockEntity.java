package com.mistbeyond.examplemod.block.entity.logistic;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.block.IConnectableBlock;
import com.mistbeyond.examplemod.block.WireBlock;
import com.mistbeyond.examplemod.core.Values;
import com.mistbeyond.examplemod.core.logistic.energy.EUTransferInfo;
import com.mistbeyond.examplemod.core.logistic.energy.IWire;
import com.mistbeyond.examplemod.core.logistic.energy.WireMeltdownState;
import com.mistbeyond.examplemod.core.registry.ProvideFactory;
import com.mistbeyond.examplemod.core.registry.RegisterBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumSet;

@RegisterBlockEntityType(Ids.WIRE)
public class WireBlockEntity extends EnergyComponentBlockEntity implements IWire {
    private static final float MAX_CURRENT = Values.DEFAULT_OUTPUT_CURRENT;
    private static final long RESISTANCE = 1L;
    private static final int OVER_CURRENT_TICKS = 20;
    private final WireMeltdownState meltdownState = new WireMeltdownState(MAX_CURRENT, OVER_CURRENT_TICKS);
    private final EnumSet<Direction> disabledConnections = EnumSet.noneOf(Direction.class);
    private EUTransferInfo lastElectricLoad = EUTransferInfo.ZERO;

    public WireBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(Init.REGISTRAR.blockEntityType(Ids.WIRE), worldPosition, blockState);
    }

    public static void serverTick(ServerLevel serverLevel, BlockPos pos, BlockState state, WireBlockEntity entity) {
        entity.tickMeltdown();
    }

    @ProvideFactory
    private static BlockEntityType.BlockEntitySupplier<?> provideFactory() {
        return WireBlockEntity::new;
    }

    @Override
    public EUTransferInfo getElectricLoad() {
        return lastElectricLoad;
    }

    @Override
    public void applyElectricLoad(EUTransferInfo electricLoad) {
        lastElectricLoad = electricLoad;
        meltdownState.recordLoad(electricLoad.current());
    }

    @Override
    public long getResistance() {
        return RESISTANCE;
    }

    @Override
    public void meltdown() {
        if (!(level instanceof ServerLevel serverLevel) || isRemoved()) {
            return;
        }
        serverLevel.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        serverLevel.setBlock(worldPosition, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public EnumSet<Direction> connections() {
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        BlockState state = getBlockState();
        if (state.getBlock() instanceof WireBlock) {
            for (Direction direction : Direction.values()) {
                if (state.getValue(IConnectableBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                    result.add(direction);
                }
            }
        }
        return result;
    }

    @Override
    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos).getBlock() instanceof IConnectableBlock connectable
                && connectable.canConnectTo(level, pos, direction);
    }

    public boolean isConnectionDisabled(Direction direction) {
        return disabledConnections.contains(direction);
    }

    public void setConnectionDisabled(Direction direction, boolean disabled) {
        if (disabled) {
            disabledConnections.add(direction);
        } else {
            disabledConnections.remove(direction);
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        meltdownState.restore(input.getIntOr("over_current_ticks", 0), input.getBooleanOr("meltdown_pending", false));
        loadDisabledConnections(input.getLongOr("disabled_connections", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("over_current_ticks", meltdownState.overCurrentTicks());
        output.putBoolean("meltdown_pending", meltdownState.isMeltdownPending());
        output.putLong("disabled_connections", disabledConnectionsMask());
    }

    @Override
    public void setBlockState(BlockState state) {
        BlockState old = getBlockState();
        super.setBlockState(state);
        if (level instanceof ServerLevel && !old.equals(state)) {
            onConnectionChanged();
        }
    }

    private void tickMeltdown() {
        int ticksBefore = meltdownState.overCurrentTicks();
        boolean pendingBefore = meltdownState.isMeltdownPending();
        if (meltdownState.tick()) {
            meltdown();
            return;
        }
        if (meltdownState.overCurrentTicks() != ticksBefore || meltdownState.isMeltdownPending() != pendingBefore) {
            setChanged();
        }
    }

    private long disabledConnectionsMask() {
        long mask = 0;
        for (Direction direction : disabledConnections) {
            mask |= 1L << direction.ordinal();
        }
        return mask;
    }

    private void loadDisabledConnections(long mask) {
        disabledConnections.clear();
        for (Direction direction : Direction.values()) {
            if ((mask & (1L << direction.ordinal())) != 0) {
                disabledConnections.add(direction);
            }
        }
    }
}
