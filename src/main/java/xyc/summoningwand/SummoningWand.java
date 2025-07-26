package xyc.summoningwand;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SummoningWand.MODID)
public class SummoningWand
{
    public static final String MODID = "summoningwand";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final DeferredRegister<Item> ITEMS
        = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> SUMMONING_WAND = ITEMS.register(
        SummoningWandItem.REGISTRY_NAME,
        SummoningWandItem::new
    );

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS
        = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> WAND_TP_SOUND = SOUND_EVENTS.register(
        SummoningWandItem.SOUND_TP_REGISTRY_NAME,
        () -> SoundEvent.createFixedRangeEvent(
            ResourceLocationUtils.fromNamespaceAndPath(MODID, SummoningWandItem.SOUND_TP_REGISTRY_NAME),
            16F
        )
    );
    public static IEventBus modBus;



    /** For Forge >= 47.4 */
    public SummoningWand(FMLJavaModLoadingContext context)
    {
        LOGGER.debug("\u001b[34m114514\u001b[0m");
        init(context, context.getModEventBus());
    }

    /** For Forge < 47.4 */
    @SuppressWarnings("removal")
    public SummoningWand()
    {
        LOGGER.debug("\u001b[34mCCB\u001b[0m");
        init(ModLoadingContext.get(), FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void init(ModLoadingContext context, IEventBus bus)
    {
        modBus = bus;
        ITEMS.register(bus);
        SOUND_EVENTS.register(bus);
        Config.register(context);
        WandUseHandManager.register();
    }
}
