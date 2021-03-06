package vanilla.entity;

import net.minecraft.entity.player.Player;
import vanilla.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.chat.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import vanilla.world.gen.feature.village.MerchantRecipe;
import vanilla.world.gen.feature.village.MerchantRecipeList;

public class NpcMerchant implements IMerchant {

	/**
	 * Instance of Merchants Inventory.
	 */
	private InventoryMerchant theMerchantInventory;

	/**
	 * This merchant's current player customer.
	 */
	private Player customer;

	/**
	 * The MerchantRecipeList instance.
	 */
	private MerchantRecipeList recipeList;
	private IChatComponent field_175548_d;

	public NpcMerchant(Player p_i45817_1_, IChatComponent p_i45817_2_) {
		this.customer = p_i45817_1_;
		this.field_175548_d = p_i45817_2_;
		this.theMerchantInventory = new InventoryMerchant(p_i45817_1_, this);
	}

	public Player getCustomer() {
		return this.customer;
	}

	public void setCustomer(Player p_70932_1_) {
	}

	public MerchantRecipeList getRecipes(Player p_70934_1_) {
		return this.recipeList;
	}

	public void setRecipes(MerchantRecipeList recipeList) {
		this.recipeList = recipeList;
	}

	public void useRecipe(MerchantRecipe recipe) {
		recipe.incrementToolUses();
	}

	/**
	 * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
	 * being played depending if the suggested itemstack is not null.
	 */
	public void verifySellingItem(ItemStack stack) {
	}

	/**
	 * Get the formatted ChatComponent that will be used for the sender's username in chat
	 */
	public IChatComponent getDisplayName() {
		return this.field_175548_d != null ? this.field_175548_d : new ChatComponentTranslation("entity.Villager.name", new Object[0]);
	}

}
