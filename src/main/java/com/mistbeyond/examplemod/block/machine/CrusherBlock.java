package com.mistbeyond.examplemod.block.machine;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.Init;
import com.mistbeyond.examplemod.block.entity.machine.CrusherEntity;
import com.mistbeyond.examplemod.core.registry.RegisterBlock;
import com.mistbeyond.examplemod.core.registry.SubscribeRegistration;
import com.mistbeyond.examplemod.core.registry.impl.BlockRegistration;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

@RegisterBlock
public class CrusherBlock extends SingleBlockMachine {
    public static final MapCodec<CrusherBlock> CODEC = simpleCodec(CrusherBlock::new);

    public CrusherBlock(Properties properties) {
        super(properties);
    }

    @SubscribeRegistration
    private static void registerCrusher(BlockRegistration registration) {
        registration.register(Ids.CRUSHER, CrusherBlock::new, p -> p.sound(SoundType.NETHERITE_BLOCK));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new CrusherEntity(worldPosition, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return level instanceof ServerLevel serverLevel
                ? createTickerHelper(type, Init.REGISTRAR.<CrusherEntity>blockEntityTyped(Ids.CRUSHER), (_, pos, state, entity) -> CrusherEntity.serverTick(serverLevel, pos, state, entity))
                : null;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, BlockState state, Player player) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CrusherEntity) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
