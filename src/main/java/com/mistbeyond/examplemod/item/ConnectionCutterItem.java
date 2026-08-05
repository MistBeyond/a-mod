package com.mistbeyond.examplemod.item;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.block.IConnectableBlock;
import com.mistbeyond.examplemod.core.registry.RegisterItem;
import com.mistbeyond.examplemod.core.registry.SubscribeRegistration;
import com.mistbeyond.examplemod.core.registry.impl.ItemRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

@RegisterItem
public class ConnectionCutterItem extends Item {
    public ConnectionCutterItem(Properties properties) {
        super(properties);
    }

    @SubscribeRegistration
    private static void register(ItemRegistration registration) {
        registration.register(Ids.CONNECTION_CUTTER, ConnectionCutterItem::new, p -> p.stacksTo(1));
    }

    private static void toggleConnection(Level level, BlockPos pos, Direction face, IConnectableBlock connectable) {
        BlockState state = level.getBlockState(pos);
        BooleanProperty property = IConnectableBlock.PROPERTY_BY_DIRECTION.get(face);
        boolean connected = state.getValue(property);
        boolean newConnected = !connected;
        connectable.setConnectionDisabled(level, pos, face, !newConnected);
        BlockState updated = state.setValue(property, newConnected);
        level.setBlock(pos, updated, Block.UPDATE_ALL);
        connectable.onConnectionsChanged(level, pos);

        BlockPos neighborPos = pos.relative(face);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof IConnectableBlock neighborConnectable) {
            Direction neighborDirection = face.getOpposite();
            neighborConnectable.setConnectionDisabled(level, neighborPos, neighborDirection, !newConnected);
            BlockState neighborUpdated = neighborState.setValue(
                    IConnectableBlock.PROPERTY_BY_DIRECTION.get(neighborDirection),
                    newConnected
            );
            level.setBlock(neighborPos, neighborUpdated, Block.UPDATE_ALL);
            neighborConnectable.onConnectionsChanged(level, neighborPos);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        if (!(level.getBlockState(pos).getBlock() instanceof IConnectableBlock connectable)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            toggleConnection(level, pos, face, connectable);
        }
        return InteractionResult.SUCCESS;
    }
}
