package cn.minecraftbe.examplemod.block;

import cn.minecraftbe.examplemod.block.entity.BlockEntityTypes;
import cn.minecraftbe.examplemod.block.entity.TestMachineBlockEntity;
import com.mojang.serialization.MapCodec;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

@Slf4j
public class TestMachine extends SingleBlockMachine implements EntityBlock {
    private static final MapCodec<TestMachine> CODEC = simpleCodec(TestMachine::new);

    public TestMachine(Properties properties) {
        super(properties);
    }

    protected static <T extends BlockEntity> @Nullable BlockEntityTicker<T> createMachineTicker(
            Level level, BlockEntityType<T> actualType
    ) {
        if (level instanceof ServerLevel serverLevel) {
            return createTickerHelper(
                    actualType,
                    BlockEntityTypes.TEST_MACHINE.get(),
                    (_, blockPos, state, entity) ->
                            TestMachineBlockEntity.serverTick(serverLevel, blockPos, state, entity)
            );
        }
        return null;
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, BlockState state, Player player) {
        var entity = level.getBlockEntity(pos);
        if (entity instanceof TestMachineBlockEntity) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
    }
}
