package cn.minecraftbe.examplemod.block;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Objects;

@Slf4j
public class TestBlock extends Machine {
    public TestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            PlayerList playerList = Objects.requireNonNull(level.getServer()).getPlayerList();
            playerList.broadcastSystemMessage(Component.literal(
                    String.format("玩家<%s>触发了事件, 目前分支: Server Thread", player.getName().getString())),
                    false
            );
        } else {
            player.sendOverlayMessage(Component.literal("检测到事件, 目前分支: Client Thread"));
        }

        return InteractionResult.CONSUME;
    }
}
