package xyc.summoningwand;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import xyc.summoningwand.enums.WandUseHand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = SummoningWand.MODID)
public class Config
{
    /** 召唤魔杖成功传送实体后的冷却时间（Tick） */
    public static int wandUseCooldown;

    /**
     * 使用魔杖传送物品是否消耗耐久（魔杖最大耐久为132）
     * <blockquote>由于物品加载在配置加载之前，无法通过配置文件设置最大耐久值</blockquote>
     */
    public static boolean wandHasDurability;

    /** 是否只有魔杖所有者可以使用该魔杖传送目标实体 */
    public static boolean wandOwnerOnly;

    /** 指定的手持有召唤魔杖时才能绑定或传送实体 */
    public static WandUseHand wandBindHand, wandTPHand;

    /**
     * 对于不完整方块，用一个最小的长方体包围其碰撞箱；
     * 如果该长方体最短的棱长度小于或等于此值，则该方块允许作为传送目标位置。
     */
    public static double blockMaxWidth;

    /**
     * 不允许作为传送目标位置的方块
     * <blockquote>默认情况下，所有无碰撞箱方块（如空气、液体、传送门方块）都允许作为传送目标位置。</blockquote>
     */
    public static HashSet<Block> blockBlacklist = new HashSet<>();

    /**
     * 强制允许作为传送目标位置的方块，优先级高于{@link Config#entityBlacklist}且无视碰撞箱
     */
    public static HashSet<Block> blockWhitelist = new HashSet<>();

    /**
     * 不允许作为传送对象的实体
     * <blockquote>
     * 默认情况下，除玩家外所有原版可右键交互实体（如生物、矿车、船、下落的方块）都允许作为传送对象。
     * </blockquote>
     */
    public static HashSet<EntityType<?>> entityBlacklist = new HashSet<>();

    /**
     * 强制允许作为传送对象的实体，优先级高于{@link Config#entityBlacklist}
     */
    public static HashSet<EntityType<?>> entityWhitelist = new HashSet<>();



    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();



