package xyc.summoningwand;

import dev.dubhe.curtain.features.player.patches.EntityPlayerMPFake;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import xyc.summoningwand.enums.UseOn;
import xyc.summoningwand.enums.WandUseHand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static xyc.summoningwand.SummoningWand.MODID;
import static xyc.summoningwand.SummoningWand.SUMMONING_WAND;

@Mod.EventBusSubscriber(modid = MODID)
public class SummoningWandItem extends Item
{
    public static final String
        REGISTRY_NAME = "summoning_wand",
        MESSAGE_BIND_SUCCESS = "message.summoningwand.bind.success",
        MESSAGE_BIND_ENTITY_BANNED = "message.summoningwand.bind.entity_forbidden",
        MESSAGE_TP_SUCCESS = "message.summoningwand.tp.success",
        MESSAGE_TP_BLOCK_BANNED = "message.summoningwand.tp.block_forbidden",
        MESSAGE_TP_BLOCK_BLOCKED = "message.summoningwand.tp.block_blocked",
        MESSAGE_TP_NOT_OWNER = "message.summoningwand.tp.not_owner",
        MESSAGE_TP_ENTITY_NOT_FOUND = "message.summoningwand.tp.entity_not_found",
        MESSAGE_TP_ENTITY_NOT_BOUND = "message.summoningwand.tp.entity_not_bound",
        TOOLTIP_HOLD_FOR_USAGE = "tooltip.summoningwand.holdForUsage",
        TOOLTIP_OWNER = "tooltip.summoningwand.owner",
        TOOLTIP_TARGET = "tooltip.summoningwand.target",
        TOOLTIP_CLICK_ENTITY1 = "tooltip.summoningwand.clickEntity1",
        TOOLTIP_CLICK_ENTITY2 = "tooltip.summoningwand.clickEntity2",
        TOOLTIP_CLICK_BLOCK1 = "tooltip.summoningwand.clickBlock1",
        TOOLTIP_CLICK_BLOCK2 = "tooltip.summoningwand.clickBlock2",
        TOOLTIP_CLICK_BLOCK3 = "tooltip.summoningwand.clickBlock3",
        TOOLTIP_CRAFT1 = "tooltip.summoningwand.craft1",
        TOOLTIP_CRAFT2 = "tooltip.summoningwand.craft2",
        CONST_MAIN_HAND = "const.summoningwand.main_hand",
        CONST_OFFHAND = "const.summoningwand.offhand",
        CONST_BOTH_HAND = "const.summoningwand.both_hand",
        CONST_LBRACKET = "const.summmoningwand.lbracket",
        CONST_RBRACKET = "const.summoningwand.rbracket",
        SOUND_TP_REGISTRY_NAME = "tp",
        SOUND_TP = "sound.summoningwand.tp";



    public SummoningWandItem()
    {
        super(new Item.Properties().durability(132));
    }



    protected enum TargetPosType
    {
        /** 可用 */
        VALID,
        /** 被有碰撞箱的方块占用 */
        BLOCKED,
        /** 在配置中被禁用 */
        BANNED
    }



