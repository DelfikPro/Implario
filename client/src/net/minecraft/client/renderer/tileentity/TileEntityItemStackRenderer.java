package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.G;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;

public class TileEntityItemStackRenderer {

	public static TileEntityItemStackRenderer instance = new TileEntityItemStackRenderer();
	private TileEntityChest field_147717_b = new TileEntityChest(0);
	private TileEntityChest field_147718_c = new TileEntityChest(1);
	private TileEntityEnderChest enderChest = new TileEntityEnderChest();
	private TileEntityBanner banner = new TileEntityBanner();
	private TileEntitySkull skull = new TileEntitySkull();

	public void renderByItem(ItemStack itemStackIn) {
		if (itemStackIn.getItem() == Items.banner) {
			this.banner.setItemValues(itemStackIn);
			TileEntityRendererDispatcher.instance.renderTileEntityAt(this.banner, 0.0D, 0.0D, 0.0D, 0.0F);
		} else if (itemStackIn.getItem() == Items.skull) {
			GameProfile gameprofile = null;

			if (itemStackIn.hasTagCompound()) {
				NBTTagCompound nbttagcompound = itemStackIn.getTagCompound();

				if (nbttagcompound.hasKey("SkullOwner", 10)) {
					gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
				} else if (nbttagcompound.hasKey("SkullOwner", 8) && nbttagcompound.getString("SkullOwner").length() > 0) {
					gameprofile = new GameProfile((UUID) null, nbttagcompound.getString("SkullOwner"));
					gameprofile = TileEntitySkull.updateGameprofile(gameprofile);
					nbttagcompound.removeTag("SkullOwner");
					nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
				}
			}

			if (TileEntitySkullRenderer.instance != null) {
				G.pushMatrix();
				G.translate(-0.5F, 0.0F, -0.5F);
				G.scale(2.0F, 2.0F, 2.0F);
				G.disableCull();
				TileEntitySkullRenderer.instance.renderSkull(0.0F, 0.0F, 0.0F, EnumFacing.UP, 0.0F, itemStackIn.getMetadata(), gameprofile, -1);
				G.enableCull();
				G.popMatrix();
			}
		} else {
			Block block = Block.getBlockFromItem(itemStackIn.getItem());

			if (block == Blocks.ender_chest) {
				TileEntityRendererDispatcher.instance.renderTileEntityAt(this.enderChest, 0.0D, 0.0D, 0.0D, 0.0F);
			} else if (block == Blocks.trapped_chest) {
				TileEntityRendererDispatcher.instance.renderTileEntityAt(this.field_147718_c, 0.0D, 0.0D, 0.0D, 0.0F);
			} else {
				TileEntityRendererDispatcher.instance.renderTileEntityAt(this.field_147717_b, 0.0D, 0.0D, 0.0D, 0.0F);
			}
		}
	}

}