    private static final ForgeConfigSpec.IntValue WAND_USE_COOLDOWN = COMMON_BUILDER.push("WAND")
        .comment("""
                
                Cooldown TICKS after successfully teleporting an entity with a summoning wand.
                Default: 60""")
        .defineInRange("wandUseCooldown", 60, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.BooleanValue WAND_HAS_DURABILITY = COMMON_BUILDER
        .comment("""
                
                Whether the wand has durability (only consumes when teleporting; the max durability is 132).
                Default: true""")
        .define("wandHasDurability", true);

    private static final ForgeConfigSpec.BooleanValue WAND_OWNER_ONLY = COMMON_BUILDER
        .comment("""
                
                Whether only the owner can use the wand to teleport the bound entity.
                Default: true""")
        .define("wandOwnerOnly", true);



    private static final ForgeConfigSpec.EnumValue<WandUseHand> WAND_BIND_HAND = CLIENT_BUILDER
        .comment("""
                
                In which hand(s) you should hold the wand to bind an entity.
                This option is intended to provide a reliable method to prevent accidental operations.
                Default: "OFF\"""")
        .defineEnum("wandBindHand", WandUseHand.OFF);

    private static final ForgeConfigSpec.EnumValue<WandUseHand> WAND_TP_HAND = CLIENT_BUILDER
        .comment("""
                
                In which hand(s) you should hold the wand to teleport the bound entity.
                This option is intended to provide a reliable method to prevent accidental operations.
                Default: "MAIN\"""")
        .defineEnum("wandTPHand", WandUseHand.MAIN);



    private static final ForgeConfigSpec.DoubleValue BLOCK_MAX_WIDTH = COMMON_BUILDER.push("BLOCK")
        .comment("""
                
                Encase the collision box of the right-clicked block with the smallest cuboid;
                If the shortest edge length of the cuboid is less than or equals to this value,
                this block is considered as a valid target position.
                For preciser information please check the source code.
                Default: 0.2""")
        .defineInRange("blockMaxWidth", 0.2, 0, 1);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_BLACKLIST = COMMON_BUILDER
        .comment("""
                
                Blocks that are NOT considered as valid target positions.
                By default, any place occupied by a block with no collision box
                (e.g. air, liquids and portals) is a valid target position.
                Use "#" prefix to represent a tag; the "minecraft:" namespace prefix can be omitted.
                Default: []
                Example: ["nether_portal", "minecraft:water", "#flowers"]""")
        .defineListAllowEmpty("blockBlacklist", List::of, Objects::nonNull);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_WHITELIST = COMMON_BUILDER
        .comment("""
                
                Blocks that are compulsorily considered as valid target positions.
                This list has the highest priority, which means the wand will try to teleport an entity to a block
                listed below regardless of its existence in blockBlacklist and its collision box.
                Use "#" prefix to represent a tag; the "minecraft:" namespace prefix can be omitted.
                Default: []
                Example: ["grass_block", "minecraft:dirt", "#stairs"]""")
        .defineListAllowEmpty("blockWhitelist", List::of, Objects::nonNull);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST = COMMON_BUILDER
        .comment("""
                
                Entity types that are NOT considered as valid targets.
                By default, any entity you can right-click to interact with
                (e.g. mobs, minecarts, boats and falling blocks) except players is a valid target.
                Use "#" prefix to represent a tag; the "minecraft:" namespace prefix can be omitted.
                Default: []
                Example: ["villager", "minecraft:wolf, "#undead"]""")
        .defineListAllowEmpty("entityBlacklist", List::of, Objects::nonNull);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_WHITELIST = COMMON_BUILDER
        .comment("""
                
                Entity types that are compulsorily considered as valid targets.
                This list has higher priority than entityBlacklist.
                Use "#" prefix to represent a tag; the "minecraft:" namespace prefix can be omitted.
                Default: []
                Example: ["player", "minecraft:falling_block"]""")
        .defineListAllowEmpty("entityWhitelist", List::of, Objects::nonNull);



    public static final ForgeConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();



    /** {@link ModLoadingContext#registerConfig} */
    public static void register(ModLoadingContext context)
    {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static void loadClientConfigs()
    {
        wandBindHand = WAND_BIND_HAND.get();
        wandTPHand = WAND_TP_HAND.get();
    }

    public static void loadCommonConfigs()
    {
        wandUseCooldown = WAND_USE_COOLDOWN.get();
        wandHasDurability = WAND_HAS_DURABILITY.get();
        wandOwnerOnly = WAND_OWNER_ONLY.get();
        blockMaxWidth = BLOCK_MAX_WIDTH.get();
        blockWhitelist.clear();
        blockWhitelist.addAll(BLOCK_WHITELIST.get().stream()
                                             .map(Config::getBlocksFromId)
                                             .flatMap(Set::stream)
                                             .filter(Objects::nonNull)
                                             .collect(Collectors.toSet()));
        blockBlacklist.clear();
        blockBlacklist.addAll(BLOCK_BLACKLIST.get().stream()
                                             .map(Config::getBlocksFromId)
                                             .flatMap(Set::stream)
                                             .filter(Objects::nonNull)
                                             .filter(id -> !blockWhitelist.contains(id))
                                             .collect(Collectors.toSet()));
        entityWhitelist.clear();
        entityWhitelist.addAll(ENTITY_WHITELIST.get().stream()
                                               .map(Config::getEntitiesFromId)
                                               .flatMap(Set::stream)
                                               .filter(Objects::nonNull)
                                               .collect(Collectors.toSet()));
        entityBlacklist.clear();
        entityBlacklist.addAll(ENTITY_BLACKLIST.get().stream()
                                               .map(Config::getEntitiesFromId)
                                               .flatMap(Set::stream)
                                               .filter(Objects::nonNull)
                                               .filter(id -> !entityWhitelist.contains(id))
                                               .collect(Collectors.toSet()));
    }



    @SuppressWarnings("CallToPrintStackTrace")
    public static void forceLoadCommonConfigs()
    {
        // 通过命令调用的、基于暴力读取文件的配置手动热更新方案，
        // 用于替代Forge自带的可靠性极差的基于事件的配置文件热更新

        Path commonPath = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config").resolve(SummoningWand.MODID + "-common.toml");
        if (Files.exists(commonPath)) {
            try (Stream<String> lines = Files.lines(commonPath)) {
                Map<String, String> map = lines
                    .map((l) -> Pattern.compile("\\s*(.+)\\s*=\\s*(.+)\\s*").matcher(l))
                    .filter(Matcher::find)
                    .collect(Collectors.toMap(
                        (m)->m.group(1).strip(),
                        (m)->m.group(2).strip()
                    ));
                setConfigValue(WAND_USE_COOLDOWN, map);
                setConfigValue(WAND_HAS_DURABILITY, map);
                setConfigValue(WAND_OWNER_ONLY, map);
                setConfigValue(BLOCK_MAX_WIDTH, map);
                setConfigValue(BLOCK_WHITELIST, map);
                setConfigValue(BLOCK_BLACKLIST, map);
                setConfigValue(ENTITY_WHITELIST, map);
                setConfigValue(ENTITY_BLACKLIST, map);
            } catch (IOException e) {
                SummoningWand.LOGGER.error("\u001b[34mFail to read common config file\u001b[0m");
                e.printStackTrace();
            }
        }
        loadCommonConfigs();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void forceLoadClientConfigs()
    {
        // 通过命令调用的、基于暴力读取文件的配置手动热更新方案，
        // 用于替代Forge自带的可靠性极差的基于事件的配置文件热更新

        Path CLIENT_PATH = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config").resolve(SummoningWand.MODID + "-client.toml");
        if (Files.exists(CLIENT_PATH)) {
            try (Stream<String> lines = Files.lines(CLIENT_PATH)) {
                Map<String, String> map = lines
                        .map((l) -> Pattern.compile("\\s*(.+)=(.+)\\s*").matcher(l))
                        .filter(Matcher::find)
                        .collect(Collectors.toMap(
                            (m)->m.group(1).strip(),
                            (m)->m.group(2).strip()
                        ));
                setConfigValue(WAND_BIND_HAND, map);
                setConfigValue(WAND_TP_HAND, map);
            } catch (IOException e) {
                SummoningWand.LOGGER.error("\u001b[34mFail to read client config file\u001b[0m");
                e.printStackTrace();
            }
        }
        loadClientConfigs();
    }

    @SuppressWarnings("SameParameterValue")
    private static void setConfigValue(ForgeConfigSpec.IntValue cv, Map<String, String> map)
    {
        String key = cv.getPath().get(cv.getPath().size() - 1);
        cv.set(Integer.valueOf(map.get(key)));
    }

    @SuppressWarnings("SameParameterValue")
    private static void setConfigValue(ForgeConfigSpec.DoubleValue cv, Map<String, String> map)
    {
        String key = cv.getPath().get(cv.getPath().size() - 1);
        cv.set(Double.valueOf(map.get(key)));
    }

    @SuppressWarnings("SameParameterValue")
    private static void setConfigValue(ForgeConfigSpec.BooleanValue cv, Map<String, String> map)
    {
        String key = cv.getPath().get(cv.getPath().size() - 1);
        cv.set(Boolean.valueOf(map.get(key)));
    }

    @SuppressWarnings("SameParameterValue")
    private static void setConfigValue(ForgeConfigSpec.EnumValue<WandUseHand> cv, Map<String, String> map)
    {
        String key = cv.getPath().get(cv.getPath().size() - 1);
        cv.set(WandUseHand.valueOf(map.get(key).replace("\"", "")));
    }

    @SuppressWarnings("SameParameterValue")
    private static void setConfigValue(ForgeConfigSpec.ConfigValue<List<? extends String>> cv, Map<String, String> map)
    {
        String key = cv.getPath().get(cv.getPath().size() - 1);
        List<String> list = List.of(
            map.get(key).replace("[", "").replace("]", "")
               .replace("\"", "").split("\\s*,\\s*")
        );
        cv.set(list.stream().filter((s) -> !s.isEmpty()).toList());
    }



    public static MutableComponent displayCommonConfigs()
    {
        return Component.translatable(
            """
                wandUseCooldown=%s
                wandHasDurability=%s
                wandOwnerOnly=%s
                blockMaxWidth=%s
                blockWhitelist=[%s]
                blockBlacklist=[%s]
                entityWhitelist=[%s]
                entityBlacklist=[%s]""",
            Component.literal(String.valueOf(wandUseCooldown)).withStyle(ChatFormatting.YELLOW),
            Component.literal(String.valueOf(wandHasDurability)).withStyle(ChatFormatting.YELLOW),
            Component.literal(String.valueOf(wandOwnerOnly)).withStyle(ChatFormatting.YELLOW),
            Component.literal(String.valueOf(blockMaxWidth)).withStyle(ChatFormatting.YELLOW),
            displayBlockSet(blockWhitelist),
            displayBlockSet(blockBlacklist),
            displayEntitySet(entityWhitelist),
            displayEntitySet(entityBlacklist)
        ).withStyle(ChatFormatting.BLUE);
    }



    private static Set<Block> getBlocksFromId(@NotNull String id)
    {
        if (id.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(id.substring(1));
            if (location != null) {
                ITagManager<Block> tagManager = ForgeRegistries.BLOCKS.tags();
                if (tagManager != null) {
                    return tagManager.getTag(TagKey.create(Registries.BLOCK, location))
                                     .stream().collect(Collectors.toSet());
                }
            }
        } else {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null && ForgeRegistries.BLOCKS.containsKey(location)) {
                Block block = ForgeRegistries.BLOCKS.getValue(location);
                if (block != null)
                    return Set.of(block);
            }
        }
        return Set.of();
    }

    private static Set<EntityType<?>> getEntitiesFromId(@NotNull String id)
    {
        if (id.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(id.substring(1));
            if (location != null) {
                ITagManager<EntityType<?>> tagManager = ForgeRegistries.ENTITY_TYPES.tags();
                if (tagManager != null) {
                    return tagManager.getTag(TagKey.create(Registries.ENTITY_TYPE, location))
                                     .stream().collect(Collectors.toSet());
                }
            }
        } else {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null && ForgeRegistries.ENTITY_TYPES.containsKey(location)) {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(location);
                if (entityType != null)
                    return Set.of(entityType);
            }
        }
        return Set.of();
    }

    private static MutableComponent displayBlockSet(Set<Block> set)
    {
        List<MutableComponent> components = set.stream().filter(Objects::nonNull).map(
            block -> {
                ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
                return location==null
                    ? null
                    : Component.literal(location.toString()).withStyle(ChatFormatting.LIGHT_PURPLE);
            }
        ).filter(Objects::nonNull).toList();

        if (components.isEmpty())
            return Component.empty();

        for (int i=1; i<components.size(); i++)
            components.get(0).append(Component.literal(", ").withStyle(ChatFormatting.BLUE))
                      .append(components.get(i));
        return components.get(0);
    }

    private static MutableComponent displayEntitySet(Set<EntityType<?>> set)
    {
        List<MutableComponent> components = set.stream().filter(Objects::nonNull).map(
                entityType -> {
                    ResourceLocation location = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                    return location==null
                            ? null
                            : Component.literal(location.toString()).withStyle(ChatFormatting.LIGHT_PURPLE);
                }
        ).filter(Objects::nonNull).toList();

        if (components.isEmpty())
            return Component.empty();

        for (int i=1; i<components.size(); i++)
            components.get(0).append(Component.literal(", ").withStyle(ChatFormatting.BLUE))
                      .append(components.get(i));
        return components.get(0);
    }



    @Mod.EventBusSubscriber(modid = SummoningWand.MODID)
    public static class ForgeCommonEvents
    {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event)
        {
            // 方块、物品等注册项在游戏启动过程中加载，但它们的标签只会在载入世界时加载。
            // 因此必须在每次启动后首次载入世界时重新加载一次配置，否则通过标签只能获取到空的注册项列表。
            loadCommonConfigs();
            SummoningWand.LOGGER.info("\u001b[34mCommon config is reloaded on server started.\u001b[0m");
        }

        @SubscribeEvent
        public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event)
        {
            WandUseHandManager.removeHandConfig(event.getEntity());
        }
    }



    @Mod.EventBusSubscriber(modid = SummoningWand.MODID, value = Dist.CLIENT)
    public static class ForgeClientEvents
    {
        @SubscribeEvent
        public static void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event)
        {
            loadClientConfigs();
            SummoningWand.LOGGER.info("\u001b[34mClient config is reloaded on logging in a server.\u001b[0m");
            WandUseHandManager.sendToServer();
        }
    }



    @Mod.EventBusSubscriber(
        modid = SummoningWand.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static class ModClientEvents
    {
        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event)
        {
            SummoningWand.LOGGER.info(
                "\u001b[34mClient config is reloaded on the changing of the TOML config file.\u001b[0m"
            );
            loadClientConfigs();
            if (Minecraft.getInstance().getConnection() != null)
                WandUseHandManager.sendToServer();
        }
    }



    @Mod.EventBusSubscriber(
        modid = SummoningWand.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static class ModCommonEvents
    {
        @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event)
        {
            SummoningWand.LOGGER.info(
                "\u001b[34mServer config is reloaded on the changing of the TOML config file.\u001b[0m"
            );
            loadCommonConfigs();
        }
    }

}
