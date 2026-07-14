/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.sweetberry.more_than_a_foxbox.client.block.entity.render;

import static dev.sweetberry.more_than_a_foxbox.block.entity.PlushieHoldingBlockEntity.SCALE_SLOWNESS;
import static dev.sweetberry.more_than_a_foxbox.block.entity.PlushieHoldingBlockEntity.STRETCH_TIME;
import static dev.sweetberry.more_than_a_foxbox.block.entity.PlushieHoldingBlockEntity.SQUISH_TIME;

import java.util.*;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.sweetberry.more_than_a_foxbox.MoreThanAFoxbox;
import dev.sweetberry.more_than_a_foxbox.block.BoxBlock;
import dev.sweetberry.more_than_a_foxbox.block.entity.BoxBlockEntity;
import dev.sweetberry.more_than_a_foxbox.block.entity.PlushieHoldingBlockEntity;
import dev.sweetberry.more_than_a_foxbox.block.property.MtfbBlockProperties;
import dev.sweetberry.more_than_a_foxbox.client.MoreThanAFoxboxClient;
import dev.sweetberry.more_than_a_foxbox.client.PlushieModel;
import dev.sweetberry.more_than_a_foxbox.data.PlushieVariant;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class PlushieBlockEntityRenderer implements BlockEntityRenderer<PlushieHoldingBlockEntity, PlushieHoldingBlockEntityRenderState> {
		public PlushieBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	public @NotNull PlushieHoldingBlockEntityRenderState createRenderState() {
		return new PlushieHoldingBlockEntityRenderState();
	}

	@Override
	public void extractRenderState(PlushieHoldingBlockEntity blockEntity, PlushieHoldingBlockEntityRenderState state, float partialTick, Vec3 vec3, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, vec3, crumblingOverlay);

		state.direction = blockEntity.getBlockState().getValue(MtfbBlockProperties.FACING);
		state.deltaStretch = blockEntity.getDeltaStretch();
		state.deltaSquish = blockEntity.getDeltaSquish();

		if (state.deltaStretch < 0.0f) {
			blockEntity.setDeltaStretch(state.deltaStretch + partialTick / SCALE_SLOWNESS);
		}
		if (blockEntity.getDeltaSquish() < SQUISH_TIME) {
			blockEntity.setDeltaSquish(blockEntity.getDeltaSquish() + partialTick / SCALE_SLOWNESS);
		}

		state.isBox = blockEntity instanceof BoxBlockEntity;

		Optional<Identifier> optionalPoseModel = blockEntity.getPoseModel(blockEntity.getBlockState());

		if (blockEntity instanceof BoxBlockEntity && optionalPoseModel.isEmpty())
			return;

		Identifier poseModel = optionalPoseModel.orElseGet(() -> MoreThanAFoxbox.id(MoreThanAFoxbox.ID + "/placeholder"));

		PlushieModel.updatePlushieModelState(state.model, poseModel);
	}

	@Override
	public void submit(PlushieHoldingBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (state.model.isEmpty())
			return;

		poseStack.pushPose();

		Matrix4f rotationTransform = BoxBlock.pointBlockToward(state.direction, 0.5f);
		poseStack.mulPose(rotationTransform);

		// Stretch & Squish
		float stretch = 1.0f - state.deltaStretch / STRETCH_TIME;
		float squish = state.deltaSquish / SQUISH_TIME;

		poseStack.translate(scaleInPlace(stretch), 0.0f, 0.0f);
		if (state.isBox)
			poseStack.translate(0.0f, scaleInPlace(squish), 0.0f);

		poseStack.scale(stretch, squish, 1.0f);

		state.model.submit(
			poseStack,
			submitNodeCollector,
			state.lightCoords,
			OverlayTexture.NO_OVERLAY,
			0
		);

		poseStack.popPose();
	}

	private float scaleInPlace(float scale) {
		return 1.0f - scale / 2.0f - 0.5f;
	}
}
