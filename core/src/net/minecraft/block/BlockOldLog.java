package net.minecraft.block;

import com.google.common.base.Predicate;

import java.util.List;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlockOldLog extends BlockLog {

	public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class, new Predicate<BlockPlanks.EnumType>() {
		public boolean apply(BlockPlanks.EnumType p_apply_1_) {
			return p_apply_1_.getMetadata() < 4;
		}
	});

	public BlockOldLog() {
		this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.OAK).withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	public MapColor getMapColor(IBlockState state) {
		BlockPlanks.EnumType blockplanks$enumtype = (BlockPlanks.EnumType) state.getValue(VARIANT);

		switch ((BlockLog.EnumAxis) state.getValue(LOG_AXIS)) {
			case X:
			case Z:
			case NONE:
			default:
				switch (blockplanks$enumtype) {
					case OAK:
					default:
						return BlockPlanks.EnumType.SPRUCE.getMapColor();

					case SPRUCE:
						return BlockPlanks.EnumType.DARK_OAK.getMapColor();

					case BIRCH:
						return MapColor.quartzColor;

					case JUNGLE:
						return BlockPlanks.EnumType.SPRUCE.getMapColor();
				}

			case Y:
				return blockplanks$enumtype.getMapColor();
		}
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.OAK.getMetadata()));
		list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
		list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
		list.add(new ItemStack(itemIn, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, BlockPlanks.EnumType.byMetadata((meta & 3) % 4));

		switch (meta & 12) {
			case 0:
				iblockstate = iblockstate.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
				break;

			case 4:
				iblockstate = iblockstate.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
				break;

			case 8:
				iblockstate = iblockstate.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
				break;

			default:
				iblockstate = iblockstate.withProperty(LOG_AXIS, BlockLog.EnumAxis.NONE);
		}

		return iblockstate;
	}

	@SuppressWarnings ("incomplete-switch")

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		int i = 0;
		i = i | ((BlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();

		switch ((BlockLog.EnumAxis) state.getValue(LOG_AXIS)) {
			case X:
				i |= 4;
				break;

			case Z:
				i |= 8;
				break;

			case NONE:
				i |= 12;
		}

		return i;
	}

	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] {VARIANT, LOG_AXIS});
	}

	protected ItemStack createStackedBlock(IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(this), 1, ((BlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata());
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	public int damageDropped(IBlockState state) {
		return ((BlockPlanks.EnumType) state.getValue(VARIANT)).getMetadata();
	}

}
