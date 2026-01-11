package net.yiran.jei_ores.compat.jei.recipe;

import net.yiran.jei_ores.JeiOres;
import net.yiran.jei_ores.client.FeaturesReciever;
import net.yiran.jei_ores.compat.jei.JEIFeaturesData;
import net.yiran.jei_ores.compat.jei.widget.TextureTooltipWidget;
import net.yiran.jei_ores.mixin.accessor.TrapezoidHeightAccessor;
import net.yiran.jei_ores.mixin.accessor.UniformHeightAccessor;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlacedFeatureJeiRecipe extends AbstractRecipeCategory<JEIFeaturesData> {

    private static final ResourceLocation DISTRIBUTION = JeiOres.id("textures/gui/distribution.png");

    public AbstractPlacedFeatureJeiRecipe(RecipeType<JEIFeaturesData> recipeType, Component title, IDrawable icon, int width, int height) {
        super(recipeType, title, icon, width, height);
    }

    protected static Component anchorText(VerticalAnchor anchor) {
        String s;
        if (anchor instanceof VerticalAnchor.Absolute absolute) {
            s = String.valueOf(absolute.y());
        } else if (anchor instanceof VerticalAnchor.AboveBottom aboveBottom) {
            int offset = aboveBottom.offset();
            if (offset == 0) {
                s = "bot";
            } else if (offset > 0) {
                s = "bot+" + offset;
            } else {
                s = "bot" + offset;
            }
        } else if (anchor instanceof VerticalAnchor.BelowTop belowTop) {
            int offset = -belowTop.offset();
            if (offset == 0) {
                s = "top";
            } else if (offset > 0) {
                s = "top+" + offset;
            } else {
                s = "top" + offset;
            }
        } else {
            throw new RuntimeException();
        }
        return Component.literal(s);
    }

    protected static Component anchorTextLong(VerticalAnchor anchor) {
        return anchorTextLongInner(anchor).withStyle(ChatFormatting.WHITE);
    }

    private static MutableComponent anchorTextLongInner(VerticalAnchor anchor) {
        if (anchor instanceof VerticalAnchor.Absolute absolute) {
            return Component.literal(String.valueOf(absolute.y()));
        } else if (anchor instanceof VerticalAnchor.AboveBottom aboveBottom) {
            int offset = aboveBottom.offset();
            if (offset == 0) {
                return Component.translatable("jei_ores.distribution.anchor.bottom");
            } else if (offset > 0) {
                return Component.translatable("jei_ores.distribution.anchor.above_bottom", offset);
            } else {
                return Component.translatable("jei_ores.distribution.anchor.below_bottom", -offset);
            }
        } else if (anchor instanceof VerticalAnchor.BelowTop belowTop) {
            int offset = -belowTop.offset();
            if (offset == 0) {
                return Component.translatable("jei_ores.distribution.anchor.top");
            } else if (offset > 0) {
                return Component.translatable("jei_ores.distribution.anchor.above_top", offset);
            } else {
                return Component.translatable("jei_ores.distribution.anchor.below_top", -offset);
            }
        } else {
            throw new RuntimeException();
        }
    }

    protected static List<Biome> getBiomes(ResourceLocation id, PlacedFeature feature) {
        Registry<Biome> biomeRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME);
        return FeaturesReciever.getBiomes()
                .get(ResourceKey.create(Registries.PLACED_FEATURE, id))
                .stream()
                .map(biomeRegistry::get)
                .toList();
    }

    protected static void addDistributionGraph(IRecipeExtrasBuilder widgets, int x, int y, HeightProvider heightProvider) {
        if (heightProvider == null) return;

        HeightProviderType type;
        VerticalAnchor min, max, midLow, midHigh;

        if (heightProvider instanceof UniformHeight uniform) {
            type = HeightProviderType.UNIFORM;
            UniformHeightAccessor accessor = (UniformHeightAccessor) uniform;
            min = accessor.getMinInclusive();
            max = accessor.getMaxInclusive();
            midLow = midHigh = null;
        } else if (heightProvider instanceof TrapezoidHeight trapezoid) {
            TrapezoidHeightAccessor accessor = (TrapezoidHeightAccessor) trapezoid;
            min = accessor.getMinInclusive();
            max = accessor.getMaxInclusive();

            int plateau = accessor.getPlateau();

            // if the min and max are the same type, we can calculate the y-level with the highest frequency
            if (min instanceof VerticalAnchor.Absolute minAbs && max instanceof VerticalAnchor.Absolute maxAbs) {
                midLow = VerticalAnchor.absolute((minAbs.y() + maxAbs.y() - plateau) / 2);
                midHigh = VerticalAnchor.absolute((minAbs.y() + maxAbs.y() + plateau) / 2);
            } else if (min instanceof VerticalAnchor.AboveBottom minBot && max instanceof VerticalAnchor.AboveBottom maxBot) {
                midLow = VerticalAnchor.aboveBottom((minBot.offset() + maxBot.offset() - plateau) / 2);
                midHigh = VerticalAnchor.aboveBottom((minBot.offset() + maxBot.offset() + plateau) / 2);
            } else if (min instanceof VerticalAnchor.BelowTop minTop && max instanceof VerticalAnchor.BelowTop maxTop) {
                midLow = VerticalAnchor.belowTop((minTop.offset() + maxTop.offset() - plateau) / 2);
                midHigh = VerticalAnchor.belowTop((minTop.offset() + maxTop.offset() + plateau) / 2);
            } else {
                midLow = midHigh = null;
            }

            if (plateau == 0) {
                type = HeightProviderType.TRIANGULAR;

                if (midLow != null) {
                    widgets.addText(anchorText(midLow), 33, 10)
                            .setPosition(x, y)
                            .setTextAlignment(HorizontalAlignment.CENTER)
                            .setTextAlignment(VerticalAlignment.CENTER);
                }
            } else {
                type = HeightProviderType.TRAPEZOID;
            }
        } else {
            type = null;
            min = max = midLow = midHigh = null;
        }

        if (type != null && min != null && max != null) {
            widgets.addWidget(new TextureTooltipWidget(
                    DISTRIBUTION,
                    x/2, y-1,
                    32, 16,
                    0, type.v,
                    256, 256, // <- 改成你 DISTRIBUTION 贴图真实尺寸
                    getDistributionGraphTooltip(type, min, max, midLow, midHigh)
            ));
            /*
            IDrawable drawable = new TextureTooltipWidget(
                    DISTRIBUTION,
                    0, type.v,   // u, v
                    32, 16,      // w, h
                    256, 256     // 整张贴图尺寸
            );

            widgets.addDrawable(drawable, x, y);

            widgets.addTexture(DISTRIBUTION, x, y, 32, 16, 0, type.v)
                    .tooltipText(getDistributionGraphTooltip(type, min, max, midLow, midHigh));*/
            /*
            widgets.addText(anchorText(min), x, y+8)
                    .setTextAlignment(HorizontalAlignment.RIGHT)
                    .setTextAlignment(VerticalAlignment.BOTTOM);*/
            widgets.addText(anchorText(min),48, 10)
                    .setPosition(x-48, y)
                    .setTextAlignment(HorizontalAlignment.RIGHT)
                    .setTextAlignment(VerticalAlignment.CENTER);
            widgets.addText(anchorText(max), 48, 10)
                    .setPosition(x+32, y)
                    .setTextAlignment(HorizontalAlignment.LEFT)
                    .setTextAlignment(VerticalAlignment.CENTER);
            /*
            widgets.addText(anchorText(max), x+32, 10)
                    .setTextAlignment(HorizontalAlignment.LEFT)
                    .setTextAlignment(VerticalAlignment.BOTTOM);*/
        }
    }

    protected static List<Component> getDistributionGraphTooltip(HeightProviderType type, VerticalAnchor min, VerticalAnchor max, VerticalAnchor midLow, VerticalAnchor midHigh) {
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(type.name);
        tooltip.add(Component.translatable("jei_ores.distribution.range", anchorTextLong(min), anchorTextLong(max)).withStyle(ChatFormatting.GRAY));
        if (midLow != null && midHigh != null) {
            if (midLow.equals(midHigh)) {
                tooltip.add(Component.translatable("jei_ores.distribution.middle", anchorTextLong(midLow)).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("jei_ores.distribution.middle_range", anchorTextLong(midLow), anchorTextLong(midHigh)).withStyle(ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    protected static Component getVeinFreqComponent(int countMin, int countMax, int rarityChance) {
        Component veinFreq;
        if (countMin != -1 && countMax != -1) {
            if (countMin == countMax) {
                veinFreq = Component.translatable("jei_ores.veins_per_chunk", countMin);
            } else {
                veinFreq = Component.translatable("jei_ores.veins_per_chunk_range", countMin, countMax);
            }
        } else if (rarityChance != -1) {
            veinFreq = Component.translatable("jei_ores.rarity_chance", rarityChance);
        } else {
            veinFreq = null;
        }
        return veinFreq;
    }

    protected enum HeightProviderType {
        UNIFORM(0, Component.translatable("jei_ores.distribution.uniform").withStyle(ChatFormatting.BLUE)),
        TRIANGULAR(16, Component.translatable("jei_ores.distribution.triangle").withStyle(ChatFormatting.GREEN)),
        TRAPEZOID(32, Component.translatable("jei_ores.distribution.trapezoid").withStyle(ChatFormatting.RED));

        public final int v;
        public final Component name;

        HeightProviderType(int v, Component name) {
            this.v = v;
            this.name = name;
        }
    }
}
