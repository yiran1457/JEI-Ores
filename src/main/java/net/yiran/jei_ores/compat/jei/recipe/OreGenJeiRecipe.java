package net.yiran.jei_ores.compat.jei.recipe;

import net.yiran.jei_ores.Config;
import net.yiran.jei_ores.JeiOres;
import net.yiran.jei_ores.compat.jei.JEIFeaturesData;
import net.yiran.jei_ores.compat.jei.stack.IBiomeIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.yiran.jei_ores.mixin.accessor.*;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("removal")
public class OreGenJeiRecipe extends AbstractPlacedFeatureJeiRecipe {
    public static RecipeType<JEIFeaturesData> recipeType = RecipeType.create(JeiOres.MODID, "ore", JEIFeaturesData.class);

    public OreGenJeiRecipe(IGuiHelper guiHelper) {
        super(
                recipeType,
                Component.translatable("jei.category.jei_ores.oregen"),
                guiHelper.createDrawableItemLike(Blocks.DEEPSLATE_DIAMOND_ORE),
                160,
                Config.oreGenPageHeight.get()
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JEIFeaturesData featuresData, IFocusGroup iFocusGroup) {
        var id = featuresData.id();
        var feature = featuresData.feature();
        OreConfiguration oreConfig = (OreConfiguration) feature.feature().value().config();
        float discardChanceOnAirExposure = oreConfig.discardChanceOnAirExposure;
        int index = 0;
        for (OreConfiguration.TargetBlockState targetBlockState : oreConfig.targetStates) {
            var target = targetBlockState.target;
            Stream<Block> blocks = Stream.empty();
            Float probability = null;
            if (target instanceof TagMatchTest tagMatchTest) {
                var tag = ((TagMatchTestAccessor) tagMatchTest).getTag();
                blocks = BuiltInRegistries.BLOCK.getOrCreateTag(tag).stream()
                        .map(Holder::value);
            } else if (target instanceof BlockMatchTest blockMatchTest) {
                var block = ((BlockMatchTestAccessor) blockMatchTest).getBlock();
                blocks = Stream.of(block);
            } else if (target instanceof BlockStateMatchTest blockStateMatchTest) {
                var state = ((BlockStateMatchTestAccessor) blockStateMatchTest).getBlockState();
                blocks = Stream.of(state.getBlock());
            } else if (target instanceof RandomBlockMatchTest randomBlockMatchTest) {
                RandomBlockMatchTestAccessor accessor = (RandomBlockMatchTestAccessor) randomBlockMatchTest;
                var block = accessor.getBlock();
                probability = accessor.getProbability();
                blocks = Stream.of(block);
            } else if (target instanceof RandomBlockStateMatchTest randomBlockStateMatchTest) {
                RandomBlockStateMatchTestAccessor accessor = (RandomBlockStateMatchTestAccessor) randomBlockStateMatchTest;
                var block = accessor.getBlockState().getBlock();
                blocks = Stream.of(block);
                probability = accessor.getProbability();
            }
            Float finalProbability = probability;
            builder.addSlot(RecipeIngredientRole.INPUT, 0, 18 + index * 18)
                    .setStandardSlotBackground()
                    .addIngredients(VanillaTypes.ITEM_STACK,
                            blocks.map(ItemLike::asItem)
                                    .map(Item::getDefaultInstance)
                                    .toList()
                    )
                    .addTooltipCallback((iRecipeSlotView, list) -> {
                        if (finalProbability != null) {
                            list.add(Component.translatable("jei_ores.chance.tooltip",finalProbability));
                        }
                    });

            Textures textures = Internal.getTextures();
            IDrawable drawable = textures.getRecipeArrow();
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 0, 18 + index * 18)
                    .setBackground(drawable, 20, 0);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 46, 18 + index++ * 18)
                    .setStandardSlotBackground()
                    .addIngredient(
                            VanillaTypes.ITEM_STACK,
                            targetBlockState.state.getBlock().asItem().getDefaultInstance()
                    );
        }

        List<Biome> biomes = List.of();
        for (PlacementModifier modifier : feature.placement()) {
            if (modifier instanceof BiomeFilter) {
                biomes = getBiomes(id, feature);
            }
        }

        if (!biomes.isEmpty())
            builder.addSlot(RecipeIngredientRole.CATALYST, 96, 18)
                    .setStandardSlotBackground()
                    .addIngredients(
                            IBiomeIngredient.INSTANCE,
                            biomes
                    );


        if (discardChanceOnAirExposure > 0)
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 142, 18)
                    .setStandardSlotBackground()
                    .addIngredient(
                            VanillaTypes.ITEM_STACK,
                            Items.BARRIER.getDefaultInstance()
                    )
                    .addTooltipCallback((iRecipeSlotView, list) -> {
                        list.add(Component.translatable("jei_ores.discard_on_air_chance",discardChanceOnAirExposure));
                    });

    }

    public void createRecipeExtras(IRecipeExtrasBuilder builder, JEIFeaturesData featuresData, IFocusGroup focuses) {
        var feature = featuresData.feature();
        HeightProvider heightProvider = null;
        int countMin = -1;
        int countMax = -1;
        int rarityChance = -1;

        for (PlacementModifier modifier : feature.placement()) {
            if (modifier instanceof HeightRangePlacement heightRange) {
                heightProvider = ((HeightRangePlacementAccessor) heightRange).getHeight();
            } else if (modifier instanceof CountPlacement countPlacement) {
                IntProvider countIntProvider = ((CountPlacementAccessor) countPlacement).getCount();
                countMin = countIntProvider.getMinValue();
                countMax = countIntProvider.getMaxValue();
            } else if (modifier instanceof RarityFilter rarityFilter) {
                rarityChance = ((RarityFilterAccessor) rarityFilter).getChance();
            }
        }

        if (feature.feature().value().feature() instanceof ScatteredOreFeature) {
            countMin = countMax = 1; // special handling, only used by ancient debris
        }

        addDistributionGraph(builder, 64, 0, heightProvider);

        Component veinFreq = getVeinFreqComponent(countMin, countMax, rarityChance);
        if (veinFreq != null) {
            builder.addText(veinFreq, 160, 45)
                    .setTextAlignment(HorizontalAlignment.RIGHT)
                    .setTextAlignment(VerticalAlignment.BOTTOM);
        }
    }

}
