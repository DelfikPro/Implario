package optifine;

import net.minecraft.Utils;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.Settings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CustomSky {

	private static CustomSkyLayer[][] worldSkyLayers = null;

	public static void reset() {
		worldSkyLayers = null;
	}

	public static void update() {
		reset();

		if (Config.isCustomSky()) {
			worldSkyLayers = readCustomSkies();
		}
	}

	private static CustomSkyLayer[][] readCustomSkies() {
		CustomSkyLayer[][] acustomskylayer = new CustomSkyLayer[10][0];
		String s = "mcpatcher/sky/world";
		int i = -1;

		for (int j = 0; j < acustomskylayer.length; ++j) {
			String s1 = s + j + "/sky";
			List list = new ArrayList();

			for (int k = 1; k < 1000; ++k) {
				String s2 = s1 + k + ".properties";

				try {
					ResourceLocation resourcelocation = new ResourceLocation(s2);
					InputStream inputstream = Config.getResourceStream(resourcelocation);

					if (inputstream == null) {
						break;
					}

					Properties properties = new Properties();
					properties.load(inputstream);
					inputstream.close();
					Config.dbg("CustomSky properties: " + s2);
					String s3 = s1 + k + ".png";
					CustomSkyLayer customskylayer = new CustomSkyLayer(properties, s3);

					if (customskylayer.isValid(s2)) {
						ResourceLocation resourcelocation1 = new ResourceLocation(customskylayer.source);
						ITextureObject itextureobject = TextureUtils.getTexture(resourcelocation1);

						if (itextureobject == null) {
							Config.log("CustomSky: Texture not found: " + resourcelocation1);
						} else {
							customskylayer.textureId = itextureobject.getGlTextureId();
							list.add(customskylayer);
							inputstream.close();
						}
					}
				} catch (FileNotFoundException var15) {
					break;
				} catch (IOException ioexception) {
					ioexception.printStackTrace();
				}
			}

			if (list.size() > 0) {
				CustomSkyLayer[] acustomskylayer2 = (CustomSkyLayer[]) list.toArray(Utils.CUSTOMSKYLAYER);
				acustomskylayer[j] = acustomskylayer2;
				i = j;
			}
		}

		if (i < 0) {
			return null;
		}
		int l = i + 1;
		CustomSkyLayer[][] acustomskylayer1 = new CustomSkyLayer[l][0];

		System.arraycopy(acustomskylayer, 0, acustomskylayer1, 0, acustomskylayer1.length);

		return acustomskylayer1;
	}

	public static void renderSky(World p_renderSky_0_, TextureManager p_renderSky_1_, float p_renderSky_2_, float p_renderSky_3_) {
		if (worldSkyLayers != null) {
			if (Settings.RENDER_DISTANCE.f() >= 8) {
				int i = p_renderSky_0_.provider.getDimensionId();

				if (i >= 0 && i < worldSkyLayers.length) {
					CustomSkyLayer[] acustomskylayer = worldSkyLayers[i];

					if (acustomskylayer != null) {
						long j = p_renderSky_0_.getWorldTime();
						int k = (int) (j % 24000L);

						for (CustomSkyLayer customskylayer : acustomskylayer) {
							if (customskylayer.isActive(p_renderSky_0_, k)) {
								customskylayer.render(k, p_renderSky_2_, p_renderSky_3_);
							}
						}

						Blender.clearBlend(p_renderSky_3_);
					}
				}
			}
		}
	}

	public static boolean hasSkyLayers(World p_hasSkyLayers_0_) {
		if (worldSkyLayers == null) {
			return false;
		}
		if (Settings.RENDER_DISTANCE.f() < 8) {
			return false;
		}
		int i = p_hasSkyLayers_0_.provider.getDimensionId();

		if (i >= 0 && i < worldSkyLayers.length) {
			CustomSkyLayer[] acustomskylayer = worldSkyLayers[i];
			return acustomskylayer != null && acustomskylayer.length > 0;
		}
		return false;
	}

}