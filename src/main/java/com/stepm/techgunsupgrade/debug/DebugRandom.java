package com.stepm.techgunsupgrade.debug;

import java.util.Random;

/** Random source that only overrides chance rolls; positional nextDouble calls stay random. */
public final class DebugRandom extends Random {
    private static final long serialVersionUID = 1L;

    @Override
    public float nextFloat() {
        return DebugSettings.isForceRandomEffects() ? 0.0f : super.nextFloat();
    }
}
