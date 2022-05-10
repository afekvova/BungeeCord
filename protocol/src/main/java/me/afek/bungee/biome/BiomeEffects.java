package me.afek.bungee.biome;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.nbt.CompoundBinaryTag;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BiomeEffects {

    int skyColor, waterFogColor, fogColor, waterColor;

    Integer foliageColor;
    String grassColorModifier;
    Music music;
    String ambientSound;
    AdditionsSound additionsSound;
    MoodSound moodSound;
    Particle particle;

    public CompoundBinaryTag encode() {
        CompoundBinaryTag.Builder result = CompoundBinaryTag.builder();
        result.putInt("sky_color", this.skyColor)
                .putInt("water_fog_color", this.waterColor)
                .putInt("fog_color", this.fogColor)
                .putInt("water_color", this.waterColor);

        if (this.foliageColor != null)
            result.putInt("foliage_color", this.foliageColor);

        if (this.grassColorModifier != null)
            result.putString("grass_color_modifier", this.grassColorModifier);

        if (this.music != null)
            result.put("music", this.music.encode());

        if (this.ambientSound != null)
            result.putString("ambient_sound", this.ambientSound);

        if (this.additionsSound != null)
            result.put("additions_sound", this.additionsSound.encode());

        if (this.moodSound != null)
            result.put("mood_sound", this.moodSound.encode());

        if (this.particle != null)
            result.put("particle", this.particle.encode());

        return result.build();
    }

    public static EffectsBuilder builder(int skyColor, int waterFogColor, int fogColor, int waterColor) {
        return new EffectsBuilder()
                .skyColor(skyColor)
                .waterFogColor(waterFogColor)
                .fogColor(fogColor)
                .waterColor(waterColor);
    }

    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class MoodSound {

        int tickDelay;
        double offset;
        int blockSearchExtent;
        String sound;

        public static MoodSound of(int tickDelay, double offset, int blockSearchExtent, String sound) {
            return new MoodSound(tickDelay, offset, blockSearchExtent, sound);
        }

        public CompoundBinaryTag encode() {
            return CompoundBinaryTag.builder()
                    .putInt("tick_delay", this.tickDelay)
                    .putDouble("offset", this.offset)
                    .putInt("block_search_extent", this.blockSearchExtent)
                    .putString("sound", this.sound)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Music {

        boolean replaceCurrentMusic;

        String sound;
        int maxDelay;
        int minDelay;

        public static Music of(boolean replaceCurrentMusic, String sound, int maxDelay, int minDelay) {
            return new Music(replaceCurrentMusic, sound, maxDelay, minDelay);
        }

        public CompoundBinaryTag encode() {
            return CompoundBinaryTag.builder()
                    .putBoolean("replace_current_music", this.replaceCurrentMusic)
                    .putString("sound", this.sound)
                    .putInt("max_delay", this.maxDelay)
                    .putInt("min_delay", this.minDelay)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class AdditionsSound {

        String sound;
        double tickChance;

        public static AdditionsSound of(String sound, double tickChance) {
            return new AdditionsSound(sound, tickChance);
        }

        public CompoundBinaryTag encode() {
            return CompoundBinaryTag.builder()
                    .putString("sound", this.sound)
                    .putDouble("tick_chance", this.tickChance)
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Particle {

        float probability;
        ParticleOptions options;

        public static Particle of(float probability, ParticleOptions options) {
            return new Particle(probability, options);
        }

        public CompoundBinaryTag encode() {
            return CompoundBinaryTag.builder()
                    .putFloat("probability", this.probability)
                    .put("options", this.options.encode())
                    .build();
        }

        @Getter
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        public static class ParticleOptions {

            String type;

            public CompoundBinaryTag encode() {
                return CompoundBinaryTag.builder()
                        .putString("type", this.type)
                        .build();
            }
        }
    }

    public static class EffectsBuilder {

        private int skyColor;
        private int waterFogColor;
        private int fogColor;
        private int waterColor;
        private Integer foliageColor;
        private String grassColorModifier;
        private Music music;
        private String ambientSound;
        private AdditionsSound additionsSound;
        private MoodSound moodSound;
        private Particle particle;

        public EffectsBuilder skyColor(int skyColor) {
            this.skyColor = skyColor;
            return this;
        }

        public EffectsBuilder waterFogColor(int waterFogColor) {
            this.waterFogColor = waterFogColor;
            return this;
        }

        public EffectsBuilder fogColor(int fogColor) {
            this.fogColor = fogColor;
            return this;
        }

        public EffectsBuilder waterColor(int waterColor) {
            this.waterColor = waterColor;
            return this;
        }

        public EffectsBuilder foliageColor(Integer foliageColor) {
            this.foliageColor = foliageColor;
            return this;
        }

        public EffectsBuilder grassColorModifier(String grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public EffectsBuilder music(Music music) {
            this.music = music;
            return this;
        }

        public EffectsBuilder ambientSound(String ambientSound) {
            this.ambientSound = ambientSound;
            return this;
        }

        public EffectsBuilder additionsSound(AdditionsSound additionsSound) {
            this.additionsSound = additionsSound;
            return this;
        }

        public EffectsBuilder moodSound(MoodSound moodSound) {
            this.moodSound = moodSound;
            return this;
        }

        public EffectsBuilder particle(Particle particle) {
            this.particle = particle;
            return this;
        }

        public BiomeEffects build() {
            return new BiomeEffects(
                    this.skyColor,
                    this.waterFogColor,
                    this.fogColor,
                    this.waterColor,
                    this.foliageColor,
                    this.grassColorModifier,
                    this.music,
                    this.ambientSound,
                    this.additionsSound,
                    this.moodSound,
                    this.particle
            );
        }
    }
}