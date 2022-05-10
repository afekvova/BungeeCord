package ru.leymooo.botfilter.packets;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import me.afek.bungee.DimensionType;
import me.afek.bungee.biome.BiomeRegistry;
import me.afek.bungee.dimension.DimensionElement;
import me.afek.bungee.dimension.DimensionRegistry;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JoinGame extends DefinedPacket {

    private final int entityId;
    private boolean hardcore = true;
    private short gameMode = 0;
    private short previousGameMode = 0;
    private Set<String> worldNames = new HashSet<>(Arrays.asList("minecraft:overworld"));
    private DimensionType dimensionType; // Auth
    private String worldName = "minecraft:overworld";
    private long seed = 1;
    private short difficulty = 0;
    private short maxPlayers = 1;
    private String levelType = "flat";
    private int viewDistance = 10;
    private boolean reducedDebugInfo = true;
    private boolean normalRespawn = true;
    private boolean debug = false;
    private boolean flat = true;
    private DimensionRegistry dimensionRegistry; // 1.16+
    private DimensionElement currentDimensionData; // 1.16.2+
    private CompoundBinaryTag biomeRegistry = BiomeRegistry.getRegistry(); // 1.16.2+

    public JoinGame(int entityId, DimensionType dimensionType) {
        this.entityId = entityId;
        this.dimensionType = dimensionType;
        this.currentDimensionData = this.createDimensionData(dimensionType, false);
        this.dimensionRegistry = new DimensionRegistry(ImmutableSet.of(this.currentDimensionData), ImmutableSet.of(this.dimensionType.getIdentifier()));
    }

    private DimensionElement createDimensionData(DimensionType dimension, boolean modern) {
        return new DimensionElement(
                dimension.getIdentifier(), 0, true,
                0.1F, false, true, true, false,
                true, false, false, false, 256,
                modern ? "#minecraft:infiniburn_nether" : "minecraft:infiniburn_nether",
                18000L, false, 8.0, dimension.getIdentifier(), 0, 256
        );
    }

    public JoinGame() {
        entityId = 0;
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        buf.writeInt(this.entityId);
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2)
            buf.writeBoolean(this.hardcore);

        buf.writeByte(this.gameMode);

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16) {
            buf.writeByte(this.previousGameMode);

            writeVarInt(this.worldNames.size(), buf);
            for (String world : this.worldNames)
                writeString(world, buf);

            CompoundBinaryTag.Builder registryContainer = CompoundBinaryTag.builder();
            ListBinaryTag encodedDimensionRegistry = this.dimensionRegistry.build(protocolVersion);

            if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2) {
                CompoundBinaryTag.Builder dimensionRegistryEntry = CompoundBinaryTag.builder();
                dimensionRegistryEntry.putString("type", "minecraft:dimension_type");
                dimensionRegistryEntry.put("value", encodedDimensionRegistry);
                registryContainer.put("minecraft:dimension_type", dimensionRegistryEntry.build());
                registryContainer.put("minecraft:worldgen/biome", this.biomeRegistry);
            } else {
                registryContainer.put("dimension", encodedDimensionRegistry);
            }

            writeCompoundTag(registryContainer.build(), buf);
        }

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16) {
            if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2) {
                this.writeCompoundTag(this.currentDimensionData.serialize(), buf);
            } else {
                writeString(this.dimensionType.getIdentifier(), buf);
            }

            writeString(this.dimensionType.getIdentifier(), buf);
        } else if (protocolVersion > ProtocolConstants.MINECRAFT_1_9) {
            buf.writeInt(this.dimensionType.getNumberType()); //dimension
        } else {
            buf.writeByte(this.dimensionType.getNumberType()); //dimension
        }

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_15)
            buf.writeLong(this.seed);

        if (protocolVersion < ProtocolConstants.MINECRAFT_1_14)
            buf.writeByte(this.difficulty);

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16_2) {
            writeVarInt(this.maxPlayers, buf);
        } else {
            buf.writeByte(this.maxPlayers);
        }

        if (protocolVersion < ProtocolConstants.MINECRAFT_1_16)
            writeString(this.levelType, buf);

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_14)
            writeVarInt(this.viewDistance, buf);

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_18)
            writeVarInt(this.viewDistance, buf);

        //if (protocolVersion >= 29) // Auth?
        buf.writeBoolean(this.reducedDebugInfo);

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_15)
            buf.writeBoolean(this.normalRespawn);

        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_16) {
            buf.writeBoolean(this.debug);
            buf.writeBoolean(this.flat);
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        throw new UnsupportedOperationException();
    }
}
