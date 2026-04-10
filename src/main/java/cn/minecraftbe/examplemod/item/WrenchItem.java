package cn.minecraftbe.examplemod.item;

import cn.minecraftbe.examplemod.api.IWrenchable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.NonNull;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        var level = context.getLevel();
        var blockPos = context.getClickedPos();
        var blockState = level.getBlockState(blockPos);

        if (blockState.getBlock() instanceof IWrenchable wrenchable) {
            if (!level.isClientSide()) {
                wrenchable.onWrenched(level, blockPos, context.getClickedFace(), context.getPlayer());
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
