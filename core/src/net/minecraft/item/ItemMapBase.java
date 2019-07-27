package net.minecraft.item;

import net.minecraft.entity.player.Player;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

public class ItemMapBase extends Item {

	/**
	 * false for all Items except sub-classes of ItemMapBase
	 */
	public boolean isMap() {
		return true;
	}

	public Packet createMapDataPacket(ItemStack stack, World worldIn, Player player) {
		return null;
	}

}
