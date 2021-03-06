package vanilla.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.Player;
import net.minecraft.inventory.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import vanilla.entity.EntityLeashKnot;
import vanilla.entity.VanillaEntity;

public class ItemLead extends Item {

	public ItemLead() {
		this.setCreativeTab(CreativeTabs.tabTools);
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public boolean onItemUse(ItemStack stack, Player playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		Block block = worldIn.getBlockState(pos).getBlock();

		if (block instanceof BlockFence) {
			if (worldIn.isClientSide) {
				return true;
			}
			attachToFence(playerIn, worldIn, pos);
			return true;
		}
		return false;
	}

	public static boolean attachToFence(Player player, World worldIn, BlockPos fence) {
		EntityLeashKnot entityleashknot = EntityLeashKnot.getKnotForPosition(worldIn, fence);
		boolean flag = false;
		double d0 = 7.0D;
		int i = fence.getX();
		int j = fence.getY();
		int k = fence.getZ();

		for (VanillaEntity entityliving : worldIn.getEntitiesWithinAABB(
				VanillaEntity.class, new AxisAlignedBB((double) i - d0, (double) j - d0, (double) k - d0, (double) i + d0, (double) j + d0, (double) k + d0))) {
			if (entityliving.getLeashed() && entityliving.getLeashedToEntity() == player) {
				if (entityleashknot == null) {
					entityleashknot = EntityLeashKnot.createKnot(worldIn, fence);
				}

				entityliving.setLeashedToEntity(entityleashknot, true);
				flag = true;
			}
		}

		return flag;
	}

}
