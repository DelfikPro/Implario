package vanilla.client.renderer.entity.layers;

import net.minecraft.client.renderer.G;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import vanilla.client.renderer.entity.vanilla.RenderSpider;
import net.minecraft.entity.EntityLivingBase;
import vanilla.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import optifine.Config;
import shadersmod.client.Shaders;

public class LayerSpiderEyes implements LayerRenderer {

	private static final ResourceLocation SPIDER_EYES = new ResourceLocation("textures/entity/spider_eyes.png");
	private final RenderSpider spiderRenderer;


	public LayerSpiderEyes(RenderSpider spiderRendererIn) {
		this.spiderRenderer = spiderRendererIn;
	}

	public void doRenderLayer(EntitySpider entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		this.spiderRenderer.bindTexture(SPIDER_EYES);
		G.enableBlend();
		G.disableAlpha();
		G.blendFunc(1, 1);

		if (entitylivingbaseIn.isInvisible()) {
			G.depthMask(false);
		} else {
			G.depthMask(true);
		}

		char c0 = 61680;
		int i = c0 % 65536;
		int j = c0 / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) i / 1.0F, (float) j / 1.0F);
		G.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (Config.isShaders()) {
			Shaders.beginSpiderEyes();
		}

		this.spiderRenderer.getMainModel().render(entitylivingbaseIn, p_177141_2_, p_177141_3_, p_177141_5_, p_177141_6_, p_177141_7_, scale);
		int k = entitylivingbaseIn.getBrightnessForRender(partialTicks);
		i = k % 65536;
		j = k / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) i / 1.0F, (float) j / 1.0F);
		this.spiderRenderer.func_177105_a(entitylivingbaseIn, partialTicks);
		G.disableBlend();
		G.enableAlpha();
	}

	public boolean shouldCombineTextures() {
		return false;
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		this.doRenderLayer((EntitySpider) entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);
	}

}
