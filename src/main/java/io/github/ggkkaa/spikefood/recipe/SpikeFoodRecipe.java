package io.github.ggkkaa.spikefood.recipe;

import com.google.gson.JsonObject;
import io.github.ggkkaa.spikefood.SpikeFood;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpikeFoodRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public SpikeFoodRecipe(ResourceLocation id) {
        this.id = id;
    }


    @Override
    public boolean matches(@NotNull CraftingContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            SpikeFood.LOGGER.debug("The Level is Client Side!!!!");
            return false;
        }

        boolean hasPotion = false;
        boolean hasFoodItem = false;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);

            if (stack.isEmpty()) continue;

            if (stack.isEdible()) {
                hasFoodItem = true;
            } else if (stack.getItem() instanceof PotionItem) {
                hasPotion = true;
            } else {
                return false;
            }
        }
        return hasFoodItem && hasPotion;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer pContainer, @NotNull RegistryAccess access) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack foodItem = ItemStack.EMPTY;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);

            if (stack.isEmpty()) continue;

            if (stack.isEdible()) {
                foodItem = stack;
            } else if (stack.getItem() instanceof PotionItem) {
                potion = stack;
            }
        }

        if (foodItem.isEmpty() || potion.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = foodItem.copy();
        CompoundTag tag = result.getOrCreateTag();

        Potion potionType = PotionUtils.getPotion(potion);
        if(!potionType.getEffects().isEmpty()) {
            ListTag storedEffects = new ListTag();
            for (MobEffectInstance effect : potionType.getEffects()) {
                CompoundTag effectTag = new CompoundTag();
                effectTag.putString("id", ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString());
                effectTag.putInt("amplifier", effect.getAmplifier());
                effectTag.putInt("duration", effect.getDuration());
                storedEffects.add(effectTag);
            }
            tag.put("Effect", storedEffects);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) {
        return new ItemStack(Items.BREAD);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack itemStack = inv.getItem(i);

            if (itemStack.getItem() instanceof PotionItem) {
                remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return remaining;
    }

    public static class Serializer implements RecipeSerializer<SpikeFoodRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public @NotNull SpikeFoodRecipe fromJson(@NotNull ResourceLocation id, JsonObject json) {
            return new SpikeFoodRecipe(id);
        }

        @Override
        public @Nullable SpikeFoodRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            return new SpikeFoodRecipe(pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SpikeFoodRecipe pRecipe) {
            // Nothing to sync.
        }

    }
}
