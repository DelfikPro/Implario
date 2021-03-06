package vanilla.entity.ai.tasks.village;

import vanilla.entity.EntityCreature;
import vanilla.entity.ai.tasks.EntityAIBase;
import vanilla.entity.ai.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import vanilla.world.gen.feature.village.Village;
import vanilla.world.gen.feature.village.VillageCollection;
import vanilla.world.gen.feature.village.VillageDoorInfo;

public class EntityAIRestrictOpenDoor extends EntityAIBase {

	private EntityCreature entityObj;
	private VillageDoorInfo frontDoor;

	public EntityAIRestrictOpenDoor(EntityCreature creatureIn) {
		this.entityObj = creatureIn;

		if (!(creatureIn.getNavigator() instanceof PathNavigateGround)) {
			throw new IllegalArgumentException("Unsupported mob type for RestrictOpenDoorGoal");
		}
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (this.entityObj.worldObj.isDaytime()) {
			return false;
		}
		BlockPos blockpos = new BlockPos(this.entityObj);
		Village village = VillageCollection.get(entityObj.worldObj).getNearestVillage(blockpos, 16);

		if (village == null) {
			return false;
		}
		this.frontDoor = village.getNearestDoor(blockpos);
		return this.frontDoor == null ? false : (double) this.frontDoor.getDistanceToInsideBlockSq(blockpos) < 2.25D;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean continueExecuting() {
		return this.entityObj.worldObj.isDaytime() ? false : !this.frontDoor.getIsDetachedFromVillageFlag() && this.frontDoor.func_179850_c(new BlockPos(this.entityObj));
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		((PathNavigateGround) this.entityObj.getNavigator()).setBreakDoors(false);
		((PathNavigateGround) this.entityObj.getNavigator()).setEnterDoors(false);
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		((PathNavigateGround) this.entityObj.getNavigator()).setBreakDoors(true);
		((PathNavigateGround) this.entityObj.getNavigator()).setEnterDoors(true);
		this.frontDoor = null;
	}

	/**
	 * Updates the task
	 */
	public void updateTask() {
		this.frontDoor.incrementDoorOpeningRestrictionCounter();
	}

}
