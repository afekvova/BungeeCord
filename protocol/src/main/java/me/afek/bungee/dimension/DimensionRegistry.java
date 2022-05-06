package me.afek.bungee.dimension;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.afek.bungee.Builder;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;

import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DimensionRegistry implements Builder<ListBinaryTag> {

    Map<String, DimensionElement> registeredDimensions;
    ImmutableSet<String> levelNames;

    public DimensionRegistry(ImmutableSet<DimensionElement> registeredDimensions, ImmutableSet<String> levelNames) {
        this.registeredDimensions = Maps.uniqueIndex(registeredDimensions, DimensionElement::getName);
        this.levelNames = levelNames;
    }

    @Override
    public ListBinaryTag build(int version) {
        ListBinaryTag.Builder<CompoundBinaryTag> listBuilder = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (DimensionElement iter : registeredDimensions.values())
            listBuilder.add(iter.build(version));
        
        return listBuilder.build();
    }
}