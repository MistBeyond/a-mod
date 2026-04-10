package cn.minecraftbe.examplemod.block;

import cn.minecraftbe.examplemod.api.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class Machine extends Block implements IWrenchable {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public Machine(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    public void onWrenched(Level level, BlockPos pos, Direction clickedFace, Player player) {
        var blockState = level.getBlockState(pos);
        var axis = clickedFace.getAxis();
        switch (axis) {
            case X, Z -> player.sendOverlayMessage(
                    Component.translatable("examplemod.overlaymessage.wrench.rotation_failed")
                            .append(" on " + axis.getName().toUpperCase() + " axis")
            );
            case Y -> {
                BlockState newState;
                if (clickedFace == Direction.UP)
                    newState = blockState.setValue(FACING, blockState.getValue(FACING).getClockWise());
                else
                    newState = blockState.setValue(FACING, blockState.getValue(FACING).getCounterClockWise());
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }

        PlayerList playerList = Objects.requireNonNull(level.getServer()).getPlayerList();
        playerList.broadcastSystemMessage(Component.literal(
                        String.format("玩家<%s>使用了扳手, 目前分支: Server Thread", player.getName().getString())),
                false
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
