package xyc.summoningwand.datagen.lang;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import xyc.summoningwand.SummoningWand;
import xyc.summoningwand.SummoningWandCommands;
import xyc.summoningwand.SummoningWandItem;

public class ZH_CN extends LanguageProvider
{
    public ZH_CN(PackOutput output)
    {
        super(output, SummoningWand.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations()
    {
        this.add(SummoningWand.SUMMONING_WAND.get(), "召唤魔杖");
        this.add(SummoningWandItem.MESSAGE_BIND_SUCCESS, "成功绑定实体：%s");
        this.add(SummoningWandItem.MESSAGE_BIND_ENTITY_BANNED, "绑定失败！目标实体类型被禁用：%s");
        this.add(SummoningWandItem.MESSAGE_TP_SUCCESS, "成功将 %s 召唤至 %s");
        this.add(SummoningWandItem.MESSAGE_TP_BLOCK_BANNED, "召唤失败！目标位置被禁用：%s");
        this.add(SummoningWandItem.MESSAGE_TP_BLOCK_BLOCKED, "召唤失败！目标位置被阻挡：%s");
        this.add(SummoningWandItem.MESSAGE_TP_NOT_OWNER, "召唤失败！该魔杖属于：%s");
        this.add(SummoningWandItem.MESSAGE_TP_ENTITY_NOT_FOUND, "召唤失败！绑定的实体不存在：%s");
        this.add(SummoningWandItem.MESSAGE_TP_ENTITY_NOT_BOUND, "未绑定实体！");
        this.add(SummoningWandItem.TOOLTIP_HOLD_FOR_USAGE, "按住 [%s] 可查看使用方法");
        this.add(SummoningWandItem.TOOLTIP_OWNER, "所有者：%s");
        this.add(SummoningWandItem.TOOLTIP_TARGET, "目标：%s");
        this.add(SummoningWandItem.TOOLTIP_CLICK_ENTITY1, "对实体使用时（需要%s持有魔杖）");
        this.add(SummoningWandItem.TOOLTIP_CLICK_ENTITY2, " 将实体绑定到魔杖");
        this.add(SummoningWandItem.TOOLTIP_CLICK_BLOCK1, "对方块使用时（需要%s持有魔杖）");
        this.add(SummoningWandItem.TOOLTIP_CLICK_BLOCK2, " 将绑定的实体召唤到指定位置");
        this.add(SummoningWandItem.TOOLTIP_CLICK_BLOCK3, " 按住 [%s] 可阻止乘客被一同传送");
        this.add(SummoningWandItem.TOOLTIP_CRAFT1, "放置在合成网格中");
        this.add(SummoningWandItem.TOOLTIP_CRAFT2, " 解除绑定（会失去附魔）");
        this.add(SummoningWandItem.CONST_MAIN_HAND, "主手");
        this.add(SummoningWandItem.CONST_OFFHAND, "副手");
        this.add(SummoningWandItem.CONST_BOTH_HAND, "主手或副手");
        this.add(SummoningWandItem.CONST_LBRACKET, "（");
        this.add(SummoningWandItem.CONST_RBRACKET, "）");
        this.add(SummoningWandItem.SOUND_TP, "召唤魔杖: 传送");
        this.add(SummoningWandCommands.MESSAGE_CONFIG_ALL_CLIENTS, "全体客户端配置");
        this.add(SummoningWandCommands.MESSAGE_CONFIG_CLIENT, "您的客户端配置");
        this.add(SummoningWandCommands.MESSAGE_CONFIG_COMMON, "通用配置列表");
        this.add(SummoningWandCommands.MESSAGE_REFRESH_ALL_CLIENTS, "全体客户端配置已同步！");
        this.add(SummoningWandCommands.MESSAGE_REFRESH_CLIENT, "您的客户端配置已同步！");
        this.add(SummoningWandCommands.MESSAGE_REFRESH_COMMON, "通用配置已更新！");
    }
}