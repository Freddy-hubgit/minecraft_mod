package de.freddy.glitchdeath;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Stores the death-name mode separately for every player UUID. */
public final class GlitchDeathConfig {
    private static final String FILE_NAME = "glitchdeath.properties";
    private static final GlitchDeathMode DEFAULT_MODE = GlitchDeathMode.GLITCH;
    private static final Map<UUID, GlitchDeathMode> PLAYER_MODES = new ConcurrentHashMap<>();

    private GlitchDeathConfig() {}

    public static void load(MinecraftServer server) {
        PLAYER_MODES.clear();
        Path file = getFile(server);
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                save(server);
                return;
            }

            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            }

            for (String key : properties.stringPropertyNames()) {
                if (!key.startsWith("player.")) {
                    continue;
                }
                try {
                    UUID uuid = UUID.fromString(key.substring("player.".length()));
                    GlitchDeathMode mode = GlitchDeathMode.fromString(properties.getProperty(key));
                    if (mode != null) {
                        PLAYER_MODES.put(uuid, mode);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed UUIDs or modes in the config.
                }
            }
        } catch (IOException ignored) {
            // Keep the in-memory defaults if the config cannot be read.
        }
    }

    public static void save(MinecraftServer server) {
        Path file = getFile(server);
        try {
            Files.createDirectories(file.getParent());
            Properties properties = new Properties();
            for (Map.Entry<UUID, GlitchDeathMode> entry : PLAYER_MODES.entrySet()) {
                properties.setProperty("player." + entry.getKey(), entry.getValue().name().toLowerCase());
            }
            try (OutputStream out = Files.newOutputStream(file)) {
                properties.store(out, "GlitchDeath per-player configuration");
            }
        } catch (IOException ignored) {
            // The command still changes the mode for the current server session.
        }
    }

    private static Path getFile(MinecraftServer server) {
        return server.getServerDirectory().resolve("config").resolve(FILE_NAME);
    }

    public static GlitchDeathMode getMode(ServerPlayer player) {
        return PLAYER_MODES.getOrDefault(player.getUUID(), DEFAULT_MODE);
    }

    public static void setMode(MinecraftServer server, ServerPlayer player, GlitchDeathMode mode) {
        PLAYER_MODES.put(player.getUUID(), mode);
        save(server);
    }
}
