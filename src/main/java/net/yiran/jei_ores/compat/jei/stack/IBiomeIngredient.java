package net.yiran.jei_ores.compat.jei.stack;

import net.yiran.jei_ores.JeiOres;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"NullableProblems"})
public class IBiomeIngredient implements IIngredientType<Biome>, IIngredientHelper<Biome>, IIngredientRenderer<Biome> {
    public static IBiomeIngredient INSTANCE = new IBiomeIngredient();

    private static final ResourceLocation missingSpriteId = JeiOres.id("jei_ores/biome_icon/missing");

    @Override
    public IIngredientType<Biome> getIngredientType() {
        return null;
    }

    @Override
    public String getDisplayName(Biome biome) {
        return I18n.get(getResourceLocation(biome).toLanguageKey("biome"));
    }

    @Override
    public String getUniqueId(Biome biome, UidContext uidContext) {
        return getResourceLocation(biome).toString();
    }

    @Override
    public ResourceLocation getResourceLocation(Biome biome) {
        return Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
    }

    @Override
    public Biome copyIngredient(Biome biome) {
        return biome;
    }

    @Override
    public String getErrorInfo(Biome biome) {
        return "";
    }

    @Override
    public void render(GuiGraphics guiGraphics, Biome biome) {

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 150);

        var atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        ResourceLocation id = getResourceLocation(biome);
        TextureAtlasSprite sprite;
        if (id == null) {
            sprite = atlas.getSprite(missingSpriteId);
        } else {
            sprite = atlas.getSprite(id.withPrefix("biome_icon/"));

            if (MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name())) {
                sprite = atlas.getSprite(missingSpriteId);
            }
        }
        guiGraphics.blit(0, 0, 0, 16, 16, sprite);

        pose.popPose();
    }

    @SuppressWarnings("removal")
    @Override
    public List<Component> getTooltip(Biome biome, TooltipFlag tooltipFlag) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal(getDisplayName(biome)));
        if (Minecraft.getInstance().options.advancedItemTooltips) {
            list.add(Component.literal(getResourceLocation(biome).toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        list.add(Component.literal(getResourceLocation(biome).getNamespace()));
        return list;
    }

    @Override
    public Class<? extends Biome> getIngredientClass() {
        return Biome.class;
    }

}
