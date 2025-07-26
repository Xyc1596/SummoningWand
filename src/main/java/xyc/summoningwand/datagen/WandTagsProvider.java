package xyc.summoningwand.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyc.summoningwand.SummoningWand;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

public class WandTagsProvider extends ItemTagsProvider
{
    public WandTagsProvider(
        PackOutput output,
        CompletableFuture<HolderLookup.Provider> provider,
        ExistingFileHelper helper
    )
    {
        super(
            output,
            provider,
            CompletableFuture.completedFuture(null),
            SummoningWand.MODID,
            helper
        );
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addTags(HolderLookup.Provider provider)
    {
        this.tag(TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("yes_steve_model", "hoes")
        )).add(SummoningWand.SUMMONING_WAND.get());
    }
}
