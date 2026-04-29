package com.mistbeyond.examplemod.block.machine;

import com.mistbeyond.examplemod.block.Wrenchable;
import com.mistbeyond.examplemod.block.state.StateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public abstract class SingleBlockMachine extends BaseEntityBlock implements Wrenchable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WORKING = StateProperties.WORKING;

    public SingleBlockMachine(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WORKING, false));
    }

    @Override
    public void onWrenched(Level level, BlockPos pos, Direction clickedFace, Player player) {
        var blockState = level.getBlockState(pos);
        var axis = clickedFace.getAxis();
        switch (axis) {
            case X, Z ->
                    player.sendOverlayMessage(Component.translatable("examplemod.overlaymessage.wrench.rotation_failed", axis.getName().toUpperCase()));
            case Y -> level.setBlock(pos, this.rotate(blockState, Rotation.CLOCKWISE_90), Block.UPDATE_ALL);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING);
    }

    @Override
    public abstract @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type);

    protected abstract void openContainer(Level level, BlockPos pos, BlockState state, Player player);

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            this.openContainer(level, pos, state, player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (useWrench(itemStack, state, level, pos, player, hand, hitResult.getDirection())) {
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
}
