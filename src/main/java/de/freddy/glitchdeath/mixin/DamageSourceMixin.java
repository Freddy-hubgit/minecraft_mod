package de.freddy.glitchdeath.mixin;

import de.freddy.glitchdeath.GlitchDeath;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {

    @Inject(
            method = "getLocalizedDeathMessage",
            at = @At("RETURN"),
            cancellable = true
    )
    private void glitchDeathMessage(
            LivingEntity victim,
            CallbackInfoReturnable<Component> cir
    ) {

        DamageSource source =
                (DamageSource) (Object) this;

        Entity killer =
                source.getEntity();

        // Kein Spieler als Killer
        if (!(killer instanceof ServerPlayer serverKiller)) {
            return;
        }

        // Nur wenn der Killer unsichtbar ist
        if (!serverKiller.isInvisible()) {
            return;
        }

        // Das Opfer muss ebenfalls ein ServerPlayer sein
        if (!(victim instanceof ServerPlayer serverVictim)) {
            return;
        }

        Component original =
                cir.getReturnValue();

        if (original == null) {
            return;
        }

        if (!(original.getContents()
                instanceof TranslatableContents contents)) {
            return;
        }

        Object[] args =
                contents.getArgs().clone();

        String playerName =
                serverKiller.getName().getString();

        String displayName =
                serverKiller.getDisplayName().getString();

        /*
         * Wir suchen ausschließlich nach dem Namen
         * des Killers in den Argumenten der Vanilla-
         * Todesnachricht.
         */
        for (int i = 0; i < args.length; i++) {

            if (!(args[i] instanceof Component component)) {
                continue;
            }

            String argumentName =
                    component.getString();

            if (!argumentName.equals(playerName)
                    && !argumentName.equals(displayName)) {
                continue;
            }

            Component replacement =
                    GlitchDeath.getReplacement(serverKiller);

            args[i] = replacement;

            MutableComponent message =
                    Component.translatable(
                            contents.getKey(),
                            args
                    );

            cir.setReturnValue(message);
            return;
        }
    }
}
