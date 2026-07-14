/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.sweetberry.more_than_a_foxbox.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.sweetberry.more_than_a_foxbox.MoreThanAFoxbox;
import dev.sweetberry.more_than_a_foxbox.client.PlushieModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class GuiPlushieRenderer extends PictureInPictureRenderer<GuiPlushieRenderState> {
	private final BlockModelRenderState blockModelState = new BlockModelRenderState();

	public GuiPlushieRenderer() {
	}

	@Override
	public @NonNull Class<GuiPlushieRenderState> getRenderStateClass() {
		return GuiPlushieRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiPlushieRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
		gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);

		poseStack.translate(-0.525F, 0.0, 0.0);
		poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
		poseStack.rotateAround(Axis.YP.rotation(renderState.yRot()), 0.5F, 0.0F, -0.5F);
		poseStack.scale(1.0F, -1.0F, -1.0F);

		Identifier modelId = getModelId(renderState);
		PlushieModel.updatePlushieModelState(blockModelState, modelId);

		blockModelState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
	}

	private Identifier getModelId(final GuiPlushieRenderState renderState) {
		Identifier modelId = Identifier.fromNamespaceAndPath(MoreThanAFoxbox.ID, MoreThanAFoxbox.ID + "/placeholder");
		if (renderState.variant().isBound()) {
			modelId = renderState.variant().value().poses().getModelFromPose(renderState.plushiePose());
		}
		return modelId;
	}

	@Override
	protected float getTranslateY(final int height, final int guiScale) {
		return height / 1.8F;
	}

	@Override
	protected @NonNull String getTextureLabel() {
		return "morethanafoxbox:plushie";
	}
}
