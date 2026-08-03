package com.mistbeyond.examplemod.integration.jade.provider;

import com.mistbeyond.examplemod.Ids;
import com.mistbeyond.examplemod.block.entity.machine.ElectricMachine;
import com.mistbeyond.examplemod.core.energy.EUEnergyHandler;
import com.mistbeyond.examplemod.integration.jade.ModJadeIds;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;

public class EUStorageProvider implements StreamServerDataProvider<BlockAccessor, EUStorageProvider.Data> {
    public static final EUStorageProvider INSTANCE = new EUStorageProvider();

    private static <M extends BlockEntity & ElectricMachine> M typedElectricMachine(BlockAccessor accessor) {
        return accessor.typedBlockEntity();
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        EUEnergyHandler handler = typedElectricMachine(accessor).getEnergyHandler();
        var voltageHint = handler.getVoltageTier().asString();
        return new Data(handler.getEUCapacity(), handler.getEUAmount(), handler.getVoltageTier().value, voltageHint);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return ModJadeIds.EU_STORAGE;
    }

    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();
        public static final Element PROGRESS_OVERLAY = JadeUI.horizontalTiledSprite(RenderPipelines.GUI_TEXTURED, Ids.thisMod("energy_progress"), 16, 16);

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var tags = accessor.getServerData().get(ModJadeIds.EU_STORAGE.toString());
            if (tags == null) {
                return;
            }
            var dataOptional = accessor.decodeFromNbt(Data.STREAM_CODEC, tags);
            if (dataOptional.isPresent()) {
                var data = dataOptional.get();
                if (data.capacity == 0) return;
                float progress = (float) data.amount / data.capacity;
                var amountStr = "§f" + IDisplayHelper.get().humanReadableNumber(data.amount, "EU", false) + "§r";
                var capacityStr = IDisplayHelper.get().humanReadableNumber(data.capacity, "EU", false);
                tooltip.add(JadeUI.progress(new ProgressView(
                        ProgressView.Part.of(progress, PROGRESS_OVERLAY),
                        Component.translatable("gui.examplemod.energy.eu", amountStr, capacityStr),
                        JadeUI.progressStyle(),
                        BoxStyle.nestedBox()
                )));
                tooltip.add(Component.translatable("gui.examplemod.energy.voltage", data.voltage + "V", data.voltageHint));
            }
        }

        @Override
        public Identifier getUid() {
            return ModJadeIds.EU_STORAGE;
        }
    }

    public record Data(long capacity, long amount, long voltage, String voltageHint) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, Data::capacity,
                ByteBufCodecs.VAR_LONG, Data::amount,
                ByteBufCodecs.VAR_LONG, Data::voltage,
                ByteBufCodecs.STRING_UTF8, Data::voltageHint,
                Data::new
        );
    }
}
