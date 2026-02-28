package com.zhigichan_31.vpp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("vpp-portals.json");

    public Map<String, PortalSettings> portals = new HashMap<>();

    public record PortalSettings(
            String server,
            int timer,
            TriggerSettings trigger,
            VisualSettings visuals,
            List<String> melody
    ) {}

    public record TriggerSettings(double scan_radius, String offset) {}
    public record VisualSettings(String particle, double orbit_rad, double orbit_speed) {}

    public static PortalConfig load() {
        try {
            if (!CONFIG_FILE.toFile().exists()) {
                PortalConfig defaultCfg = createDefault();
                save(defaultCfg);
                return defaultCfg;
            }
            try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
                PortalConfig config = GSON.fromJson(reader, PortalConfig.class);
                return config != null ? config : createDefault();
            }
        } catch (IOException e) {
            return createDefault();
        }
    }

    public static void save(PortalConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
            GSON.toJson(config, writer);
        } catch (IOException ignored) {}
    }

    private static PortalConfig createDefault() {
        PortalConfig config = new PortalConfig();
        config.portals.put("lobby_portal", new PortalSettings(
                "lobby", 100,
                new TriggerSettings(4.0, "~5, ~, ~5"),
                new VisualSettings("minecraft:portal", 1.2, 0.1),
                List.of(
                        "1/block.beehive.enter-1.0-1.0-player",
                        "100/entity.enderman.teleport-1.0-1.0-world"
                )
        ));
        return config;
    }
}
