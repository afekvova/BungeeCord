package me.afek.bungee;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DimensionType {

    NETHER(-1, "minecraft:the_nether"),
    OVERWORLD(0, "minecraft:overworld"),
    END(1, "minecraft:the_end");

    public static final DimensionType[] values = values();

    int numberType; // 1.8 - 1.15.2
    String identifier; // minecraft:overworld, minecraft:the_nether, minecraft:the_end

    public static DimensionType valueOf(int numberType) {
        for (DimensionType dimensionType : values)
            if (dimensionType.getNumberType() == numberType) return dimensionType;

        return DimensionType.OVERWORLD;
    }
}
