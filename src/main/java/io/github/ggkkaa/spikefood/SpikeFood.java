package io.github.ggkkaa.spikefood;

import com.mojang.logging.LogUtils;
import io.github.ggkkaa.spikefood.recipe.ModRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SpikeFood.MOD_ID)
public class SpikeFood
{
    public static final String MOD_ID = "spikefood";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpikeFood(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        LOGGER.debug("Registering Recipes...");
        ModRecipes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        LOGGER.debug("UUID: {}", playerUUID);
    }
}
