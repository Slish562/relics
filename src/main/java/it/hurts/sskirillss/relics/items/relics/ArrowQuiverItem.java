package it.hurts.sskirillss.relics.items.relics;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.IHasTooltip;
import it.hurts.sskirillss.relics.items.RelicItem;
import it.hurts.sskirillss.relics.items.relics.renderer.ArrowQuiverModel;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.RelicsConfig;
import it.hurts.sskirillss.relics.utils.TooltipUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.Collections;
import java.util.List;

public class ArrowQuiverItem extends RelicItem implements ICurioItem, IHasTooltip {
    public ArrowQuiverItem() {
        super(Rarity.COMMON);
    }

    @Override
    public List<ITextComponent> getShiftTooltip() {
        List<ITextComponent> tooltip = Lists.newArrayList();
        tooltip.add(new TranslationTextComponent("tooltip.relics.arrow_quiver.shift_1"));
        tooltip.add(new TranslationTextComponent("tooltip.relics.arrow_quiver.shift_2"));
        return tooltip;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.addAll(TooltipUtils.applyTooltip(stack));
    }

    @Override
    public void curioTick(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        if (!(livingEntity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) livingEntity;
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        if (!player.isShiftKeyDown()) return;
        if (player.tickCount % RelicsConfig.ArrowQuiver.ARROW_PICKUP_COOLDOWN.get() != 0) return;
        int radius = RelicsConfig.ArrowQuiver.ARROW_PICKUP_RADIUS.get();
        for (AbstractArrowEntity arrow : player.getCommandSenderWorld().getEntitiesOfClass(AbstractArrowEntity.class,
                player.getBoundingBox().inflate(radius, radius, radius))) {
            if (arrow.pickup != AbstractArrowEntity.PickupStatus.ALLOWED) continue;
            arrow.playerTouch(player);
            break;
        }
    }

    @Override
    public List<ResourceLocation> getLootChests() {
        return Collections.singletonList(LootTables.VILLAGE_FLETCHER);
    }

    private final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/items/models/arrow_quiver.png");

    @Override
    public void render(String identifier, int index, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemStack stack) {
        ArrowQuiverModel model = new ArrowQuiverModel();
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

    @Mod.EventBusSubscriber(modid = Reference.MODID)
    public static class ArrowQuiverEvents {
        @SubscribeEvent
        public static void onArrowShoot(ArrowLooseEvent event) {
            if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            if (player.getCooldowns().isOnCooldown(ItemRegistry.ARROW_QUIVER.get())) return;
            if (!CuriosApi.getCuriosHelper().findEquippedCurio(ItemRegistry.ARROW_QUIVER.get(), player).isPresent()) return;
            if (random.nextFloat() > RelicsConfig.ArrowQuiver.MULTISHOT_CHANCE.get()) return;
            for (int i = 0; i < RelicsConfig.ArrowQuiver.ADDITIONAL_ARROW_AMOUNT.get(); i++) {
                ItemStack bow = player.getMainHandItem();
                ItemStack ammo = player.getProjectile(bow);
                AbstractArrowEntity projectile = ((ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW))
                        .createArrow(player.getCommandSenderWorld(), ammo, player);
                projectile.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                projectile.shootFromRotation(player, player.xRot, player.yRot, 0.0F, BowItem.getPowerForTime(event.getCharge()) * 3.0F, 5.0F);
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow) > 0)
                    projectile.setBaseDamage(projectile.getBaseDamage() + (double) EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow) * 0.5D + 0.5D);
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow) > 0)
                    projectile.setKnockback(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow));
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0)
                    projectile.setSecondsOnFire(100);
                player.getCommandSenderWorld().addFreshEntity(projectile);
            }
            player.getCooldowns().addCooldown(ItemRegistry.ARROW_QUIVER.get(), RelicsConfig.ArrowQuiver.MULTISHOT_COOLDOWN.get() * 20);
        }
    }
}