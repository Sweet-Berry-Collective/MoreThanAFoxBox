/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.sweetberry.more_than_a_foxbox.block.entity;

import java.util.Optional;

import dev.sweetberry.more_than_a_foxbox.data.MtfbComponents;
import dev.sweetberry.more_than_a_foxbox.data.PlushieDataComponent;
import dev.sweetberry.more_than_a_foxbox.data.PlushieVariant;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public abstract class PlushieHoldingBlockEntity extends BlockEntity {
	public static final String PLUSHIE_KEY = "plushie";
	public static final String NAME_KEY = "name";
	public static final float STRETCH_TIME = 10.0f;
	public static final float SQUISH_TIME = 10.0f;
	public static final float SCALE_SLOWNESS = 2.0f;
	private float deltaStretch = 0.0f;
	private float deltaSquish = SQUISH_TIME;

	public PlushieHoldingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void setPlushieData(PlushieDataComponent component, @Nullable Component name) {
		var builder = DataComponentPatch.builder()
			.set(MtfbComponents.PLUSHIE.get(), component);

		if (name != null) {
			builder.set(DataComponents.CUSTOM_NAME, name);
		}

		applyComponents(
			components(),
			builder.build()
		);

		setChanged();
	}

	public void removePlushieData() {
		applyComponents(
			components(),
			DataComponentPatch.builder()
				.remove(MtfbComponents.PLUSHIE.get())
				.remove(DataComponents.CUSTOM_NAME)
				.build()
		);
		setChanged();
	}

	public Optional<PlushieDataComponent> getPlushieData() {
		return Optional.ofNullable(components().get(MtfbComponents.PLUSHIE.get()));
	}

	public boolean hasPlushieData() {
		return components().has(MtfbComponents.PLUSHIE.get());
	}

	public Optional<Component> getName() {
		return Optional.ofNullable(components().get(DataComponents.CUSTOM_NAME));
	}

	public Optional<Holder.Reference<PlushieVariant>> getPlushieVariant() {
		if (level == null)
			return Optional.empty();

		return getPlushieData().flatMap(plushieDataComponent -> level.registryAccess().get(plushieDataComponent.variant()));
	}

	public abstract Optional<Identifier> getPoseModel(BlockState state);

	@Override
	protected void loadAdditional(ValueInput input) {
		var component = input.read(PLUSHIE_KEY, PlushieDataComponent.CODEC);

		var name = input.read(NAME_KEY, ComponentSerialization.CODEC).orElse(null);

		component.ifPresent(it -> setPlushieData(it, name));
	}

	@Override
	protected void saveAdditional(@NonNull ValueOutput output) {
		getPlushieData()
			.ifPresent(it -> output.store(PLUSHIE_KEY, PlushieDataComponent.CODEC, it));

		getName()
			.ifPresent(it -> output.store(NAME_KEY, ComponentSerialization.CODEC, it));
	}

	@Override
	public void setChanged() {
		super.setChanged();

		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(worldPosition);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	public void playSound(Level level, BlockPos pos) {
		if (level.isClientSide())
			return;

		var variant = this.components().get(MtfbComponents.PLUSHIE.get());

		if (variant == null)
			return;

		var maybeSound = variant.getInteractionSound(level.registryAccess());

		if (maybeSound == null)
			return;

		var center = Vec3.atCenterOf(pos);

		level.playSeededSound(null, center.x, center.y, center.z, maybeSound, SoundSource.PLAYERS, 1f, 1f,  level.getRandom().nextLong());
	}

	public float getDeltaStretch() {
		return deltaStretch;
	}

	public void setDeltaStretch(float deltaStretch) {
		this.deltaStretch = deltaStretch;
	}

	public float getDeltaSquish() {
		return deltaSquish;
	}

	public void setDeltaSquish(float deltaSquish) {
		this.deltaSquish = deltaSquish;
	}
	
	public void resetStretchSquish() {
		this.deltaStretch = -STRETCH_TIME / 2.0f;
		this.deltaSquish = SQUISH_TIME / 2.0f;
	}
}
