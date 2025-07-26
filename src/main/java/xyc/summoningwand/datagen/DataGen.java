package xyc.summoningwand.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyc.summoningwand.SummoningWand;
import xyc.summoningwand.datagen.lang.EN_US;
import xyc.summoningwand.datagen.lang.ZH_CN;

@Mod.EventBusSubscriber(modid = SummoningWand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        generator.addProvider(event.includeClient(), new ZH_CN(output));
        generator.addProvider(event.includeClient(), new EN_US(output));
        generator.addProvider(event.includeClient(), new WandRecipeProvider(output));
        generator.addProvider(event.includeClient(), new WandModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new WandSoundProvider(output, helper));
        generator.addProvider(
            event.includeServer(),
            new WandTagsProvider(output, event.getLookupProvider(), helper)
        );
    }
}
