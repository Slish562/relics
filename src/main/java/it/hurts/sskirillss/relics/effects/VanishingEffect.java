package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class VanishingEffect extends MobEffect {
    public VanishingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
            if (event.getEntity().hasEffect(EffectRegistry.VANISHING))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onHandRender(RenderHandEvent event) {
            if (Minecraft.getInstance().player.hasEffect(EffectRegistry.VANISHING) && event.getItemStack().isEmpty())
                event.setCanceled(true);
        }
    }
}