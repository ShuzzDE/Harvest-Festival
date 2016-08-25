package joshie.harvest.api.cooking;

import joshie.harvest.cooking.Utensil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.List;

public interface IFoodRegistry {
    /** Returns a collection of all the ingredients that are registered **/
    Collection<ICookingIngredient> getIngredients();

    /** Registers this item as a cooking component type
     *  @param      stack the itemstack
     *  @param      component the component **/
    void register(ItemStack stack, ICookingIngredient component);
    
    /** Registers a special recipe handler, called before the normal
     *  recipes are ever processed
     * @param handler */
    void registerRecipeHandler(ISpecialRecipeHandler handler);

    /** Call this if you don't wish to implement IKnife,
     *  Use OreDictionary.WILDCARD_VALUE if metadata doesn't matter
     * @param stack the knife stack */
    void registerKnife(ItemStack stack);

    /** Returns a set of all the components this stack provides
     * 
     * @param       stack the stack
     * @return      the components the stack provides*/
    List<ICookingIngredient> getCookingComponents(ItemStack stack);
    
    /** Returns a cooking component based on it's unlocalized name
     *  Cooking components are regitered when an item is registered to them,
     *  if they have no stack, then they are not registered, and therefore
     *  do not exist. And cannot be found with this method.
     *  
     *  @param      unlocalized the unlocalized name of the component
     *  @return     the cooking component  */
    ICookingIngredient getIngredient(String unlocalized);
    
    /** Creates a new cooking category, e.g. "fruit"
     *  To add things to this category, simple call 
     *  newCategory("fruit").add(apple, banana, pineapple);
     *  You could recreate this with the newIngredient
     *  by setting stats to 0, but this is for convenience.
     * @param       unlocalized name
     * @return      the component
     */
    ICookingIngredient newCategory(String unlocalized);
    
    /** Creates a new ingredient type for usage in cooking
     *  @param      unlocalised the unlocalised name, this needs to be unique
     *          The food stats are how much this ingredient affects recipes
     *          when it gets added to them as optional ingredients;
     *  @param      hunger the hunger (vanilla) this ingredient fills
     *  @param      saturation the saturation (vanilla) this ingredient fills
     *  @param      exhaustion     how much exhaustion this ingredient adds
     *  @param      eatTimer the eatTimer, this is how many ticks extra this adds to eating time **/
    ICookingIngredient newIngredient(String unlocalised, int hunger, float saturation, float exhaustion, int eatTimer);

    /** Returns a fluid for this ingredient if it's valid
     *  This is called by the client when rendering, to determine how to rend the item.
     *  Returns null if the ingredient doesn't have a fluid associated with it.
     * @param       ingredient the ingredient
     * @return      a fluid, can be null */
    ResourceLocation getFluid(ItemStack ingredient);
    
    /** Returns a utensil based on the unlocalized name
     *  Returns kitchen counter if none was found.
     * 
     * @param       unlocalized name
     * @return      the utensil */
    Utensil getUtensil(String unlocalized);

    /** Add this meal to the recipe registry,
     *
     * @param key            the resource path for this meal (i.e. harvestfestival:cookies)
     * @param utensil        the utensil this meal requires
     * @param hunger         how much hunger this meal fills
     * @param saturation     how much saturation this meal provides
     * @param exhaustion     how much exhaustion this meal adds
     * @param eatTimer       how long this meal should take to eat
     * @param ingredients    the ingredients for this recipe, use getIngredient to grab any of the default ingredients
     * @return  the recipe that was added, where you can manipulate it further if neccessary
     */
    IMealRecipe addMeal(ResourceLocation key, Utensil utensil, int hunger, float saturation, float exhaustion, int eatTimer, ICookingIngredient... ingredients);

    /** Add a recipe, with a custom stack output
     *  Use case for such thing is wheat > bread
     * @param output    the output item
     * @param utensil   the utensil
     * @param ingredients the ingredients */
    void addRecipe(ItemStack output, Utensil utensil, ICookingIngredient... ingredients);

    /** Returns a resulting itemstack for the ingredients input
     *  @param      utensil the utensil in use
     *  @param      ingredients the ingredients
     *  @return     the resulting item */
    ItemStack getResult(Utensil utensil, List<ItemStack> ingredients);

    /** Returns a default copy of this meal
     *  @param name     this is the resource path of the name, if it's a harvestfestival meal you can just use the name,
     *                  if it's added by other mods you should use the normal resourcepath **/
    ItemStack getMeal(String name);
    
    /** Returns a copy of this meal, with it's best stats
     *  Can and will return null if it was not found.
     *  @param name     this is the resource path of the name, if it's a harvestfestival meal you can just use the name,
     *                  if it's added by other mods you should use the normal resourcepath **/
    ItemStack getBestMeal(String name);
}
