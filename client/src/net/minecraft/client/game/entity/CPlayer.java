package net.minecraft.client.game.entity;

import net.minecraft.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.block.GuiCommandBlock;
import net.minecraft.client.gui.block.GuiEnchantment;
import net.minecraft.client.gui.block.GuiHopper;
import net.minecraft.client.gui.block.GuiRepair;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.network.protocol.minecraft_47.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityControllable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.PlayerGuiBridge;
import net.minecraft.init.Items;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.potion.Potion;
import net.minecraft.network.protocol.minecraft_47.play.client.*;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

import static net.minecraft.network.protocol.minecraft_47.play.client.C0BPacketEntityAction.Action.*;

public class CPlayer extends AbstractClientPlayer {

	public final NetHandlerPlayClient sendQueue;
	private final StatFileWriter statWriter;

	/**
	 * The last X position which was transmitted to the server, used to determine when the X position changes and needs
	 * to be re-trasmitted
	 */
	private double lastReportedPosX;

	/**
	 * The last Y position which was transmitted to the server, used to determine when the Y position changes and needs
	 * to be re-transmitted
	 */
	private double lastReportedPosY;

	/**
	 * The last Z position which was transmitted to the server, used to determine when the Z position changes and needs
	 * to be re-transmitted
	 */
	private double lastReportedPosZ;

	/**
	 * The last yaw value which was transmitted to the server, used to determine when the yaw changes and needs to be
	 * re-transmitted
	 */
	private float lastReportedYaw;

	/**
	 * The last pitch value which was transmitted to the server, used to determine when the pitch changes and needs to
	 * be re-transmitted
	 */
	private float lastReportedPitch;

	/**
	 * the last sneaking state sent to the server
	 */
	private boolean serverSneakState;

	/**
	 * the last sprinting state sent to the server
	 */
	private boolean serverSprintState;

	/**
	 * Reset to 0 every time position is sent to the server, used to send periodic updates every 20 ticks even when the
	 * player is not moving.
	 */
	private int positionUpdateTicks;
	private boolean hasValidHealth;
	private String clientBrand;
	public MovementInput movementInput;
	protected Minecraft mc;

	/**
	 * Used to tell if the player pressed forward twice. If this is at 0 and it's pressed (And they are allowed to
	 * sprint, aka enough food on the ground etc) it sets this to 7. If it's pressed and it's greater than 0 enable
	 * sprinting.
	 */
	protected int sprintToggleTimer;

	/**
	 * Ticks left before sprinting is disabled.
	 */
	public int sprintingTicksLeft;
	public float renderArmYaw;
	public float renderArmPitch;
	public float prevRenderArmYaw;
	public float prevRenderArmPitch;
	private int horseJumpPowerCounter;
	private float horseJumpPower;

	/**
	 * The amount of time an entity has been in a Portal
	 */
	public float timeInPortal;

	/**
	 * The amount of time an entity has been in a Portal the previous tick
	 */
	public float prevTimeInPortal;

