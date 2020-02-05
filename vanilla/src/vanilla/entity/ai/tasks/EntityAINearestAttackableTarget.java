package vanilla.entity.ai.tasks;

import net.minecraft.entity.Entity;
import vanilla.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.Player;
import net.minecraft.util.EntitySelectors;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class EntityAINearestAttackableTarget<T extends EntityLivingBase> extends EntityAITarget {

	protected final Class<T> targetClass;
	private final int targetChance;

	/**
	 * Instance of EntityAINearestAttackableTargetSorter.
	 */
	protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
	protected Predicate<? super T> targetEntitySelector;
	protected EntityLivingBase targetEntity;

	public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight) {
		this(creature, classTarget, checkSight, false);
	}

	public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
		this(creature, classTarget, 10, checkSight, onlyNearby, (Predicate<? super T>) null);
	}

	public EntityAINearestAttackableTarget(EntityCreature creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, final Predicate<? super T> targetSelector) {
		super(creature, checkSight, onlyNearby);
		this.targetClass = classTarget;
		this.targetChance = chance;
		this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(creature);
		this.setMutexBits(1);
		this.targetEntitySelector = new Predicate<T>() {
			public boolean test(T p_apply_1_) {
				if (targetSelector != null && !targetSelector.test(p_apply_1_)) {
					return false;
				}
				if (p_apply_1_ instanceof Player) {
					double d0 = EntityAINearestAttackableTarget.this.getTargetDistance();

					if (p_apply_1_.isSneaking()) {
						d0 *= 0.800000011920929D;
					}

					if (p_apply_1_.isInvisible()) {
						float f = ((Player) p_apply_1_).inventory.getArmorVisibility();

						if (f < 0.1F) {
							f = 0.1F;
						}

						d0 *= (double) (0.7F * f);
					}

					if ((double) p_apply_1_.getDistanceToEntity(EntityAINearestAttackableTarget.this.taskOwner) > d0) {
						return false;
					}
				}

				return EntityAINearestAttackableTarget.this.isSuitableTarget(p_apply_1_, false);
			}
		};
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
			return false;
		}
		double d0 = this.getTargetDistance();
		List<T> list = this.taskOwner.worldObj.getEntitiesWithinAABB(this.targetClass, this.taskOwner.getEntityBoundingBox().expand(d0, 4.0D, d0),
				(entity) -> targetEntitySelector.test(entity) && EntitySelectors.NOT_SPECTATING.test(entity));
		list.sort(this.theNearestAttackableTargetSorter);

		if (list.isEmpty()) {
			return false;
		}
		this.targetEntity = (EntityLivingBase) list.get(0);
		return true;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.targetEntity);
		super.startExecuting();
	}

	public static class Sorter implements Comparator<Entity> {

		private final Entity theEntity;

		public Sorter(Entity theEntityIn) {
			this.theEntity = theEntityIn;
		}

		public int compare(Entity p_compare_1_, Entity p_compare_2_) {
			double d0 = this.theEntity.getDistanceSqToEntity(p_compare_1_);
			double d1 = this.theEntity.getDistanceSqToEntity(p_compare_2_);
			return d0 < d1 ? -1 : d0 > d1 ? 1 : 0;
		}

	}

}
