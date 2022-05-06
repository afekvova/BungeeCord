package me.afek.bungee.biome;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.nbt.CompoundBinaryTag;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BiomeElement {

    String precipitation;
    float depth;
    float temperature;
    float scale;
    float downfall;
    String category;
    BiomeEffects effects;

    public CompoundBinaryTag encode() {
        return CompoundBinaryTag.builder()
                .putString("precipitation", this.precipitation)
                .putFloat("depth", this.depth)
                .putFloat("temperature", this.temperature)
                .putFloat("scale", this.scale)
                .putFloat("downfall", this.downfall)
                .putString("category", this.category)
                .put("effects", this.effects.encode())
                .build();
    }
}