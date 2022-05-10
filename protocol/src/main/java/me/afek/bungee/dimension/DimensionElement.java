package me.afek.bungee.dimension;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.afek.bungee.Builder;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.md_5.bungee.protocol.ProtocolConstants;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DimensionElement implements Builder<CompoundBinaryTag> {

    String name;
    int dimension;
    boolean natural;
    float ambientLight;
    boolean shrunk;
    boolean ultrawarm;
    boolean hasCeiling;
    boolean hasSkylight;
    boolean piglinSafe;
    boolean bedsWork;
    boolean respawnAnchorsWork;
    boolean hasRaids;
    int logicalHeight;
    String infiniburn;
    long fixedTime;
    boolean createDragonFight;
    double coordinateScale;
    String effects;
    int minY; // 1.17+
    int height; // 1.17+

    @Override
    public CompoundBinaryTag build(int version) {
        CompoundBinaryTag details = serialize();
        if (version >= ProtocolConstants.MINECRAFT_1_16_2)
            return CompoundBinaryTag.builder().putString("name", name).putInt("id", dimension).put("element", details).build();

        return details.putString("name", name);
    }

    public CompoundBinaryTag serialize() {
        CompoundBinaryTag.Builder elementBuilder = CompoundBinaryTag.builder();
        elementBuilder.putBoolean("natural", natural);
        elementBuilder.putFloat("ambient_light", ambientLight);
        elementBuilder.putBoolean("shrunk", shrunk);
        elementBuilder.putBoolean("ultrawarm", ultrawarm);
        elementBuilder.putBoolean("has_ceiling", hasCeiling);
        elementBuilder.putBoolean("has_skylight", hasSkylight);
        elementBuilder.putBoolean("piglin_safe", piglinSafe);
        elementBuilder.putBoolean("bed_works", bedsWork);
        elementBuilder.putBoolean("respawn_anchor_works", respawnAnchorsWork);
        elementBuilder.putBoolean("has_raids", hasRaids);
        elementBuilder.putInt("logical_height", logicalHeight);
        elementBuilder.putString("infiniburn", infiniburn);
        elementBuilder.putLong("fixed_time", fixedTime);
        elementBuilder.putBoolean("has_enderdragon_fight", createDragonFight);
        elementBuilder.putDouble("coordinate_scale", coordinateScale);
        elementBuilder.putString("effects", effects);
        elementBuilder.putInt("min_y", minY);
        elementBuilder.putInt("height", height);
        return elementBuilder.build();
    }
}
