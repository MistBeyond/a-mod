package cn.minecraftbe.examplemod.integration.jade;

import cn.minecraftbe.examplemod.block.entity.TestMachineBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

import java.util.List;

public class TestMachineProvider implements StreamServerDataProvider<BlockAccessor, TestMachineProvider.Data> {
    public static final TestMachineProvider INSTANCE = new TestMachineProvider();

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        TestMachineBlockEntity entity = accessor.typedBlockEntity();
        return new Data((float) entity.getSmeltProgress(), entity.getItemHandler().copyToList());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.Ids.TEST_MACHINE;
    }


    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var data = TestMachineProvider.INSTANCE.decodeFromData(accessor).orElse(null);
            if (data != null) {
                tooltip.add(JadeUI.item(data.stacks.getFirst()));
                tooltip.append(JadeUI.item(data.stacks.get(1)));
                tooltip.append(JadeUI.progressArrow(data.smeltProgress()).alignSelfCenter().settings($ ->
                        $.paddingHorizontal(10))
                );
                tooltip.append(JadeUI.item(data.stacks.getLast()));
            }
        }

        @Override
        public Identifier getUid() {
            return JadePlugin.Ids.TEST_MACHINE;
        }
    }

    public record Data(float smeltProgress, List<ItemStack> stacks) {
        public static StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Data::smeltProgress,
                ItemStack.OPTIONAL_LIST_STREAM_CODEC, Data::stacks,
                Data::new
        );
    }
}
