package io.github.ggkkaa.spikefood;

import com.mojang.logging.LogUtils;
import io.github.ggkkaa.spikefood.recipe.ModRecipes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
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
        ItemStack stack = event.getCrafting();
        if (!stack.hasTag()) return;

        CompoundTag tag = stack.getTag();
        if (!tag.contains("Effect", Tag.TAG_LIST)) return;
        ListTag effectList = tag.getList("Effect", Tag.TAG_COMPOUND);

        CompoundTag effect = tag.getCompound("Effect");

        CompoundTag allEffects = tag.contains("Effects", Tag.TAG_COMPOUND)
                ? tag.getCompound("Effects")
                : new CompoundTag();

        UUID uuid = event.getEntity().getUUID();
        String uuidKey = uuid.toString();

        ListTag finalEffectList = effectList.copy();
        allEffects.put(uuidKey, finalEffectList);
        tag.put("Effects", allEffects);
        tag.remove("Effect");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public class CommonEvents {

        @SubscribeEvent
        public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
            ItemStack stack = event.getItem();
            if (!stack.hasTag()) return;

            CompoundTag tag = stack.getTag();
            if (!tag.contains("Effects", Tag.TAG_COMPOUND)) return;

            CompoundTag allEffects = tag.getCompound("Effects");

            for (String key : allEffects.getAllKeys()) {
                ListTag list = allEffects.getList(key, Tag.TAG_COMPOUND);

                for (Tag raw : list) {
                    if (!(raw instanceof CompoundTag effectTag)) continue;

                    String id = effectTag.getString("id");
                    int amplifier = effectTag.getInt("amplifier");
                    int duration = effectTag.getInt("duration");

                    ResourceLocation effectId = ResourceLocation.tryParse(id);
                    if (effectId == null) continue;

                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                    if (effect == null) continue;

                    MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier);
                    event.getEntity().addEffect(instance);
                }
            }
        }
    }
}