    /**
     * 取消原有交互结果（如打开村民交易GUI、让宠物坐下、开/关门）
     */
    protected static void cancel(PlayerInteractEvent event, InteractionResult result)
    {
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    protected static TargetPosType getTargetPosType(
        Level level,
        BlockPos blockPos,
        Direction direction,
        Entity target,
        boolean useNormal
    )
    {
        BlockState state = level.getBlockState(blockPos);
        Block block = state.getBlock();
        if (Config.blockWhitelist.contains(block))
            return TargetPosType.VALID;
        if (Config.blockBlacklist.contains(block))
            return TargetPosType.BANNED;

        if (target instanceof LeashFenceKnotEntity) {
            ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
            if (tags != null
             && tags.getTag(TagKey.create(Registries.BLOCK, ResourceLocationUtils.withDefaultNamespace("fences")))
                    .contains(block)
            ) return TargetPosType.VALID;
        }

        if (target instanceof Minecart && block instanceof BaseRailBlock)
            return TargetPosType.VALID;

        VoxelShape shape = state.getCollisionShape(level, blockPos);
        if (shape.isEmpty())
            return TargetPosType.VALID;
        else {
            if (useNormal) {
                double wNormal = switch (direction) {
                    case UP -> shape.max(Direction.Axis.Y);
                    case DOWN -> 1 - shape.min(Direction.Axis.Y);
                    case EAST -> shape.max(Direction.Axis.X);
                    case WEST -> 1 - shape.min(Direction.Axis.X);
                    case SOUTH -> shape.max(Direction.Axis.Z);
                    case NORTH -> 1 - shape.min(Direction.Axis.Z);
                };
                return wNormal > Config.blockMaxWidth ? TargetPosType.BLOCKED : TargetPosType.VALID;
            }
            else {
                double wY = Math.min(shape.max(Direction.Axis.Y), 1 - shape.min(Direction.Axis.Y)), // UP & DOWN
                       wX = Math.min(shape.max(Direction.Axis.X), 1 - shape.min(Direction.Axis.X)), // EAST & WEST
                       wZ = Math.min(shape.max(Direction.Axis.Z), 1 - shape.min(Direction.Axis.Z)); // SOUTH & NORTH
                return switch (direction) {
                        case UP, DOWN -> Math.min(wY, Math.min(wX, wZ));
                        case EAST, WEST -> Math.min(wX, Math.min(wY, wZ));
                        case SOUTH, NORTH -> Math.min(wZ, Math.min(wX, wY));
                    } > Config.blockMaxWidth ? TargetPosType.BLOCKED : TargetPosType.VALID;
            }
        }
    }

    public static Pair<ItemStack, InteractionHand> getWandFromProperHand(Player player, WandUseHand hand)
    {
        switch (hand) {
            case MAIN -> {
                ItemStack stack = player.getMainHandItem();
                return stack.getItem() instanceof SummoningWandItem
                        ? Pair.of(stack, InteractionHand.MAIN_HAND)
                        : null;
            }
            case OFF -> {
                ItemStack stack = player.getOffhandItem();
                return stack.getItem() instanceof SummoningWandItem
                        ? Pair.of(stack, InteractionHand.OFF_HAND)
                        : null;
            }
            default -> {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof SummoningWandItem)
                    return Pair.of(stack, InteractionHand.MAIN_HAND);
                else {
                    stack = player.getOffhandItem();
                    return stack.getItem() instanceof SummoningWandItem
                            ? Pair.of(stack, InteractionHand.OFF_HAND)
                            : null;
                }
            }
        }
    }

    protected static boolean notHoldingWandInProperHand(Player player, WandUseHand properHand)
    {
        return !switch (properHand) {
            case MAIN -> player.getMainHandItem().getItem() instanceof SummoningWandItem;
            case OFF -> player.getOffhandItem().getItem() instanceof SummoningWandItem;
            case BOTH -> player.getMainHandItem().getItem() instanceof SummoningWandItem
                      || player.getOffhandItem().getItem() instanceof SummoningWandItem;
        };
    }

    protected static boolean isValidTarget(Entity target)
    {
        EntityType<?> type = target.getType();
        if (Config.entityWhitelist.contains(type))
            return true;
        return !(isRealPlayer(target) || Config.entityBlacklist.contains(type));
    }

    protected static boolean isRealPlayer(Entity target)
    {
        if (target instanceof Player player)
            return ModList.get().isLoaded("curtain") && !(player instanceof EntityPlayerMPFake);
        else
            return false;
    }

    @Nullable
    protected static CompoundTag getBoundTags(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        return tag==null || tag.getString("target").isEmpty() ? null : tag;
    }

    protected static void teleport(ServerLevel level, Entity target, BlockPos pos)
    {
        // 参考：net.minecraft.server.commands.TeleportCommand#performeTeleport
        // Curtain假人直接moveTo会错位（模型与实际位置不一致）
        if (target.teleportTo(level, pos.getX()+0.5F, pos.getY(), pos.getZ()+0.5F,
                              Set.of(), target.getYRot(), target.getXRot())) {
            if (!(target instanceof LivingEntity living && living.isFallFlying())) {
                target.setDeltaMovement(target.getDeltaMovement().multiply(1, 0, 1));
                target.setOnGround(true);
            }
            if (target instanceof PathfinderMob mob)
                mob.getNavigation().stop();
        }
    }



    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public Component getName(ItemStack stack)
    {
        CompoundTag tag = getBoundTags(stack);
        return tag==null
            ? this.getDescription()
            : ((MutableComponent)this.getDescription())
                .append(Component.translatable(CONST_LBRACKET))
                .append(Component.translatable(tag.getString("targetName")))
                .append(Component.translatable(CONST_RBRACKET))
                .withStyle(ChatFormatting.YELLOW);
    }



    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        // 右击方块时会调用4次RightClickBlock事件（客户端+主手、客户端+副手、服务端+主手、服务端+副手）；
        // 如果主手事件被取消，则副手事件不会执行；客户端事件取消不影响服务端事件。

        if (event.isCanceled())
            return;

