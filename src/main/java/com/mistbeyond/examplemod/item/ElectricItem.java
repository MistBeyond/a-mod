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
public class ElectricItem extends Item implements ElectricProperty.Provider<ElectricItem.ChargeInfo> {
    @Getter
    protected final ChargeInfo chargeInfo;

    public ElectricItem(Properties properties, VoltageTier ioVoltage) {
        var chargeInfo = new ChargeInfo(ioVoltage, 1, 60 * 20);
        this(properties, chargeInfo);
    }

    private ElectricItem(Properties properties, ChargeInfo chargeInfo) {
        super(properties.component(ModDataComponents.ENERGY.get(), 0L));
        this.chargeInfo = chargeInfo;
    }

    public static ElectricItem createInfinite(Properties properties) {
        return new ElectricItem(properties, ChargeInfo.INFINITY);
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
            if (isInfinite()) return Long.MAX_VALUE;
            return ElectricProperty.power(this);
        }

        public long capacity() {
            if (isInfinite()) return Long.MAX_VALUE;
            return ElectricProperty.totalEnergy(this);
        }

        public boolean isInfinite() {
            return this == ChargeInfo.INFINITY;
        }
    }
}
