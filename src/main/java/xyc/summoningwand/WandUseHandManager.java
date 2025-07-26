package xyc.summoningwand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyc.summoningwand.enums.UseOn;
import xyc.summoningwand.enums.WandUseHand;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SummoningWand.MODID)
public class WandUseHandManager
{
    /** 客户端发送给服务端 */
    private record HandConfig(WandUseHand entityInteractHand, WandUseHand blockInteractHand)
    {
        public void encoder(FriendlyByteBuf buf)
        {
            // buf存储数据类型为byte[256]，首字节固定为数据包编号（超过255时从0开始重新计数）
            // 实际写入信息从第二个字节开始
            buf.writeEnum(entityInteractHand);
            buf.writeEnum(blockInteractHand);
        }

        public static HandConfig decoder(FriendlyByteBuf buf)
        {
            return new HandConfig(
                WandUseHand.values()[buf.getByte(1)],
                WandUseHand.values()[buf.getByte(2)]
            );
        }

        public void messageConsumer(Supplier<NetworkEvent.Context> context)
        {
            NetworkEvent.Context ctx = context.get();
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                ctx.enqueueWork(() -> handConfigs.put(sender, this));
                ctx.setPacketHandled(true);
                SummoningWand.LOGGER.info(
                    "\u001b[34mReceive hand config from \u001b[33m{}\u001b[34m: \u001b[33m{}\u001b[0m",
                    sender.getName().getString(), this
                );
            }
        }

        public static HandConfig fromClientConfig()
        {
            return new HandConfig(Config.wandBindHand, Config.wandTPHand);
        }

        @Override
        public String toString()
        {
            return String.format("HandConfig{wandBindHand: %s, wandTPHand: %s}", entityInteractHand, blockInteractHand);
        }

        public MutableComponent toComponent()
        {
            return Component.translatable(
                "{wandBindHand=%s, wandTPHand=%s}",
                Component.literal(entityInteractHand.name()).withStyle(ChatFormatting.YELLOW),
                Component.literal(blockInteractHand.name()).withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.LIGHT_PURPLE);
        }
    }



    /** 服务端发送给客户端 */
    private record HandConfigRequest()
    {
        public void encoder(FriendlyByteBuf buf) {}

        public static HandConfigRequest decoder(FriendlyByteBuf buf) { return new HandConfigRequest(); }

        public void messageConsumer(Supplier<NetworkEvent.Context> context)
        {
            NetworkEvent.Context ctx = context.get();
            Config.forceLoadClientConfigs();
            ctx.enqueueWork(WandUseHandManager::sendToServer);
            ctx.setPacketHandled(true);
            SummoningWand.LOGGER.info("\u001b[34mSend hand configs to server.\u001b[0m");
        }
    }




    private static final String PROTOCOL_VERSION = "1.0";

    private static final SimpleChannel HandConfigChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocationUtils.fromNamespaceAndPath(SummoningWand.MODID, "main"),
        () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static final HashMap<Player, HandConfig> handConfigs = new HashMap<>();

    public static WandUseHand getProperHand(Player player, UseOn useOn, boolean isClientSide)
    {
        return switch (useOn) {
            case ENTITY -> isClientSide ? Config.wandBindHand : handConfigs.get(player).entityInteractHand;
            case BLOCK -> isClientSide ? Config.wandTPHand : handConfigs.get(player).blockInteractHand;
        };
    }

    public static void register()
    {
        HandConfigChannel.registerMessage(
            packetId++,
            HandConfig.class,
            HandConfig::encoder,
            HandConfig::decoder,
            HandConfig::messageConsumer
        );
        HandConfigChannel.registerMessage(
            packetId++,
            HandConfigRequest.class,
            HandConfigRequest::encoder,
            HandConfigRequest::decoder,
            HandConfigRequest::messageConsumer
        );
        SummoningWand.LOGGER.info("\u001b[34mRegister hand config messages.\u001b[0m");
    }

    public static void sendToServer()
    {
        HandConfig msg = HandConfig.fromClientConfig();
        HandConfigChannel.sendToServer(msg);
        SummoningWand.LOGGER.info("\u001b[34mSend hand configs to server: \u001b[33m{}\u001b[0m", msg);
    }

    public static void requestFromClient(ServerPlayer player)
    {
        HandConfigChannel.send(PacketDistributor.PLAYER.with(() -> player), new HandConfigRequest());
        SummoningWand.LOGGER.info(
            "\u001b[34mRequest hand configs from client: \u001b[33m{}\u001b[0m", player.getName().getString()
        );
    }

    public static void requestFromAllClients()
    {
        HandConfigChannel.send(PacketDistributor.ALL.noArg(), new HandConfigRequest());
        SummoningWand.LOGGER.info("\u001b[34mRequest hand configs from all clients\u001b[0m");
    }

    public static void removeHandConfig(Player player)
    {
        HandConfig msg = handConfigs.remove(player);
        SummoningWand.LOGGER.info("\u001b[34mRemove hand configs from server: \u001b[33m{}\u001b[0m", msg);
    }

    public static MutableComponent displayHandConfig(Player player)
    {
        return Component.literal("Player: ").withStyle(ChatFormatting.BLUE)
                        .append(((MutableComponent)player.getName()).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(", Hand Config: ").withStyle(ChatFormatting.BLUE))
                        .append(handConfigs.get(player).toComponent());
    }

    public static MutableComponent displayAllHandConfigs()
    {
        List<MutableComponent> components = handConfigs.keySet().stream().filter(Objects::nonNull)
                                                       .map(WandUseHandManager::displayHandConfig).toList();

        if (components.isEmpty())
            return Component.empty();

        for (int i=1; i<components.size(); i++)
            components.get(0).append("\n").append(components.get(i));
        return components.get(0);
    }
}
