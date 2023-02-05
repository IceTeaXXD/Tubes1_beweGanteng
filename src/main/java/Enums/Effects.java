package Enums;

public enum Effects {
    AFTERBURNER(1),
    ASTEROID_FIELD(2),
    GAS_CLOUD(4),
    SUPER_FOOD(8),
    SHIELD(16);

    public final Integer value;

    Effects(Integer value) {
        this.value = value;
    }

    public static Effects valueOf(Integer value) {
        for (Effects effect : Effects.values()) {
            if (effect.value == value) return effect;
        }

        throw new IllegalArgumentException("Value not found");
    }
}