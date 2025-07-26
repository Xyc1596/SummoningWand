package xyc.summoningwand.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import xyc.summoningwand.SummoningWand;
import xyc.summoningwand.SummoningWandItem;

public class WandSoundProvider extends SoundDefinitionsProvider
{
    public WandSoundProvider(PackOutput output, ExistingFileHelper helper)
    {
        super(output, SummoningWand.MODID, helper);
    }

    @Override
    public void registerSounds()
    {
        this.add(SummoningWand.WAND_TP_SOUND.get(), definition()
            .subtitle(SummoningWandItem.SOUND_TP)
            .with(sound(SoundEvents.ENDERMAN_TELEPORT.getLocation(), SoundDefinition.SoundType.EVENT).volume(0.5))
        );
    }
}
