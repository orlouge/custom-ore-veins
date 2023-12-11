package io.github.orlouge.customoreveins;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;

public interface HasDimensionType {
    RegistryEntry<DimensionType> getDimension();
    void setDimension(RegistryEntry<DimensionType> dimension);
}
