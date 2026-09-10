package de.freddy.glitchdeath.mixin;

import de.freddy.glitchdeath.GlitchDeath;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {

    @Inject(method = "getLocalizedDeathMessage", at = @At("RETURN"), cancellable = true)
    private void glitchDeathMessage(LivingEntity victim, CallbackInfoReturnable<Component> cir) {
        DamageSource source = (DamageSource) (Object) this;
        Entity killer = source.getEntity();

        if (!(killer instanceof Player player) || !player.isInvisible()) {
            return;
        }

        Component original = cir.getReturnValue();
        if (original == null || !(original.getContents() instanceof TranslatableContents contents)) {
            return;
        }

        Object[] args = contents.getArgs().clone();
        String playerName = player.getName().getString();
        String displayName = player.getDisplayName().getString();
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        Component replacement = GlitchDeath.getReplacement(serverPlayer);

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Component component
                    && (component.getString().equals(displayName) || component.getString().equals(playerName))) {
                args[i] = replacement;
                MutableComponent message = Component.translatable(contents.getKey(), args);
                cir.setReturnValue(message);
                return;
            }
        }
    }
}
