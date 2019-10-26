package net.minecraft.resources.event.events.player;

import lombok.*;
import net.minecraft.entity.player.Player;

@Getter
public class PlayerMoveEvent extends APlayerСancelableEvent {
	private final double srcX, srcY, srcZ;
	private final double dstX, dstY, dstZ;
	@Setter
	private boolean abltCancel = false;

	public PlayerMoveEvent(Player player, double srcX, double srcY, double srcZ, double dstX, double dstY, double dstZ){
		super(player);
		this.srcX = srcX;
		this.srcY = srcY;
		this.srcZ = srcZ;
		this.dstX = dstX;
		this.dstY = dstY;
		this.dstZ = dstZ;
	}

	@Override
	public boolean isCanceled() {
		return super.isCanceled() || abltCancel;
	}
}
