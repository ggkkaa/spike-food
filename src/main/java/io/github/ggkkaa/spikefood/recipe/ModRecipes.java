package io.github.ggkkaa.spikefood.recipe;

import io.github.ggkkaa.spikefood.SpikeFood;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SpikeFood.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SpikeFoodRecipe>> SPIKE_FOOD_SERIALIZER =
            SERIALIZERS.register("food_spiking", () -> SpikeFoodRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
