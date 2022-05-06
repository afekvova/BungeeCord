package me.afek.bungee.biome;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public enum BiomeRegistry {

    PLAINS("minecraft:plains", 1,
            new BiomeElement("rain", 0.125F, 0.8F, 0.05F, 0.4F, "plains",
                    BiomeEffects.builder(7907327, 329011, 12638463, 415920)
                            .moodSound(BiomeEffects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
                            .build()
            )
    ),
    SWAMP("minecraft:swamp", 6,
            new BiomeElement("rain", -0.2F, 0.8F, 0.1F, 0.9F, "swamp",
                    BiomeEffects.builder(7907327, 329011, 12638463, 415920)
                            .grassColorModifier("swamp")
                            .foliageColor(6975545)
                            .moodSound(BiomeEffects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
                            .build()
            )
    ),
    SWAMP_HILLS("minecraft:swamp_hills", 134,
            new BiomeElement("rain", -0.1F, 0.8F, 0.3F, 0.9F, "swamp",
                    BiomeEffects.builder(7907327, 329011, 12638463, 415920)
                            .grassColorModifier("swamp")
                            .foliageColor(6975545)
                            .moodSound(BiomeEffects.MoodSound.of(6000, 2.0, 8, "minecraft:ambient.cave"))
                            .build()
            )
    );

    String name;
    int id;
    BiomeElement biomeElement;

    public CompoundBinaryTag encodeBiome() {
        return CompoundBinaryTag.builder()
                .putString("name", this.name)
                .putInt("id", this.id)
                .put("element", this.biomeElement.encode())
                .build();
    }

    public static CompoundBinaryTag getRegistry() {
        return CompoundBinaryTag.builder()
                .putString("type", "minecraft:worldgen/biome")
                .put("value", ListBinaryTag.from(Arrays.stream(BiomeRegistry.values()).map(BiomeRegistry::encodeBiome).collect(Collectors.toList())))
                .build();
    }
}