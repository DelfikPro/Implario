package vanilla.client.renderer.entity.vanilla;

import vanilla.client.game.model.ModelSkeleton;
import net.minecraft.client.renderer.G;
import vanilla.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import vanilla.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;

public class RenderSkeleton extends RenderBiped<EntitySkeleton> {

	private static final ResourceLocation skeletonTextures = new ResourceLocation("textures/entity/skeleton/skeleton.png");
	private static final ResourceLocation witherSkeletonTextures = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

	public RenderSkeleton(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelSkeleton(), 0.5F);
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerBipedArmor(this) {
			protected void initArmor() {
				this.field_177189_c = new ModelSkeleton(0.5F, true);
				this.field_177186_d = new ModelSkeleton(1.0F, true);
			}
		});
	}

	/**
	 * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
	 * entityLiving, partialTickTime
	 */
	protected void preRenderCallback(EntitySkeleton entitylivingbaseIn, float partialTickTime) {
		if (entitylivingbaseIn.getSkeletonType() == 1) {
			G.scale(1.2F, 1.2F, 1.2F);
		}
	}

	public void transformHeldFull3DItemLayer() {
		G.translate(0.09375F, 0.1875F, 0.0F);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(EntitySkeleton entity) {
		return entity.getSkeletonType() == 1 ? witherSkeletonTextures : skeletonTextures;
	}

}