	public CPlayer(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatFileWriter statFile) {
		super(worldIn, netHandler.getGameProfile());
		this.sendQueue = netHandler;
		this.statWriter = statFile;
		this.mc = mcIn;
		this.dimension = 0;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	/**
	 * Heal living entity (param: amount of half-hearts)
	 */
	public void heal(float healAmount) {
	}

	/**
	 * Called when a player mounts an entity. e.g. mounts a pig, mounts a boat.
	 */
	public void mountEntity(Entity entityIn) {
		super.mountEntity(entityIn);

		if (entityIn instanceof EntityMinecart) {
			this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart) entityIn));
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {
		if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
			super.onUpdate();

			if (this.isRiding()) {
				this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
				this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
			} else {
				this.onUpdateWalkingPlayer();
			}
		}
	}

	/**
	 * called every tick when the player is on foot. Performs all the things that normally happen during movement.
	 */
	public void onUpdateWalkingPlayer() {
		boolean sprinting = this.isSprinting();

		if (sprinting != this.serverSprintState) {
			this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, sprinting ? START_SPRINTING : STOP_SPRINTING));
			this.serverSprintState = sprinting;
		}

		boolean sneaking = this.isSneaking();

		if (sneaking != this.serverSneakState) {
			this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, sneaking ? START_SNEAKING : STOP_SNEAKING));

			this.serverSneakState = sneaking;
		}

		if (!this.isCurrentViewEntity()) return;


		double dx = this.posX - this.lastReportedPosX;
		double dy = this.getEntityBoundingBox().minY - this.lastReportedPosY;
		double dz = this.posZ - this.lastReportedPosZ;
		double dyaw = (double) (this.rotationYaw - this.lastReportedYaw);
		double dpitch = (double) (this.rotationPitch - this.lastReportedPitch);

		boolean sendPosition = dx * dx + dy * dy + dz * dz > 0.0009 || this.positionUpdateTicks >= 20;
		boolean sendRotation = dyaw != 0.0D || dpitch != 0.0D;

		if (this.ridingEntity == null) {
			if (sendPosition && sendRotation) {
				this.sendQueue.addToSendQueue(
						new C03PacketPlayer.C06PacketPlayerPosLook(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround));
			} else if (sendPosition) {
				this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.onGround));
			} else if (sendRotation) {
				this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
			} else {
				this.sendQueue.addToSendQueue(new C03PacketPlayer(this.onGround));
			}
		} else {
			this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, this.rotationYaw, this.rotationPitch, this.onGround));
			sendPosition = false;
		}

		++this.positionUpdateTicks;

		if (sendPosition) {
			this.lastReportedPosX = this.posX;
			this.lastReportedPosY = this.getEntityBoundingBox().minY;
			this.lastReportedPosZ = this.posZ;
			this.positionUpdateTicks = 0;
		}

		if (sendRotation) {
			this.lastReportedYaw = this.rotationYaw;
			this.lastReportedPitch = this.rotationPitch;
		}
	}

	/**
	 * Called when player presses the drop item key
	 */
	public void dropOneItem(boolean dropAll) {
		C07PacketPlayerDigging.Action c07packetplayerdigging$action = dropAll ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
		this.sendQueue.addToSendQueue(new C07PacketPlayerDigging(c07packetplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
	}

	/**
	 * Joins the passed in entity item with the world. Args: entityItem
	 */
	protected void joinEntityItemWithWorld(EntityItem itemIn) {
	}

	/**
	 * Sends a chat message from the player. Args: chatMessage
	 */
	public void sendChatMessage(String message) {
		if(message.startsWith("/")) {
			if(CommandHandler.executeClientSide(this, message) == 0)return;
		}
		if (message.startsWith("=")) Utils.processCommand(message.substring(1));
		else this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
	}

	/**
	 * Swings the item the player is holding.
	 */
	public void swingItem() {
		super.swingItem();
		this.sendQueue.addToSendQueue(new C0APacketAnimation());
	}

	public void respawnPlayer() {
		this.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
	}

	/**
	 * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
	 * second with the reduced value. Args: damageAmount
	 */
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		if (!this.isEntityInvulnerable(damageSrc)) {
			this.setHealth(this.getHealth() - damageAmount);
		}
	}

	@Override
	public <T> void openGui(Class<T> type, T element) {
		if (type == TileEntitySign.class) mc.displayGuiScreen(new GuiEditSign((TileEntitySign) element));
		else if (type == CommandBlockLogic.class) mc.displayGuiScreen(new GuiCommandBlock((CommandBlockLogic) element));
		else if (type == ItemStack.class) {
			ItemStack stack = (ItemStack) element;

			if (stack.getItem() == Items.writable_book) {
				this.mc.displayGuiScreen(new GuiScreenBook(this, stack, true));
			}
		} else if (type == Inventory.class) {
			Inventory container = (Inventory) element;
			String s = container instanceof IInteractionObject ? ((IInteractionObject) container).getGuiID() : "minecraft:container";

			if ("minecraft:chest".equals(s)) {
				this.mc.displayGuiScreen(new GuiChest(this.inventory, container));
			} else if ("minecraft:hopper".equals(s)) {
				this.mc.displayGuiScreen(new GuiHopper(this.inventory, container));
			} else if ("minecraft:furnace".equals(s)) {
				this.mc.displayGuiScreen(new GuiFurnace(this.inventory, container));
			} else if ("minecraft:brewing_stand".equals(s)) {
				this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, container));
			} else if ("minecraft:beacon".equals(s)) {
				this.mc.displayGuiScreen(new GuiBeacon(this.inventory, container));
			} else if ("minecraft:dispenser".equals(s) || "minecraft:dropper".equals(s)) {
				this.mc.displayGuiScreen(new GuiDispenser(this.inventory, container));
			} else {
				this.mc.displayGuiScreen(new GuiChest(this.inventory, container));
			}
		} else if (type == IInteractionObject.class) {
			IInteractionObject guiOwner = (IInteractionObject) element;
			String s = guiOwner.getGuiID();

			if ("minecraft:crafting_table".equals(s)) {
				this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj));
			} else if ("minecraft:enchanting_table".equals(s)) {
				this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, guiOwner));
			} else if ("minecraft:anvil".equals(s)) {
				this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.worldObj));
			}
		} else PlayerGuiBridge.open(this, type, element, false);
	}

	/**
	 * set current crafting inventory back to the 2x2 square
	 */
	public void closeScreen() {
		this.sendQueue.addToSendQueue(new C0DPacketCloseWindow(this.openContainer.windowId));
		this.closeScreenAndDropStack();
	}

	public void closeScreenAndDropStack() {
		this.inventory.setItemStack(null);
		super.closeScreen();
		this.mc.displayGuiScreen(null);
	}

	/**
	 * Updates health locally.
	 */
	public void setPlayerSPHealth(float health) {
		if (this.hasValidHealth) {
			float f = this.getHealth() - health;

			if (f <= 0.0F) {
				this.setHealth(health);

				if (f < 0.0F) {
					this.hurtResistantTime = this.maxHurtResistantTime / 2;
				}
			} else {
				this.lastDamage = f;
				this.setHealth(this.getHealth());
				this.hurtResistantTime = this.maxHurtResistantTime;
				this.damageEntity(DamageSource.generic, f);
				this.hurtTime = this.maxHurtTime = 10;
			}
		} else {
			this.setHealth(health);
			this.hasValidHealth = true;
		}
	}

	/**
	 * Adds a value to a statistic field.
	 */
	public void addStat(StatBase stat, int amount) {
		if (stat != null && stat.independent) super.addStat(stat, amount);
	}

	/**
	 * Sends the player's abilities to the server (if there is one).
	 */
	public void sendPlayerAbilities() {
		this.sendQueue.addToSendQueue(new C13PacketPlayerAbilities(this.capabilities));
	}

	/**
	 * returns true if this is an EntityPlayerSP, or the logged in player.
	 */
	public boolean isUser() {
		return true;
	}

	protected void sendHorseJump() {
		this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.RIDING_JUMP, (int) (this.getHorseJumpPower() * 100.0F)));
	}

	public void sendHorseInventory() {
		this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.OPEN_INVENTORY));
	}

	public void setClientBrand(String brand) {
		this.clientBrand = brand;
	}

	public String getClientBrand() {
		return this.clientBrand;
	}

	public StatFileWriter getStatFileWriter() {
		return this.statWriter;
	}

	public void addChatComponentMessage(IChatComponent chatComponent) {
		this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
	}

	protected boolean pushOutOfBlocks(double x, double y, double z) {
		if (this.noClip) {
			return false;
		}
		BlockPos blockpos = new BlockPos(x, y, z);
		double d0 = x - (double) blockpos.getX();
		double d1 = z - (double) blockpos.getZ();

		if (!this.isOpenBlockSpace(blockpos)) {
			int i = -1;
			double d2 = 9999.0D;

			if (this.isOpenBlockSpace(blockpos.west()) && d0 < d2) {
				d2 = d0;
				i = 0;
			}

			if (this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
				d2 = 1.0D - d0;
				i = 1;
			}

			if (this.isOpenBlockSpace(blockpos.north()) && d1 < d2) {
				d2 = d1;
				i = 4;
			}

			if (this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
				i = 5;
			}

			float f = 0.1F;

			if (i == 0) {
				this.motionX = (double) -f;
			}

			if (i == 1) {
				this.motionX = (double) f;
			}

			if (i == 4) {
				this.motionZ = (double) -f;
			}

			if (i == 5) {
				this.motionZ = (double) f;
			}
		}

		return false;
	}

	/**
	 * Returns true if the block at the given BlockPos and the block above it are NOT full cubes.
	 */
	private boolean isOpenBlockSpace(BlockPos pos) {
		return !this.worldObj.getBlockState(pos).getBlock().isNormalCube() && !this.worldObj.getBlockState(pos.up()).getBlock().isNormalCube();
	}

	/**
	 * Set sprinting switch for Entity.
	 */
	public void setSprinting(boolean sprinting) {
		super.setSprinting(sprinting);
		this.sprintingTicksLeft = sprinting ? 600 : 0;
	}

	/**
	 * Sets the current XP, total XP, and level number.
	 */
	public void setXPStats(float currentXP, int maxXP, int level) {
		this.experience = currentXP;
		this.experienceTotal = maxXP;
		this.experienceLevel = level;
	}

	/**
	 * Send a chat message to the CommandSender
	 */
	public void sendMessage(IChatComponent component) {
		this.mc.ingameGUI.getChatGUI().printChatMessage(component);
	}

	/**
	 * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
	 */
	public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
		return permLevel <= 0;
	}

	/**
	 * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
	 * the coordinates 0, 0, 0
	 */
	public BlockPos getPosition() {
		return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
	}

	public void playSound(String name, float volume, float pitch) {
		this.worldObj.playSound(this.posX, this.posY, this.posZ, name, volume, pitch, false);
	}

	/**
	 * Returns whether the entity is in a server world
	 */
	public boolean isServerWorld() {
		return true;
	}

	public boolean isRidingHorse() {
		return this.ridingEntity instanceof EntityControllable && ((EntityControllable) this.ridingEntity).isControllable();
	}

	public float getHorseJumpPower() {
		return this.horseJumpPower;
	}

	/**
	 * Called when the player performs a critical hit on the Entity. Args: entity that was hit critically
	 */
	public void onCriticalHit(Entity entityHit) {
		this.mc.effectRenderer.emitParticleAtEntity(entityHit, ParticleType.CRIT);
	}

	public void onEnchantmentCritical(Entity entityHit) {
		this.mc.effectRenderer.emitParticleAtEntity(entityHit, ParticleType.CRIT_MAGIC);
	}

	/**
	 * Returns if this entity is sneaking.
	 */
	public boolean isSneaking() {
		return this.movementInput != null && this.movementInput.sneak && !this.sleeping;
	}

	public void updateEntityActionState() {
		super.updateEntityActionState();

		if (this.isCurrentViewEntity()) {
			this.moveStrafing = this.movementInput.moveStrafe;
			this.moveForward = this.movementInput.moveForward;
			this.isJumping = this.movementInput.jump;
			this.prevRenderArmYaw = this.renderArmYaw;
			this.prevRenderArmPitch = this.renderArmPitch;
			this.renderArmPitch = (float) ((double) this.renderArmPitch + (double) (this.rotationPitch - this.renderArmPitch) * 0.5D);
			this.renderArmYaw = (float) ((double) this.renderArmYaw + (double) (this.rotationYaw - this.renderArmYaw) * 0.5D);
		}
	}

	protected boolean isCurrentViewEntity() {
		return this.mc.getRenderViewEntity() == this;
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	public void onLivingUpdate() {
		if (this.sprintingTicksLeft > 0) {
			--this.sprintingTicksLeft;

			if (this.sprintingTicksLeft == 0) {
				this.setSprinting(false);
			}
		}

		if (this.sprintToggleTimer > 0) {
			--this.sprintToggleTimer;
		}

		this.prevTimeInPortal = this.timeInPortal;

		if (this.inPortal) {
			if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
				this.mc.displayGuiScreen(null);
			}

			if (this.timeInPortal == 0.0F) {
				this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
			}

			this.timeInPortal += 0.0125F;

			if (this.timeInPortal >= 1.0F) {
				this.timeInPortal = 1.0F;
			}

			this.inPortal = false;
		} else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60) {
			this.timeInPortal += 0.006666667F;

			if (this.timeInPortal > 1.0F) {
				this.timeInPortal = 1.0F;
			}
		} else {
			if (this.timeInPortal > 0.0F) {
				this.timeInPortal -= 0.05F;
			}

			if (this.timeInPortal < 0.0F) {
				this.timeInPortal = 0.0F;
			}
		}

		if (this.timeUntilPortal > 0) {
			--this.timeUntilPortal;
		}

		boolean jump = this.movementInput.jump;
		boolean sneak = this.movementInput.sneak;
		float f = 0.8F;
		boolean fastForward = this.movementInput.moveForward >= f;
		this.movementInput.updatePlayerMoveState();

		if (this.isUsingItem() && !this.isRiding()) {
			this.movementInput.moveStrafe *= 0.2F;
			this.movementInput.moveForward *= 0.2F;
			this.sprintToggleTimer = 0;
		}

		this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
		this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
		this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
		this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
		boolean canRun = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

		if (this.onGround && !sneak && !fastForward && this.movementInput.moveForward >= f && !this.isSprinting() && canRun && !this.isUsingItem() && !this.isPotionActive(Potion.blindness)) {
			if (this.sprintToggleTimer <= 0 && !KeyBinding.SPRINT.isKeyDown()) {
				this.sprintToggleTimer = 7;
			} else {
				this.setSprinting(true);
			}
		}

		if (!this.isSprinting() && this.movementInput.moveForward >= f && canRun && !this.isUsingItem() && !this.isPotionActive(Potion.blindness) && KeyBinding.SPRINT.isKeyDown()) {
			this.setSprinting(true);
		}

		if (this.isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !canRun)) {
			this.setSprinting(false);
		}

		if (this.capabilities.allowFlying) {
			if (this.mc.playerController.isSpectatorMode()) {
				if (!this.capabilities.isFlying) {
					this.capabilities.isFlying = true;
					this.sendPlayerAbilities();
				}
			} else if (!jump && this.movementInput.jump) {
				if (this.flyToggleTimer == 0) {
					this.flyToggleTimer = 7;
				} else {
					this.capabilities.isFlying = !this.capabilities.isFlying;
					this.sendPlayerAbilities();
					this.flyToggleTimer = 0;
				}
			}
		}

		if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
			if (this.movementInput.sneak) {
				this.motionY -= (double) (this.capabilities.getFlySpeed() * 3.0F);
			}

			if (this.movementInput.jump) {
				this.motionY += (double) (this.capabilities.getFlySpeed() * 3.0F);
			}
		}

		if (this.isRidingHorse()) {
			if (this.horseJumpPowerCounter < 0) {
				++this.horseJumpPowerCounter;

				if (this.horseJumpPowerCounter == 0) {
					this.horseJumpPower = 0.0F;
				}
			}

			if (jump && !this.movementInput.jump) {
				this.horseJumpPowerCounter = -10;
				this.sendHorseJump();
			} else if (!jump && this.movementInput.jump) {
				this.horseJumpPowerCounter = 0;
				this.horseJumpPower = 0.0F;
			} else if (jump) {
				++this.horseJumpPowerCounter;

				if (this.horseJumpPowerCounter < 10) {
					this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
				} else {
					this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
				}
			}
		} else {
			this.horseJumpPower = 0.0F;
		}

		super.onLivingUpdate();

		if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
			this.capabilities.isFlying = false;
			this.sendPlayerAbilities();
		}
	}

}
