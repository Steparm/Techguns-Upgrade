package com.stepm.techgunsupgrade.upgrade.effect;

/**
 * One executable meaning from an upgrade description.
 *
 * <p>The English description is never parsed to create this object. Values,
 * conditions and probabilities are declared explicitly in the definition
 * registry.</p>
 */
public final class EffectSpec {
    private final String id;
    private final EffectTrigger trigger;
    private final EffectAction action;
    private final EffectCondition condition;
    private final double value;
    private final double probability;
    private final double conditionValue;
    private final String sourceFragment;

    private EffectSpec(Builder builder) {
        this.id = builder.id;
        this.trigger = builder.trigger;
        this.action = builder.action;
        this.condition = builder.condition;
        this.value = builder.value;
        this.probability = builder.probability;
        this.conditionValue = builder.conditionValue;
        this.sourceFragment = builder.sourceFragment;
    }

    public EffectTrigger getTrigger() { return trigger; }
    public String getId() { return id; }
    public EffectAction getAction() { return action; }
    public EffectCondition getCondition() { return condition; }
    public double getValue() { return value; }
    public double getProbability() { return probability; }
    public double getConditionValue() { return conditionValue; }
    public String getSourceFragment() { return sourceFragment; }

    public static Builder builder(EffectTrigger trigger, EffectAction action, double value,
                                  String sourceFragment) {
        return new Builder(trigger, action, value, sourceFragment);
    }

    public static final class Builder {
        private final EffectTrigger trigger;
        private final EffectAction action;
        private final double value;
        private final String sourceFragment;
        private String id = "";
        private EffectCondition condition = EffectCondition.ALWAYS;
        private double probability = 1.0;
        private double conditionValue = 0.0;

        private Builder(EffectTrigger trigger, EffectAction action, double value,
                        String sourceFragment) {
            if (trigger == null || action == null) {
                throw new IllegalArgumentException("Effect trigger and action are required");
            }
            if (sourceFragment == null || sourceFragment.trim().isEmpty()) {
                throw new IllegalArgumentException("Effect source fragment is required");
            }
            this.trigger = trigger;
            this.action = action;
            this.value = value;
            this.sourceFragment = sourceFragment;
        }

        public Builder condition(EffectCondition condition, double conditionValue) {
            this.condition = condition == null ? EffectCondition.ALWAYS : condition;
            this.conditionValue = conditionValue;
            return this;
        }

        public Builder probability(double probability) {
            this.probability = probability;
            return this;
        }

        public Builder id(String id) {
            this.id = id == null ? "" : id;
            return this;
        }

        public EffectSpec build() {
            if (!Double.isFinite(value) || !Double.isFinite(probability)
                    || probability < 0.0 || probability > 1.0) {
                throw new IllegalArgumentException("Invalid effect numbers for " + sourceFragment);
            }
            return new EffectSpec(this);
        }
    }
}
