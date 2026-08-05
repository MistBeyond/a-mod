package com.mistbeyond.examplemod.core.logistic.energy;

/**
 * Tracks a wire's per-tick current load and schedules a meltdown after a configurable
 * number of consecutive over-current ticks.
 */
public final class WireMeltdownState {
    private final float maxCurrent;
    private final int overCurrentTicksRequired;
    private int overCurrentTicks;
    private boolean meltdownPending;
    private float currentTickMaxCurrent;

    public WireMeltdownState(float maxCurrent, int overCurrentTicksRequired) {
        if (!Float.isFinite(maxCurrent) || maxCurrent < 0) {
            throw new IllegalArgumentException("maxCurrent must be a finite non-negative value: " + maxCurrent);
        }
        if (overCurrentTicksRequired <= 0) {
            throw new IllegalArgumentException("overCurrentTicksRequired must be positive: " + overCurrentTicksRequired);
        }
        this.maxCurrent = maxCurrent;
        this.overCurrentTicksRequired = overCurrentTicksRequired;
    }

    /**
     * Records the highest current seen on the current game tick.
     */
    public void recordLoad(float current) {
        currentTickMaxCurrent = Math.max(currentTickMaxCurrent, current);
    }

    /**
     * Advances one server tick.
     *
     * @return {@code true} when the meltdown scheduled by a previous overload streak should
     * be performed now
     */
    public boolean tick() {
        if (meltdownPending) {
            meltdownPending = false;
            currentTickMaxCurrent = 0;
            return true;
        }
        if (currentTickMaxCurrent > maxCurrent) {
            overCurrentTicks++;
            if (overCurrentTicks >= overCurrentTicksRequired) {
                meltdownPending = true;
            }
        } else {
            overCurrentTicks = 0;
        }
        currentTickMaxCurrent = 0;
        return false;
    }

    public int overCurrentTicks() {
        return overCurrentTicks;
    }

    public boolean isMeltdownPending() {
        return meltdownPending;
    }

    /**
     * Restores persisted state after the block entity is loaded.
     */
    public void restore(int overCurrentTicks, boolean meltdownPending) {
        this.overCurrentTicks = Math.max(overCurrentTicks, 0);
        this.meltdownPending = meltdownPending;
        this.currentTickMaxCurrent = 0;
    }
}