        Player player = event.getEntity();
        boolean isClientSide = event.getLevel().isClientSide();
        WandUseHand properHand = WandUseHandManager.getProperHand(player, UseOn.BLOCK, isClientSide);
        if (notHoldingWandInProperHand(player, properHand))
            return;     // 持魔杖的手不正确时正常交互

        var pair = getWandFromProperHand(player, properHand);
        if (pair == null) {
            cancel(event, InteractionResult.FAIL);
            return;
        }

        ItemStack itemStack = pair.getLeft();
        Item item = itemStack.getItem();
        if (player.getCooldowns().isOnCooldown(item)) {
            cancel(event, InteractionResult.FAIL);
            return;
        }

        InteractionHand hand = pair.getRight();
        player.swing(hand);

        if (isClientSide) {     // ClientLevel无法从UUID获取实体
            cancel(event, InteractionResult.PASS);  // InteractionResult.SUCCESS会导致始终挥动主手
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        CompoundTag tag = getBoundTags(itemStack);
        if (tag==null) {
            player.displayClientMessage(
                Component.translatable(
                    MESSAGE_TP_ENTITY_NOT_BOUND
                ).withStyle(ChatFormatting.RED), true
            );
            cancel(event, InteractionResult.FAIL);
            return;
        }

        UUID ownerUUID = UUID.fromString(tag.getString("owner"));
        if (Config.wandOwnerOnly && !player.getUUID().equals(ownerUUID)) {
            player.displayClientMessage(
                Component.translatable(
                    MESSAGE_TP_NOT_OWNER,
                    Component.literal(tag.getString("ownerName")).withStyle(ChatFormatting.YELLOW)
                ).withStyle(ChatFormatting.RED), true
            );
            cancel(event, InteractionResult.FAIL);
            return;
        }

        UUID targetUUID = UUID.fromString(tag.getString("target"));
        Entity target = level.getEntity(targetUUID);
        if (target==null || target.isRemoved()) {
            player.displayClientMessage(
                Component.translatable(
                    MESSAGE_TP_ENTITY_NOT_FOUND,
                    Component.translatable(tag.getString("targetName")).withStyle(ChatFormatting.YELLOW)
                ).withStyle(ChatFormatting.RED), true
            );
            cancel(event, InteractionResult.FAIL);
            return;
        }

        BlockHitResult hitVec = event.getHitVec();
        Direction direction = hitVec.getDirection();
        BlockPos blockPos = hitVec.getBlockPos();
        TargetPosType targetPosType = getTargetPosType(level, blockPos, direction, target, true);
        if (targetPosType != TargetPosType.VALID) {
            blockPos = blockPos.relative(direction);
            targetPosType = getTargetPosType(level, blockPos, direction, target, false);
        }
        switch (targetPosType) {
            case VALID -> {
                if (player.isShiftKeyDown())
                    target.unRide();
                teleport(level, target, blockPos);
                player.displayClientMessage(
                    Component.translatable(
                        MESSAGE_TP_SUCCESS,
                        Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW),
                        Component.literal(
                            String.format("(%d, %d, %d)", blockPos.getX(), blockPos.getY(), blockPos.getZ())
                        ).withStyle(ChatFormatting.YELLOW)
                    ).withStyle(ChatFormatting.GOLD), true
                );
                if (Config.wandHasDurability)
                    itemStack.hurtAndBreak(
                        1,
                        player,
                        player1 -> player1.broadcastBreakEvent(hand)
                    );
                player.getCooldowns().addCooldown(item, Config.wandUseCooldown);
                target.playSound(SummoningWand.WAND_TP_SOUND.get());
                cancel(event, InteractionResult.sidedSuccess(level.isClientSide()));
                return;
            }
            case BANNED -> player.displayClientMessage(
                Component.translatable(
                    MESSAGE_TP_BLOCK_BANNED,
                    level.getBlockState(blockPos).getBlock().getName().withStyle(ChatFormatting.YELLOW)
                ).withStyle(ChatFormatting.RED), true
            );
            case BLOCKED -> player.displayClientMessage(
                Component.translatable(
                    MESSAGE_TP_BLOCK_BLOCKED,
                    level.getBlockState(blockPos).getBlock().getName().withStyle(ChatFormatting.YELLOW)
                ).withStyle(ChatFormatting.RED), true
            );
        }
        cancel(event, InteractionResult.FAIL);
    }



    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event)
    {
        // 只对主手调用（客户端+主手、服务端+主手）

        if (event.isCanceled())
            return;

        Player player = event.getEntity();
        boolean isClientSide = event.getLevel().isClientSide();
        WandUseHand properHand = WandUseHandManager.getProperHand(player, UseOn.ENTITY, isClientSide);
        if (notHoldingWandInProperHand(player, properHand))
            return;     // 持魔杖的手不正确时正常交互

        var pair = getWandFromProperHand(player, properHand);
        if (pair == null) {
            cancel(event, InteractionResult.FAIL);
            return;
        }
        ItemStack itemStack = pair.getLeft();

        player.swing(pair.getRight());
        if (isClientSide) {
            cancel(event, InteractionResult.PASS);
            return;
        }

        Entity target = event.getTarget();
        if (!isValidTarget(target)) {
            player.displayClientMessage(
                Component.translatable(
                    MESSAGE_BIND_ENTITY_BANNED,
                    ((MutableComponent) target.getType().getDescription()).withStyle(ChatFormatting.YELLOW)
                ).withStyle(ChatFormatting.RED), true
            );
            cancel(event, InteractionResult.FAIL);
            return;
        }

        itemStack.addTagElement("owner", StringTag.valueOf(player.getStringUUID()));
        itemStack.addTagElement("target", StringTag.valueOf(target.getStringUUID()));
        itemStack.addTagElement("ownerName", StringTag.valueOf(player.getName().getString()));
        itemStack.addTagElement("targetName", StringTag.valueOf(target.getName().getString()));

        player.displayClientMessage(
            Component.translatable(
                MESSAGE_BIND_SUCCESS,
                Component.literal(target.getName().getString()).withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.GOLD), true
        );  // 直接使用target.getName().withStyle()会改变文本组件本身的颜色，导致UI标题变色；方块或方块实体无此问题
        cancel(event, InteractionResult.SUCCESS);
    }



    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(
        ItemStack itemStack,
        @Nullable Level level,
        List<Component> components,
        TooltipFlag isAdvanced
    )
    {
        if (level==null)
            return;

        CompoundTag tag = getBoundTags(itemStack);
        if (tag != null) {
            components.add(
                Component.translatable(TOOLTIP_OWNER, tag.getString("ownerName"))
                         .withStyle(ChatFormatting.GRAY)
            );
            components.add(
                Component.translatable(TOOLTIP_TARGET, Component.translatable(tag.getString("targetName")))
                         .withStyle(ChatFormatting.GRAY)
            );
        }

        components.add(
            Component.translatable(
                TOOLTIP_HOLD_FOR_USAGE, Component.literal("Shift").withStyle(ChatFormatting.WHITE)
            ).withStyle(ChatFormatting.DARK_GRAY)
        );

        if (Screen.hasShiftDown()) {
            components.add(Component.empty());
            components.add(Component.translatable(
                TOOLTIP_CLICK_ENTITY1,
                Component.translatable(switch (Config.wandBindHand) {
                    case MAIN -> CONST_MAIN_HAND;
                    case OFF -> CONST_OFFHAND;
                    case BOTH -> CONST_BOTH_HAND;
                }).withStyle(ChatFormatting.WHITE)
            ).withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable(TOOLTIP_CLICK_ENTITY2).withStyle(ChatFormatting.GOLD));

            components.add(Component.translatable(
                TOOLTIP_CLICK_BLOCK1,
                Component.translatable(switch (Config.wandTPHand) {
                    case MAIN -> CONST_MAIN_HAND;
                    case OFF -> CONST_OFFHAND;
                    case BOTH -> CONST_BOTH_HAND;
                }).withStyle(ChatFormatting.WHITE)
            ).withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable(TOOLTIP_CLICK_BLOCK2).withStyle(ChatFormatting.GOLD));
            components.add(Component.translatable(
                TOOLTIP_CLICK_BLOCK3, Component.literal("Shift").withStyle(ChatFormatting.WHITE)
            ).withStyle(ChatFormatting.GOLD));

            components.add(Component.translatable(TOOLTIP_CRAFT1).withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable(TOOLTIP_CRAFT2).withStyle(ChatFormatting.GOLD));
        }
    }



    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegisterEvents
    {
        @SubscribeEvent
        public static void registerItemProperty(FMLClientSetupEvent event)
        {
            event.enqueueWork(() ->
                ItemProperties.register(
                    SUMMONING_WAND.get(),
                    ResourceLocationUtils.fromNamespaceAndPath(MODID, "bind_state"),
                    (stack, level, living, id)
                        -> stack.getItem() instanceof SummoningWandItem && SummoningWandItem.getBoundTags(stack)!=null
                        ? 1F : 0F
                )
            );
        }

        @SubscribeEvent
        public static void registerCreativeModeTab(BuildCreativeModeTabContentsEvent event)
        {
            if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                var entries = event.getEntries();
                entries.putAfter(
                    new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK),
                    new ItemStack(SUMMONING_WAND.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                );
            }
        }
    }

}
