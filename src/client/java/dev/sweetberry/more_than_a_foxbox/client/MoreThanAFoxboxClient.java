/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.sweetberry.more_than_a_foxbox.client;

import dev.sweetberry.more_than_a_foxbox.MoreThanAFoxbox;
import dev.sweetberry.more_than_a_foxbox.block.MtfbBlocks;
import dev.sweetberry.more_than_a_foxbox.block.entity.MtfbBlockEntityTypes;
import dev.sweetberry.more_than_a_foxbox.block.entity.PlushieBlockEntity;
import dev.sweetberry.more_than_a_foxbox.client.block.entity.render.PlushieBlockEntityRenderer;
import dev.sweetberry.more_than_a_foxbox.client.entity.render.BoxSeatEntityRenderer;
import dev.sweetberry.more_than_a_foxbox.client.network.MtfbClientNetworking;
import dev.sweetberry.more_than_a_foxbox.client.screen.GuiPlushieRenderer;
import dev.sweetberry.more_than_a_foxbox.client.screen.SewingTableScreen;
import dev.sweetberry.more_than_a_foxbox.client.util.ModelUtil;
import dev.sweetberry.more_than_a_foxbox.entity.MtfbEntityTypes;
import dev.sweetberry.more_than_a_foxbox.menu.MtfbMenus;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemModels;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoreThanAFoxboxClient implements ClientModInitializer {
	public static final Map<Identifier, ExtraModelKey<BlockStateModel>> MODEL_KEYS = new HashMap<>();
	
	@Override
	public void onInitializeClient() {
		MtfbClientNetworking.register();

		ModelLoadingPlugin.register(pluginContext -> {
			pluginContext.modifyBlockModelAfterBake().register((model, context) -> {
				if (context.state().is(MtfbBlocks.PLUSHIE.get())) {
					return new BlockStateModel() {
						@Override
						public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
							model.collectParts(random, output);
						}

						@Override
						public Material.Baked particleMaterial() {
							return model.particleMaterial();
						}

						@Override
						public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
							PlushieBlockEntity be = level.getBlockEntity(pos, MtfbBlockEntityTypes.PLUSHIE.get())
								.orElse(null);
							if (be != null) {
								Identifier poseModel = be.getPoseModel(be.getBlockState())
									.orElseGet(() -> MoreThanAFoxbox.id(MoreThanAFoxbox.ID + "/placeholder"));

								if (MODEL_KEYS.containsKey(poseModel)) {
									return Minecraft.getInstance().getModelManager()
										.getModel(MODEL_KEYS.get(poseModel))
										.particleMaterial(level, pos, state);
								}
							}
							return particleMaterial();
						}

						@Override
						public @BakedQuad.MaterialFlags int materialFlags() {
							return 0;
						}
					};
				}
				return model;
			});
		});
		PreparableModelLoadingPlugin.register(
			ModelUtil::getPlushieModels,
			(data, pluginContext) -> {
				for (Identifier id : data) {
					ExtraModelKey<BlockStateModel> modelKey = ExtraModelKey.create(id::toString);
					MODEL_KEYS.put(id, modelKey);
					pluginContext.addModel(modelKey, SimpleUnbakedExtraModel.blockStateModel(id));
				}
			}
		);

		ItemModels.ID_MAPPER.put(MoreThanAFoxbox.id("plushie"), PlushieModel.Unbaked.CODEC);

		BlockEntityRenderers.register(
			MtfbBlockEntityTypes.CARDBOARD_BOX.get(),
			PlushieBlockEntityRenderer::new
		);

		BlockEntityRenderers.register(
			MtfbBlockEntityTypes.PLUSHIE.get(),
			PlushieBlockEntityRenderer::new
		);

		EntityRenderers.register(
			MtfbEntityTypes.BOX_SEAT.get(),
			BoxSeatEntityRenderer::new
		);

		PictureInPictureRendererRegistry.register(_ -> new GuiPlushieRenderer());

		MenuScreens.register(MtfbMenus.SEWING_TABLE.get(), SewingTableScreen::new);
	}
}
