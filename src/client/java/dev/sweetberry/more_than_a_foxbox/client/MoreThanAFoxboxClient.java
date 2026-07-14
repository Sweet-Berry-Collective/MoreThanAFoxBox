/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.sweetberry.more_than_a_foxbox.client;

import dev.sweetberry.more_than_a_foxbox.MoreThanAFoxbox;
import dev.sweetberry.more_than_a_foxbox.block.entity.MtfbBlockEntityTypes;
import dev.sweetberry.more_than_a_foxbox.client.block.entity.render.PlushieBlockEntityRenderer;
import dev.sweetberry.more_than_a_foxbox.client.entity.render.BoxSeatEntityRenderer;
import dev.sweetberry.more_than_a_foxbox.client.network.MtfbClientNetworking;
import dev.sweetberry.more_than_a_foxbox.client.screen.GuiPlushieRenderer;
import dev.sweetberry.more_than_a_foxbox.client.screen.SewingTableScreen;
import dev.sweetberry.more_than_a_foxbox.client.util.ModelUtil;
import dev.sweetberry.more_than_a_foxbox.entity.MtfbEntityTypes;
import dev.sweetberry.more_than_a_foxbox.menu.MtfbMenus;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemModels;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MoreThanAFoxboxClient implements ClientModInitializer {
	public static final Map<Identifier, ExtraModelKey<BlockStateModel>> MODEL_KEYS = new HashMap<>();
	
	@Override
	public void onInitializeClient() {
		MtfbClientNetworking.register();

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
