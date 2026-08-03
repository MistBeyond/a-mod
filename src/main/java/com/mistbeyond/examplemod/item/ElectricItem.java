package com.mistbeyond.examplemod.item;

import com.mistbeyond.examplemod.core.VoltageTier;
import com.mistbeyond.examplemod.core.energy.ElectricProperty;
import com.mistbeyond.examplemod.core.energy.VoltageTierLimited;
import com.mistbeyond.examplemod.item.componet.ModDataComponents;
import lombok.Getter;
import net.minecraft.world.item.Item;

/**
 * A zero energy {@link ModDataComponents#ENERGY component} is implicitly added to it.
 */
@Getter
public class ElectricItem extends Item implements ElectricProperty.Provider<ElectricItem.ChargeInfo> {
    protected final ChargeInfo chargeInfo;

    /**
     * Whether this item is an infinite energy source, backed by {@link com.mistbeyond.examplemod.core.energy.InfiniteEUHandler}.
     * {@link #getElectricProperty()} still returns a display property ({@link ChargeInfo#INFINITY}) for infinite items.
     */
    private final boolean infinite;

    public ElectricItem(Properties properties, VoltageTier ioVoltage) {
        this(properties, new ChargeInfo(ioVoltage, 1, 60 * 20), false);
    }

    private ElectricItem(Properties properties, ChargeInfo chargeInfo, boolean infinite) {
        super(properties.component(ModDataComponents.ENERGY.get(), 0L));
        this.chargeInfo = chargeInfo;
        this.infinite = infinite;
    }

    public static ElectricItem createInfinite(Properties properties) {
        return new ElectricItem(properties, ChargeInfo.INFINITY, true);
    }

    @Override
    public ChargeInfo getElectricProperty() {
        return this.chargeInfo;
    }

    public record ChargeInfo(@Getter VoltageTier voltageTier, long current,
                             int duration) implements ElectricProperty, VoltageTierLimited {
        public static final ChargeInfo INFINITY = new ChargeInfo(VoltageTier.MAX, Long.MAX_VALUE, 1);

        @Override
        public long voltage() {
            return voltageTier.value;
        }

        public long maxPower() {
            return ElectricProperty.power(this);
        }

        public long capacity() {
            return ElectricProperty.totalEnergy(this);
        }
    }
}
