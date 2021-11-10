package ru.afek.auth.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Title;
import net.md_5.bungee.protocol.packet.TitleTimes;
import ru.leymooo.botfilter.caching.PacketUtils;
import ru.leymooo.botfilter.config.Settings;

/**
 * @author Afek
 */

public class CachedActionBar {

    private ByteBuf[] times;
    private ByteBuf[] actionBar;

    public CachedActionBar(String raw, int in, int stay, int out) {
        if (!raw.isEmpty()) {
            String actionBar = raw.replace("%prefix%", Settings.IMP.MESSAGES.PREFIX);
            if (!actionBar.isEmpty()) {
                this.actionBar = new ByteBuf[PacketUtils.PROTOCOLS_COUNT];
                Title actionBarPacket = new Title();
                actionBarPacket.setAction(Title.Action.ACTIONBAR);
                actionBarPacket.setText(ComponentSerializer.toString(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', actionBar))));
                PacketUtils.fillArray(this.actionBar, actionBarPacket, Protocol.GAME);
            }

            if (this.actionBar != null) {
                this.times = new ByteBuf[PacketUtils.PROTOCOLS_COUNT];
                Title times = new Title();
                times.setFadeIn( in );
                times.setStay( stay );
                times.setFadeOut( out );
                times.setAction( Title.Action.TIMES );
                PacketUtils.fillArray( this.times, times, ProtocolConstants.MINECRAFT_1_8, ProtocolConstants.MINECRAFT_1_16_4, Protocol.GAME );
                TitleTimes times1 = new TitleTimes();
                times1.setFadeIn( in );
                times1.setStay( stay );
                times1.setFadeOut( out );
                PacketUtils.fillArray( this.times, times1, ProtocolConstants.MINECRAFT_1_17, ProtocolConstants.getLastSupportedProtocol(), Protocol.GAME );
            }
        }
    }

    public void writeActionBar(Channel channel, int version) {
        version = PacketUtils.rewriteVersion(version);
        if (times != null) {
            channel.write(times[version].retainedDuplicate());
        }
        if (actionBar != null) {
            channel.write(actionBar[version].retainedDuplicate());
        }
    }

    public void release() {
        if (actionBar != null) {
            for (ByteBuf buf : actionBar) {
                PacketUtils.releaseByteBuf(buf);
            }
            actionBar = null;
        }
        if (times != null) {
            for (ByteBuf buf : times) {
                PacketUtils.releaseByteBuf(buf);
            }
            times = null;
        }
    }
}
