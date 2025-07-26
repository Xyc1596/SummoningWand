package xyc.summoningwand.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import xyc.summoningwand.SummoningWand;
import xyc.summoningwand.SummoningWandItem;

import java.util.function.Consumer;

class WandRecipeProvider extends RecipeProvider
{
    public WandRecipeProvider(PackOutput output) {
        super(output);
    }

    public void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer)
    {
        Item s = Items.STICK, a = Items.AMETHYST_SHARD, p = Items.ENDER_PEARL;
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SummoningWand.SUMMONING_WAND.get())
            .define('S', Ingredient.of(s))
            .define('A', Ingredient.of(a))
            .define('P', Ingredient.of(p))
            .pattern(" AP")
            .pattern(" SA")
            .pattern("S  ")
            .unlockedBy(
                "has_amethyst_shard_or_ender_pearl",
                inventoryTrigger(ItemPredicate.Builder.item().of(a, p).build())
            )
            .save(consumer);

        Item w = SummoningWand.SUMMONING_WAND.get();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, w)
            .requires(w)
            .unlockedBy(getHasName(w), has(w))
            .save(
                consumer,
                ResourceLocation.fromNamespaceAndPath(
                    SummoningWand.MODID,
                    SummoningWandItem.REGISTRY_NAME + "_clear"
                )
            );
    }
}
