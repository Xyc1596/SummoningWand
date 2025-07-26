package xyc.summoningwand.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import xyc.summoningwand.SummoningWand;
import xyc.summoningwand.SummoningWandCommands;
import xyc.summoningwand.SummoningWandItem;

public class EN_US extends LanguageProvider
{
    public EN_US(PackOutput output)
    {
        super(output, SummoningWand.MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        this.add(SummoningWand.SUMMONING_WAND.get(), "Summoning Wand");
        this.add(SummoningWandItem.MESSAGE_BIND_SUCCESS, "Successfully bind entity：%s");
        this.add(SummoningWandItem.MESSAGE_BIND_ENTITY_BANNED, "Binding failed! The entity type is banned：%s");
        this.add(SummoningWandItem.MESSAGE_TP_SUCCESS, "Successfully summon %s to %s");
        this.add(SummoningWandItem.MESSAGE_TP_BLOCK_BANNED, "Summoning failed! The target position is banned：%s");
        this.add(SummoningWandItem.MESSAGE_TP_BLOCK_BLOCKED, "Summoning failed! The target position is blocked：%s");
        this.add(SummoningWandItem.MESSAGE_TP_NOT_OWNER, "Summoning failed! The wand belongs to：%s");
        this.add(SummoningWandItem.MESSAGE_TP_ENTITY_NOT_FOUND, "Summoning failed! Cannot find the bound entity：%s");
        this.add(SummoningWandItem.MESSAGE_TP_ENTITY_NOT_BOUND, "No entity is bound!");
        this.add(SummoningWandItem.TOOLTIP_HOLD_FOR_USAGE, "Hold [%s] for usage");
        this.add(SummoningWandItem.TOOLTIP_OWNER, "Owner：%s");
        this.add(SummoningWandItem.TOOLTIP_TARGET, "Target：%s");
        this.add(SummoningWandItem.TOOLTIP_CLICK_ENTITY1, "Using on an entity (Requiring holding the wand with %s)");
        this.add(SummoningWandItem.TOOLTIP_CLICK_ENTITY2, " Bind the entity to the wand");
        this.add(SummoningWandItem.TOOLTIP_CLICK_BLOCK1, "Using on a block (Requiring holding the wand with the %s）");
        this.add(SummoningWandItem.TOOLTIP_CLICK_BLOCK2, " Summon the bound entity to the target position");
        this.add(SummoningWandItem.TOOLTIP_CLICK_BLOCK3, " Hold [%s] to prevent teleporting passengers");
        this.add(SummoningWandItem.TOOLTIP_CRAFT1, "Placing in a crafting grid");
        this.add(SummoningWandItem.TOOLTIP_CRAFT2, " Unbind (will lose enchantments)");
        this.add(SummoningWandItem.CONST_MAIN_HAND, "main hand");
        this.add(SummoningWandItem.CONST_OFFHAND, "offhand");
        this.add(SummoningWandItem.CONST_BOTH_HAND, "main hand / offhand");
        this.add(SummoningWandItem.CONST_LBRACKET, " (");
        this.add(SummoningWandItem.CONST_RBRACKET, ") ");
        this.add(SummoningWandItem.SOUND_TP, "Summoning Wand: Teleport");
        this.add(SummoningWandCommands.MESSAGE_CONFIG_ALL_CLIENTS, "ALL CLIENT CONFIGS");
        this.add(SummoningWandCommands.MESSAGE_CONFIG_CLIENT, "YOUR CLIENT CONFIGS");
        this.add(SummoningWandCommands.MESSAGE_CONFIG_COMMON, "COMMON CONFIGS");
        this.add(SummoningWandCommands.MESSAGE_REFRESH_ALL_CLIENTS, "All client configs have been synchronized!");
        this.add(SummoningWandCommands.MESSAGE_REFRESH_CLIENT, "Your client configs have been synchronized!");
        this.add(SummoningWandCommands.MESSAGE_REFRESH_COMMON, "Common configs have been refreshed!");
    }
}