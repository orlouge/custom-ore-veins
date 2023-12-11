package io.github.orlouge.customoreveins.mixin;

import io.github.orlouge.customoreveins.HasDimensionType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoiseConfig.class)
public class NoiseConfigMixin implements HasDimensionType {
    public RegistryEntry<DimensionType> dimensionType = null;

    @Override
    public RegistryEntry<DimensionType> getDimension() {
        return dimensionType;
    }

    @Override
    public void setDimension(RegistryEntry<DimensionType> dimension) {
        this.dimensionType = dimension;
    }
}
