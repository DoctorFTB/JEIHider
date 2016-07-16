package wanion.jeihider;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 */

import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class JEIHider implements IModPlugin
{
    private IItemBlacklist itemBlacklist;
    private IItemRegistry iItemRegistry;
    private final List<ItemStack> itemStacksToHide = new ArrayList<>();
    private final boolean hideRecyclerRecipes;
    private final boolean hideEnchantmentRecipes;
    private final Class<? extends IRecipeCategory> recyclerCategoryClass;
    private final Class<? extends IRecipeCategory> enchantmentCategoryClass;

    @SuppressWarnings("unchecked")
    public JEIHider() throws ClassNotFoundException
    {
        final Configuration config = new Configuration(new File("." + File.separatorChar + "JEI Hider.cfg"));
        hideRecyclerRecipes = Loader.isModLoaded("IC2") && config.get("hideRecyclerRecipes", Configuration.CATEGORY_GENERAL, false).getBoolean();
        hideEnchantmentRecipes = Loader.isModLoaded("IC2") && config.get("hideEnchantmentRecipes", Configuration.CATEGORY_GENERAL, false).getBoolean();
        recyclerCategoryClass = hideRecyclerRecipes ? (Class<? extends IRecipeCategory>) Class.forName("ic2.jeiIntegration.recipe.misc.ScrapboxRecipeCategory") : null;
        enchantmentCategoryClass = hideEnchantmentRecipes ? (Class<? extends IRecipeCategory>) Class.forName("") : null;
    }

    @Override
    public void register(@Nonnull IModRegistry iModRegistry)
    {
        itemBlacklist = iModRegistry.getJeiHelpers().getItemBlacklist();
        iItemRegistry = iModRegistry.getItemRegistry();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime iJeiRuntime)
    {
        if (itemStacksToHide.isEmpty()) {
            final IRecipeRegistry iRecipeRegistry = iJeiRuntime.getRecipeRegistry();
            for (final ItemStack itemStack : iItemRegistry.getItemList()) {
                final List<IRecipeCategory> recipeCategoriesOfInput = iRecipeRegistry.getRecipeCategoriesWithInput(itemStack);
                final List<IRecipeCategory> recipeCategoriesOfOutput = iRecipeRegistry.getRecipeCategoriesWithOutput(itemStack);
                if (recipeCategoriesOfInput.isEmpty() && recipeCategoriesOfOutput.isEmpty())
                    itemStacksToHide.add(itemStack);
                if ((hideRecyclerRecipes || hideEnchantmentRecipes) && !recipeCategoriesOfInput.isEmpty() && recipeCategoriesOfOutput.isEmpty()) {
                    if (recipeCategoriesOfInput.size() == 1) {
                        if (hideRecyclerRecipes && recipeCategoriesOfInput.get(0).getClass() == recyclerCategoryClass)
                            itemStacksToHide.add(itemStack);
                        else if (hideEnchantmentRecipes && recipeCategoriesOfInput.get(0).getClass() == enchantmentCategoryClass)
                            itemStacksToHide.add(itemStack);
                    } else if (recipeCategoriesOfInput.size() == 2 && hideRecyclerRecipes && hideEnchantmentRecipes)
                        if ((recipeCategoriesOfInput.get(0).getClass() == recyclerCategoryClass && recipeCategoriesOfInput.get(1).getClass() == enchantmentCategoryClass) ||
                                (recipeCategoriesOfInput.get(1).getClass() == recyclerCategoryClass && recipeCategoriesOfInput.get(0).getClass() == enchantmentCategoryClass))
                            itemStacksToHide.add(itemStack);
                }
            }
        }
        itemStacksToHide.forEach(itemBlacklist::addItemToBlacklist);
    }
}