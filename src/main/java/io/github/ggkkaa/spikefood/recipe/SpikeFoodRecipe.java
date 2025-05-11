package io.github.ggkkaa.spikefood.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.ggkkaa.spikefood.SpikeFood;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SpikeFoodRecipe implements Recipe<CraftingContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final ResourceLocation id;

    public SpikeFoodRecipe(NonNullList<Ingredient> inputItems, ItemStack output, ResourceLocation id) {
        this.inputItems = inputItems;
        this.output = output;
        this.id = id;
    }


    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            SpikeFood.LOGGER.debug("The Level is Client Side!!!!");
            return false;
        }

        boolean hasIngredient1 = false;
        boolean hasIngredient2 = false;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);

            if (stack.isEmpty()) continue;

            if (inputItems.get(0).test(stack)) {
                hasIngredient1 = true;
            } else if (inputItems.get(1).test(stack)) {
                hasIngredient2 = true;
            } else {
                SpikeFood.LOGGER.debug("Invalid item detected!");
                return false;
            }
        }

        SpikeFood.LOGGER.debug("Returning {}", hasIngredient1 && hasIngredient2);
        return hasIngredient1 && hasIngredient2;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess access) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<SpikeFoodRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "food_spiking";
    }

    public static class Serializer implements RecipeSerializer<SpikeFoodRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SpikeFood.MOD_ID, "food_spiking");

        @Override
        public SpikeFoodRecipe fromJson(ResourceLocation id, JsonObject json) {
            SpikeFood.LOGGER.debug("Turning JSON into SpikeFoodRecipe...");
            ItemStack output = ShapedRecipe.itemStackFromJson(json);

            JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(2, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            SpikeFood.LOGGER.debug("Finished making SpikeFoodRecipe");
            return new SpikeFoodRecipe(inputs, output, id);
        }

        @Override
        public @Nullable SpikeFoodRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack output = pBuffer.readItem();
            return new SpikeFoodRecipe(inputs, output, pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, SpikeFoodRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());

            for ( Ingredient ingredient : pRecipe.getIngredients() ) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
        }

    }
}
