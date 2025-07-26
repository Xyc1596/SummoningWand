package xyc.summoningwand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SummoningWand.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SummoningWandCommands
{
    public static final String
        MESSAGE_HEAD = "---------------- %s ----------------\n%s",
        MESSAGE_CONFIG_ALL_CLIENTS = "message.config.all_clients",
        MESSAGE_CONFIG_CLIENT = "message.config.client",
        MESSAGE_CONFIG_COMMON = "message.config.common",
        MESSAGE_REFRESH_ALL_CLIENTS = "message.refresh.all_clients",
        MESSAGE_REFRESH_CLIENT = "message.refresh.client",
        MESSAGE_REFRESH_COMMON = "message.refresh.common";



    /** 获取所有玩家的客户端设置 */
    protected static class GetAllClientConfigs implements Command<CommandSourceStack>
    {
        public static final GetAllClientConfigs INSTANCE = new GetAllClientConfigs();

        @Override
        public int run(CommandContext<CommandSourceStack> context)
        {
            context.getSource().sendSystemMessage(
                Component.translatable(
                    MESSAGE_HEAD,
                    Component.translatable(MESSAGE_CONFIG_ALL_CLIENTS),
                    WandUseHandManager.displayAllHandConfigs()
                ).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)
            );
            return SINGLE_SUCCESS;
        }
    }

    /** 获取自己的客户端设置 */
    protected static class GetClientConfig implements Command<CommandSourceStack>
    {
        public static final GetClientConfig INSTANCE = new GetClientConfig();

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
        {
            Player player = context.getSource().getPlayerOrException();
            player.sendSystemMessage(
                Component.translatable(
                    MESSAGE_HEAD,
                    Component.translatable(MESSAGE_CONFIG_CLIENT),
                    WandUseHandManager.displayHandConfig(player)
                ).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)
            );
            return SINGLE_SUCCESS;
        }
    }

    /** 获取服务器设置 */
    protected static class GetCommonConfig implements Command<CommandSourceStack>
    {
        public static final GetCommonConfig INSTANCE = new GetCommonConfig();

        @Override
        public int run(CommandContext<CommandSourceStack> context)
        {
            context.getSource().sendSystemMessage(
                Component.translatable(
                    MESSAGE_HEAD,
                    Component.translatable(MESSAGE_CONFIG_COMMON),
                    Config.displayCommonConfigs()
                ).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)
            );
            return SINGLE_SUCCESS;
        }
    }

    /** 将全部玩家客户端设置同步到服务端 */
    protected static class RefreshAllClientConfigs implements Command<CommandSourceStack>
    {
        public static final RefreshAllClientConfigs INSTANCE = new RefreshAllClientConfigs();

        @Override
        public int run(CommandContext<CommandSourceStack> context)
        {
            CommandSourceStack src = context.getSource();
            WandUseHandManager.requestFromAllClients();
            src.sendSuccess(() -> Component.translatable(MESSAGE_REFRESH_ALL_CLIENTS), true);
            return SINGLE_SUCCESS;
        }
    }

    /** 将自己的客户端设置同步到服务端 */
    protected static class RefreshClientConfig implements Command<CommandSourceStack>
    {
        public static final RefreshClientConfig INSTANCE = new RefreshClientConfig();

        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
        {
            CommandSourceStack src = context.getSource();
            WandUseHandManager.requestFromClient(src.getPlayerOrException());
            src.sendSuccess(() -> Component.translatable(MESSAGE_REFRESH_CLIENT), true);
            return SINGLE_SUCCESS;
        }
    }

    /** 刷新服务端设置 */
    protected static class RefreshCommonConfig implements Command<CommandSourceStack>
    {
        public static final RefreshCommonConfig INSTANCE = new RefreshCommonConfig();

        @Override
        public int run(CommandContext<CommandSourceStack> context)
        {
            Config.forceLoadCommonConfigs();
            context.getSource().sendSuccess(() -> Component.translatable(MESSAGE_REFRESH_COMMON), true);
            return SINGLE_SUCCESS;
        }
    }



    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(
            Commands.literal(SummoningWand.MODID).then(
                Commands.literal("config").then(
                    Commands.literal("allClients").then(
                        Commands.literal("GET")
                                .executes(GetAllClientConfigs.INSTANCE)
                    ).then(
                        Commands.literal("REFRESH")
                                .executes(RefreshAllClientConfigs.INSTANCE)
                    ).requires(stack -> stack.hasPermission(4))
                ).then(
                    Commands.literal("client").then(
                        Commands.literal("GET")
                                .executes(GetClientConfig.INSTANCE)
                    ).then(
                        Commands.literal("REFRESH")
                                .executes(RefreshClientConfig.INSTANCE)
                    ).requires(stack -> stack.hasPermission(0) && stack.isPlayer())
                ).then(
                    Commands.literal("common").then(
                        Commands.literal("GET")
                                .requires(stack -> stack.hasPermission(0))
                                .executes(GetCommonConfig.INSTANCE)
                    ).then(
                        Commands.literal("REFRESH")
                                .requires(stack -> stack.hasPermission(4))
                                .executes(RefreshCommonConfig.INSTANCE)
                    )
                )
            )
        );
    }
}
