package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.CreativeTabs;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.chat.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class BlockWorkbench extends Block {

	protected BlockWorkbench() {
		super(Material.wood);
		this.setCreativeTab(CreativeTabs.tabDecorations);
	}

	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, Player playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isClientSide) {
			return true;
		}

		playerIn.openGui(IInteractionObject.class, new BlockWorkbench.InterfaceCraftingTable(worldIn, pos));
		playerIn.triggerAchievement(StatList.craftingTableOpenedStat);
		return true;
	}

	public static class InterfaceCraftingTable implements IInteractionObject {

		private final World world;
		private final BlockPos position;

		public InterfaceCraftingTable(World worldIn, BlockPos pos) {
			this.world = worldIn;
			this.position = pos;
		}

		public String getName() {
			return null;
		}

		public boolean hasCustomName() {
			return false;
		}

		public IChatComponent getDisplayName() {
			return new ChatComponentTranslation(Blocks.crafting_table.getUnlocalizedName() + ".name", new Object[0]);
		}

		public Container createContainer(InventoryPlayer playerInventory, Player playerIn) {
			return new ContainerWorkbench(playerInventory, this.world, this.position);
		}

		public String getGuiID() {
			return "minecraft:crafting_table";
		}

	}

}
