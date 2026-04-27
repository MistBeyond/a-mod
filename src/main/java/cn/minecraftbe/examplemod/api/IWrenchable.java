package cn.minecraftbe.examplemod.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public interface IWrenchable {
    void onWrenched(Level level, BlockPos pos, Direction face, Player player);

    default boolean useWrench(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction face) {
        if (!itemStack.is(Tags.Items.TOOLS_WRENCH)) return false;

        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            this.onWrenched(level, pos, face, player);
        }
        return true;
    }
}
