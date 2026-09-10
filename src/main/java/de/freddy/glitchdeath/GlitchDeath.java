package de.freddy.glitchdeath;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class GlitchDeath implements ModInitializer {
    public static final String MOD_ID = "glitch-death";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(GlitchDeathConfig::load);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("glitchdeath")
                        // Every player can use these commands. Console/command blocks are rejected below.
                        .then(Commands.literal("mode")
                                .executes(context -> showMode(context.getSource()))
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("glitch");
                                            builder.suggest("name");
                                            builder.suggest("random");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> setMode(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "value")))))
                        .then(Commands.literal("status")
                                .executes(context -> showMode(context.getSource())))
                        .then(Commands.literal("glitch")
                                .executes(context -> setMode(context.getSource(), "glitch")))
                        .then(Commands.literal("name")
                                .executes(context -> setMode(context.getSource(), "name")))
                        .then(Commands.literal("random")
                                .executes(context -> setMode(context.getSource(), "random")))));
    }

    private static ServerPlayer getPlayer(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer player ? player : null;
    }

    private static int showMode(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            source.sendFailure(Component.literal("Dieser Command kann nur von einem Spieler verwendet werden."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "Dein GlitchDeath-Modus: " + GlitchDeathConfig.getMode(player).name().toLowerCase()), false);
        return 1;
    }

    private static int setMode(CommandSourceStack source, String value) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            source.sendFailure(Component.literal("Dieser Command kann nur von einem Spieler verwendet werden."));
            return 0;
        }

        GlitchDeathMode mode = GlitchDeathMode.fromString(value);
        if (mode == null) {
            source.sendFailure(Component.literal("Unbekannter Modus. Nutze: glitch, name oder random."));
            return 0;
        }

        GlitchDeathConfig.setMode(source.getServer(), player, mode);
        source.sendSuccess(() -> Component.literal(
                "Dein GlitchDeath-Modus wurde auf " + mode.name().toLowerCase() + " gesetzt."), false);
        return 1;
    }

    /** Replaces only the invisible killer's name; the rest of the vanilla death message is untouched. */
    public static Component getReplacement(ServerPlayer killer) {
        return switch (GlitchDeathConfig.getMode(killer)) {
            case NAME -> killer.getDisplayName();
            case GLITCH -> Component.literal("GLITCH")
                    .withStyle(Style.EMPTY.withObfuscated(true));
            case RANDOM -> randomName();
        };
    }

    private static Component randomName() {
        String[] syllables = {"Shadow", "Ghost", "Pixel", "Void", "Nova", "Frost", "Night", "Blaze", "Storm", "Phantom"};
        String first = syllables[ThreadLocalRandom.current().nextInt(syllables.length)];
        int number = ThreadLocalRandom.current().nextInt(10, 10000);
        return Component.literal(first + number);
    }
}
