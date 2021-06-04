package it.hurts.sskirillss.relics.items.relics;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.IHasTooltip;
import it.hurts.sskirillss.relics.items.RelicItem;
import it.hurts.sskirillss.relics.items.relics.renderer.SporeSackModel;
import it.hurts.sskirillss.relics.utils.*;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.loot.LootTables;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Collections;
import java.util.List;

public class SporeSackItem extends RelicItem implements ICurioItem, IHasTooltip {
    public SporeSackItem() {
        super(Rarity.UNCOMMON);
    }

    @Override
    public java.util.List<ITextComponent> getShiftTooltip() {
        java.util.List<ITextComponent> tooltip = Lists.newArrayList();
        tooltip.add(new TranslationTextComponent("tooltip.relics.spore_sack.shift_1"));
        return tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.addAll(TooltipUtils.applyTooltip(stack));
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class SporeSackEvents {
        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            if (!(event.getEntity() instanceof ProjectileEntity)) return;
            ProjectileEntity projectile = (ProjectileEntity) event.getEntity();
            if (projectile.getOwner() == null || !(projectile.getOwner() instanceof PlayerEntity)) return;
            PlayerEntity player = (PlayerEntity) projectile.getOwner();
            World world = projectile.getCommandSenderWorld();
            if (world.isClientSide()) return;
            if (!CuriosApi.getCuriosHelper().findEquippedCurio(ItemRegistry.SPORE_SACK.get(), player).isPresent()
                    || player.getCooldowns().isOnCooldown(ItemRegistry.SPORE_SACK.get())
                    || world.getRandom().nextFloat() > RelicsConfig.SporeSack.SPORE_CHANCE.get()) return;
            ((ServerWorld) world).sendParticles(new RedstoneParticleData(0, 255, 0, 1),
                    projectile.getX(), projectile.getY(), projectile.getZ(), 100, 1, 1, 1, 0.5);
            world.playSound(null, projectile.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                    SoundCategory.PLAYERS, 1.0F, 0.5F);
            player.getCooldowns().addCooldown(ItemRegistry.SPORE_SACK.get(), RelicsConfig.SporeSack.SPORE_COOLDOWN.get() * 20);
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox()
                    .inflate(RelicsConfig.SporeSack.SPORE_RADIUS.get()))) {
                if (entity == player) continue;
                entity.addEffect(new EffectInstance(Effects.POISON, RelicsConfig.SporeSack.POISON_DURATION.get() * 20,
                        RelicsConfig.SporeSack.POISON_AMPLIFIER.get()));
                entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, RelicsConfig.SporeSack.SLOWNESS_DURATION.get() * 20,
                        RelicsConfig.SporeSack.SLOWNESS_AMPLIFIER.get()));
            }
        }
    }

    @Override
    public List<ResourceLocation> getLootChests() {
        return Collections.singletonList(LootTables.JUNGLE_TEMPLE);
    }

    private final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/items/models/spore_sack.png");

    @Override
    public void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemStack stack) {
        SporeSackModel model = new SporeSackModel();
        matrixStack.pushPose();
        ICurio.RenderHelper.translateIfSneaking(matrixStack, livingEntity);
        ICurio.RenderHelper.rotateIfSneaking(matrixStack, livingEntity);
        model.renderToBuffer(matrixStack, renderTypeBuffer.getBuffer(RenderType.entityCutout(TEXTURE)),
                light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.popPose();
    }

    @Override
    public boolean canRender(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        return true;
    }
}