package com.mistbeyond.examplemod.block;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.block.entity.TestMachineBlockEntity;
import com.mistbeyond.examplemod.core.registry.RegisterBlock;
import com.mistbeyond.examplemod.core.registry.SubscribeRegistration;
import com.mistbeyond.examplemod.core.registry.impl.BlockRegistration;
import com.mojang.serialization.MapCodec;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
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

@Slf4j
@RegisterBlock
public class TestMachine extends BaseEntityBlock implements Wrenchable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final MapCodec<TestMachine> CODEC = simpleCodec(TestMachine::new);

    public TestMachine(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    protected static <T extends BlockEntity> @Nullable BlockEntityTicker<T> createMachineTicker(
            Level level, BlockEntityType<T> actualType
    ) {
        if (level instanceof ServerLevel serverLevel) {
            return createTickerHelper(
                    actualType,
                    Init.REGISTRAR.<TestMachineBlockEntity>blockEntityTyped(Ids.TEST_MACHINE),
                    (_, blockPos, state, entity) ->
                            TestMachineBlockEntity.serverTick(serverLevel, blockPos, state, entity)
            );
        }
        return null;
    }

    @SubscribeRegistration
    private static void registerTestMachine(BlockRegistration registration) {
        registration.register(Ids.TEST_MACHINE, TestMachine::new, p -> p.sound(SoundType.NETHERITE_BLOCK));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new TestMachineBlockEntity(worldPosition, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createMachineTicker(level, type);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            level.playLocalSound(pos, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

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

    protected void openContainer(Level level, BlockPos pos, BlockState state, Player player) {
        var entity = level.getBlockEntity(pos);
        if (entity instanceof TestMachineBlockEntity) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
    }
}
