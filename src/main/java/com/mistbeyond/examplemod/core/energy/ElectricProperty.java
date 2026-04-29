package com.mistbeyond.examplemod.core.energy;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface ElectricProperty {
    static <T extends ElectricProperty> MapCodec<T> simpleMapCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.LONG.fieldOf("voltage").forGetter(ElectricProperty::voltage),
                Codec.LONG.fieldOf("current").forGetter(ElectricProperty::current),
                Codec.INT.fieldOf("duration").forGetter(ElectricProperty::duration)
        ).apply(inst, factory::create));
    }

    static <T extends ElectricProperty> StreamCodec<RegistryFriendlyByteBuf, T> simpleStreamCodec(Factory<T> factory) {
        return StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, ElectricProperty::voltage,
                ByteBufCodecs.VAR_LONG, ElectricProperty::current,
                ByteBufCodecs.VAR_INT, ElectricProperty::duration,
                factory::create
        );
    }

    static <T extends ElectricProperty> Codec<T> simpleCodec(Factory<T> factory) {
        return simpleMapCodec(factory).codec();
    }

    static long power(ElectricProperty prop) {
        return Math.multiplyExact(prop.voltage(), prop.current());
    }

    static long totalEnergy(ElectricProperty prop) {
        return Math.multiplyExact(power(prop), prop.duration());
    }

    long voltage();

    long current();

    int duration();

    default VoltageTier voltageTier() {
        return VoltageTier.of(voltage());
    }

    @FunctionalInterface
    interface Factory<T extends ElectricProperty> {
        T create(long voltage, long current, int duration);
    }

    interface Provider<T extends ElectricProperty> {
        T getElectricProperty();
    }

    interface Common extends ElectricProperty {
        default long totalEnergy() {
            return ElectricProperty.totalEnergy(this);
        }

        default long power() {
            return ElectricProperty.power(this);
        }
    }
}
