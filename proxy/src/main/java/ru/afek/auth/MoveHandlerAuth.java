package ru.afek.auth;

import ru.leymooo.botfilter.packets.TeleportConfirm;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.PlayerPosition;
import ru.leymooo.botfilter.packets.Player;
import net.md_5.bungee.netty.PacketHandler;

public class MoveHandlerAuth extends PacketHandler {

    public double x, y, z, lastY;
    public boolean onGround;
    public int teleportId, waitingTeleportId, ticks;

    public MoveHandlerAuth() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.onGround = false;
        this.teleportId = -1;
        this.waitingTeleportId = 9876;
        this.lastY = 0.0;
        this.ticks = 0;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handle(final Player player) throws Exception {
        this.onGround = player.isOnGround();
    }

    @Override
    public void handle(final PlayerPosition pos) throws Exception {
        this.x = pos.getX();
        this.lastY = this.y;
        this.y = pos.getY();
        this.z = pos.getZ();
        this.onGround = pos.isOnGround();
        this.onMove();
    }

    @Override
    public void handle(final PlayerPositionAndLook posRot) throws Exception {
        if (((AuthConnector) this).getVersion() == 47 && posRot.getX() == 7.0 && posRot.getY() == 450.0 && posRot.getZ() == 7.0 && this.waitingTeleportId == 9876) {
            this.ticks = 0;
            this.y = -1.0;
            this.lastY = -1.0;
            this.waitingTeleportId = -1;
        }
        this.x = posRot.getX();
        this.lastY = this.y;
        this.y = posRot.getY();
        this.z = posRot.getZ();
        this.onGround = posRot.isOnGround();
        this.onMove();
    }

    @Override
    public void handle(final TeleportConfirm confirm) throws Exception {
        if (confirm.getTeleportId() == this.waitingTeleportId) {
            this.ticks = 0;
            this.y = -1.0;
            this.lastY = -1.0;
            this.waitingTeleportId = -1;
        }
    }

    public void onMove() {
        throw new UnsupportedOperationException("Method is not overrided");
    }

    public static double getSpeed(final int ticks) {
        return formatDouble(-((Math.pow(0.98, ticks) - 1.0) * 3.92));
    }

    public static double formatDouble(final double d) {
        return Math.floor(d * 100.0) / 100.0;
    }
}
