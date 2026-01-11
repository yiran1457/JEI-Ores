package net.yiran.jei_ores;

import net.yiran.jei_ores.networking.JeiOresPacketHandler;
import net.yiran.jei_ores.networking.FeaturesSender;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("removal")
@Mod(JeiOres.MODID)
public class JeiOres {
    public static final String MODID = "jei_ores";
    public static final Logger LOG = LoggerFactory.getLogger("JEI Ores");

    public JeiOres() {

        JeiOres.init();
        JeiOresPacketHandler.init();

        MinecraftForge.EVENT_BUS.addListener(JeiOres::onDatapackSync);

        FMLJavaModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                Config.SPEC
        );
    }

    public static void init() {
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        List<ServerPlayer> players = event.getPlayer() == null ? event.getPlayerList().getPlayers() : List.of(event.getPlayer());
        players.forEach(playerListPlayer -> FeaturesSender.onSyncDataPackContents(
                playerListPlayer,
                p -> true,
                (player, packet) ->
                        JeiOresPacketHandler.CHANNELS.get(packet.getClass()).send(PacketDistributor.PLAYER.with(() -> player), packet)
        ));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
