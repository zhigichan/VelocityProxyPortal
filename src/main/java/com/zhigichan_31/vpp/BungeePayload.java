package com.zhigichan_31.vpp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;

public record BungeePayload(String subCommand, String serverName) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bungeecord", "main");
    public static final CustomPacketPayload.Type<BungeePayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, BungeePayload> CODEC = CustomPacketPayload.codec(
            (payload, buf) -> {
                byte[] subBytes = payload.subCommand.getBytes(StandardCharsets.UTF_8);
                buf.writeShort(subBytes.length);
                buf.writeBytes(subBytes);
                byte[] serverBytes = payload.serverName.getBytes(StandardCharsets.UTF_8);
                buf.writeShort(serverBytes.length);
                buf.writeBytes(serverBytes);
            },
            buf -> {
                int len1 = buf.readUnsignedShort();
                String sub = buf.readCharSequence(len1, StandardCharsets.UTF_8).toString();
                int len2 = buf.readUnsignedShort();
                String server = buf.readCharSequence(len2, StandardCharsets.UTF_8).toString();
                return new BungeePayload(sub, server);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
