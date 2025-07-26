package xyc.summoningwand.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyc.summoningwand.ResourceLocationUtils;
import xyc.summoningwand.SummoningWand;
import xyc.summoningwand.SummoningWandItem;

public class WandModelProvider extends ItemModelProvider
{
    public WandModelProvider(PackOutput output, ExistingFileHelper helper)
    {
        super(output, SummoningWand.MODID, helper);
    }

    @Override
    public void registerModels()
    {
        ResourceLocation alt = ResourceLocationUtils.fromNamespaceAndPath(
            SummoningWand.MODID,
            SummoningWandItem.REGISTRY_NAME + "_activated"
        );
        ResourceLocation alt_with_item = ResourceLocationUtils.fromNamespaceAndPath(
            SummoningWand.MODID,
            "item/" + SummoningWandItem.REGISTRY_NAME + "_activated"
        );
        basicItem(alt)
            .parent(new ModelFile.UncheckedModelFile("item/handheld"))
            .texture("layer0", alt_with_item);
        basicItem(SummoningWand.SUMMONING_WAND.get())
            .parent(new ModelFile.UncheckedModelFile("item/handheld"))
            .texture(
                "layer0",
                ResourceLocationUtils.fromNamespaceAndPath(
                    SummoningWand.MODID,
                    "item/" + SummoningWandItem.REGISTRY_NAME
                )
            )
            .override()
            .predicate(ResourceLocationUtils.fromNamespaceAndPath(SummoningWand.MODID, "bind_state"), 1)
            .model(new ModelFile.UncheckedModelFile(alt_with_item))
            .end();
    }
}
