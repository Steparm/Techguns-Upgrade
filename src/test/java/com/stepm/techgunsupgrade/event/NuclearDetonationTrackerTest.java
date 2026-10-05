package com.stepm.techgunsupgrade.event;

import com.stepm.techgunsupgrade.config.TguConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NuclearDetonationTrackerTest {
    @Test
    void usesConfiguredShortDelayAndClampsUnsafeValues() {
        int original = TguConfig.nuclearDetonationDelayTicks;
        try {
            TguConfig.nuclearDetonationDelayTicks = 12;
            assertEquals(12, NuclearDetonationTracker.configuredDelayTicks());
            TguConfig.nuclearDetonationDelayTicks = -100;
            assertEquals(6, NuclearDetonationTracker.configuredDelayTicks());
            TguConfig.nuclearDetonationDelayTicks = 100;
            assertEquals(30, NuclearDetonationTracker.configuredDelayTicks());
        } finally {
            TguConfig.nuclearDetonationDelayTicks = original;
        }
    }
}
