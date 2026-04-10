package cn.minecraftbe.examplemod.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IWrenchable {
    void onWrenched(Level level, BlockPos pos, Direction face, Player player);
}
