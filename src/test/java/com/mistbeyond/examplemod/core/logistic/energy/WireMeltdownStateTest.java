package com.mistbeyond.examplemod.core.logistic.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WireMeltdownStateTest {
    private static final float MAX_CURRENT = 16;
    private static final int REQUIRED_TICKS = 20;

    @Test
    void schedulesMeltdownOnNextTickAfterTwentyOverCurrentTicks() {
        WireMeltdownState state = new WireMeltdownState(MAX_CURRENT, REQUIRED_TICKS);

        for (int i = 0; i < REQUIRED_TICKS; i++) {
            state.recordLoad(MAX_CURRENT + 1);
            assertFalse(state.tick());
        }

        assertTrue(state.isMeltdownPending());
        assertEquals(REQUIRED_TICKS, state.overCurrentTicks());
        assertTrue(state.tick());
        assertFalse(state.isMeltdownPending());
    }

    @Test
    void resetsStreakWhenCurrentDropsBelowMax() {
        WireMeltdownState state = new WireMeltdownState(MAX_CURRENT, REQUIRED_TICKS);
        for (int i = 0; i < 10; i++) {
            state.recordLoad(MAX_CURRENT + 1);
            state.tick();
        }

        state.recordLoad(MAX_CURRENT - 1);
        state.tick();

        assertFalse(state.isMeltdownPending());
        assertEquals(0, state.overCurrentTicks());
    }

    @Test
    void countsMultipleOverCurrentLoadsOncePerTick() {
        WireMeltdownState state = new WireMeltdownState(MAX_CURRENT, REQUIRED_TICKS);

        state.recordLoad(MAX_CURRENT + 1);
        state.recordLoad(MAX_CURRENT + 2);
        state.tick();

        assertEquals(1, state.overCurrentTicks());
    }

    @Test
    void currentEqualToMaxIsNotOverCurrent() {
        WireMeltdownState state = new WireMeltdownState(MAX_CURRENT, REQUIRED_TICKS);

        state.recordLoad(MAX_CURRENT);
        state.tick();

        assertEquals(0, state.overCurrentTicks());
    }
}
