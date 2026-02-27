package com.zhigichan_31.vpp;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod("vpp")
public class VelocityProxyPortal {
    public static PortalConfig CONFIG;
    private static final Map<UUID, Integer> teleportTimer = new HashMap<>();
    private static final Map<UUID, Long> lastTeleportTime = new HashMap<>();

    public VelocityProxyPortal(IEventBus modEventBus) {
        CONFIG = PortalConfig.load();
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("bungeecord").versioned("1").optional();
        registrar.playToClient(BungeePayload.TYPE, BungeePayload.CODEC, (p, c) -> {});
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("vpp")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("reload").executes(c -> {
                    CONFIG = PortalConfig.load();
                    c.getSource().sendSuccess(() -> Component.literal("§a[VPP] Конфиг успешно перезагружен!"), true);
                    return 1;
                })));
    }

    @EventBusSubscriber(modid = "vpp")
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) return;

            // Скан портала раз в 5 тиков для экономии TPS
            if (player.tickCount % 5 != 0 && !teleportTimer.containsKey(player.getUUID())) return;

            Entity portal = player.level().getEntities((Entity) null, player.getBoundingBox().inflate(8.0),
                            e -> e.getTags().stream().anyMatch(tag -> CONFIG.portals.containsKey(tag)))
                    .stream().findFirst().orElse(null);

            if (portal != null) {
                String portalId = portal.getTags().stream().filter(tag -> CONFIG.portals.containsKey(tag)).findFirst().orElse(null);
                var settings = CONFIG.portals.get(portalId);

                if (settings != null && portal.distanceTo(player) <= settings.trigger().scan_radius()) {
                    UUID uuid = player.getUUID();
                    if (System.currentTimeMillis() - lastTeleportTime.getOrDefault(uuid, 0L) < 10000) return;

                    int current = teleportTimer.getOrDefault(uuid, 0) + 1;
                    teleportTimer.put(uuid, current);

                    handleVisuals(player, portal, settings, current);

                    // Сообщение раз в 0.5 сек (10 тиков) - огромная экономия трафика
                    if (current % 10 == 0 || current == 1) {
                        int secondsLeft = Math.max(0, (settings.timer() - current) / 20 + 1);
                        player.displayClientMessage(Component.literal("§fТелепортация на §b" + settings.server() + " §fчерез: §e" + secondsLeft + " сек."), true);
                    }

                    if (current >= settings.timer()) executeTeleport(player, settings);
                    return;
                }
            }

            if (teleportTimer.containsKey(player.getUUID())) {
                teleportTimer.remove(player.getUUID());
                player.displayClientMessage(Component.literal("§cТелепортация отменена"), true);
            }
        }
    }

    private static void handleVisuals(ServerPlayer p, Entity ent, PortalConfig.PortalSettings s, int ticks) {
        // Оптимизация сети: частицы только в четные тики
        if (ticks % 2 == 0) {
            double r = s.visuals().orbit_rad();
            double speed = s.visuals().orbit_speed();
            double angle = ticks * speed;
            spawnPart(p, s.visuals().particle(), ent.getX() + Math.cos(angle) * r, ent.getY() + 0.8, ent.getZ() + Math.sin(angle) * r);
            spawnPart(p, s.visuals().particle(), ent.getX() + Math.cos(angle + Math.PI) * r, ent.getY() + 0.8, ent.getZ() + Math.sin(angle + Math.PI) * r);
        }

        for (String note : s.melody()) {
            try {
                String[] parts = note.split("/", 2);
                if (Integer.parseInt(parts[0].trim()) == ticks) {
                    String[] data = parts[1].split("-");
                    playSound(p, data[0], Float.parseFloat(data[1]), Float.parseFloat(data[2]), data[3].trim());
                }
            } catch (Exception ignored) {}
        }
    }

    private static void playSound(ServerPlayer p, String id, float pitch, float vol, String type) {
        ResourceLocation res = ResourceLocation.parse(id.contains(":") ? id : "minecraft:" + id);
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(res);
        if (sound == null) return;
        Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);

        if ("player".equals(type)) {
            p.connection.send(new ClientboundSoundPacket(holder, SoundSource.PLAYERS, p.getX(), p.getY(), p.getZ(), vol, pitch, p.getRandom().nextLong()));
        } else {
            p.level().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, vol, pitch);
        }
    }

    private static void executeTeleport(ServerPlayer p, PortalConfig.PortalSettings s) {
        lastTeleportTime.put(p.getUUID(), System.currentTimeMillis());
        teleportTimer.remove(p.getUUID());
        if ("look".equals(s.trigger().offset())) {
            Vec3 look = p.getLookAngle().normalize();
            p.teleportTo(p.getX() - look.x * 2.0, p.getY(), p.getZ() - look.z * 2.0);
        }
        PacketDistributor.sendToPlayer(p, new BungeePayload("Connect", s.server()));
    }

    private static void spawnPart(ServerPlayer p, String name, double x, double y, double z) {
        // Если в конфиге пусто, "none" или null — мгновенно выходим, экономя ресурсы
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("none")) return;

        try {
            ResourceLocation res = ResourceLocation.parse(name);
            BuiltInRegistries.PARTICLE_TYPE.getOptional(res).ifPresent(type -> {
                if (type instanceof ParticleOptions options) {
                    p.serverLevel().sendParticles(options, x, y, z, 1, 0, 0, 0, 0);
                }
            });
        } catch (Exception ignored) {
            // Если в конфиге написана абракадабра — просто игнорируем, чтобы сервер не лагал
        }
    }
}
