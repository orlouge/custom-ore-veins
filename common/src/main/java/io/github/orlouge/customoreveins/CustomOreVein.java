package io.github.orlouge.customoreveins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.Optional;

public record CustomOreVein(
        Toggle toggle,
        double minOreThreshold, double maxOreThreshold, double minOreChance, double maxOreChance, double rawOreChance,
        double gapThreshold, double blockGenerationChance, int maxDensityIntrusion, double liminalDensityReduction,
        Block ore, Optional<Block> rawOreBlock, Block stone
) {
    public static final Codec<CustomOreVein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Toggle.CODEC.fieldOf("toggle").forGetter(CustomOreVein::toggle),
            Codec.DOUBLE.optionalFieldOf("min_ore_threshold", 0.0).forGetter(CustomOreVein::minOreThreshold),
            Codec.DOUBLE.optionalFieldOf("max_ore_threshold", 0.2).forGetter(CustomOreVein::maxOreThreshold),
            Codec.DOUBLE.optionalFieldOf("min_ore_chance", 0.1).forGetter(CustomOreVein::minOreChance),
            Codec.DOUBLE.optionalFieldOf("max_ore_chance", 0.3).forGetter(CustomOreVein::maxOreChance),
            Codec.DOUBLE.optionalFieldOf("raw_ore_chance", 0.02).forGetter(CustomOreVein::rawOreChance),
            Codec.DOUBLE.optionalFieldOf("gap_threshold", -0.3).forGetter(CustomOreVein::gapThreshold),
            Codec.DOUBLE.optionalFieldOf("block_generation_chance", 0.7).forGetter(CustomOreVein::blockGenerationChance),
            Codec.INT.optionalFieldOf("max_density_intrusion", 20).forGetter(CustomOreVein::maxDensityIntrusion),
            Codec.DOUBLE.optionalFieldOf("liminal_density_reduction", 0.2).forGetter(CustomOreVein::liminalDensityReduction),
            Registries.BLOCK.getCodec().fieldOf("ore").forGetter(CustomOreVein::ore),
            Registries.BLOCK.getCodec().optionalFieldOf("raw_ore").forGetter(CustomOreVein::rawOreBlock),
            Registries.BLOCK.getCodec().fieldOf("stone").forGetter(CustomOreVein::stone)
    ).apply(instance, CustomOreVein::new));

    public ChunkNoiseSampler.BlockStateSampler createSampler(DensityFunction.DensityFunctionVisitor toActual, DensityFunction veinRidged, DensityFunction veinGap, RandomSplitter randomDeriver) {
        DensityFunction veinToggle = toggle.density.apply(toActual);
        BlockState blockState = null;
        return (pos) -> {
            double rawValue = veinToggle.sample(pos);
            int y = pos.blockY();
            int topDist = this.toggle.maxY - y;
            int bottomDist = y - this.toggle.minY;
            double value = 0.6 * Math.min(this.toggle.maxValue - rawValue, rawValue - this.toggle.minValue) / (this.toggle.maxValue - this.toggle.minValue);
            if (bottomDist >= 0 && topDist >= 0) {
                int minDist = Math.min(topDist, bottomDist);
                double scaledDist = MathHelper.clampedMap(minDist, 0.0, this.maxDensityIntrusion, -this.liminalDensityReduction, 0.0);
                if (value + scaledDist < 0) {
                    return blockState;
                } else {
                    Random random = randomDeriver.split(pos.blockX(), y, pos.blockZ());
                    if (random.nextFloat() > this.blockGenerationChance) {
                        return blockState;
                    } else if (veinRidged.sample(pos) >= 0.0) {
                        return blockState;
                    } else {
                        double oreValue = MathHelper.clampedMap(value, this.minOreThreshold, this.maxOreThreshold, this.minOreChance, this.maxOreChance);
                        if (value >= this.minOreThreshold && (double) random.nextFloat() < oreValue && veinGap.sample(pos) > this.gapThreshold) {
                            return (random.nextFloat() < this.rawOreChance ? this.rawOreBlock.orElse(this.ore) : this.ore).getDefaultState();
                        } else {
                            return this.stone.getDefaultState();
                        }
                    }
                }
            } else {
                return blockState;
            }
        };
    }

    public static class Visitor implements DensityFunction.DensityFunctionVisitor {
        private final NoiseConfig noiseConfig;

        public Visitor(NoiseConfig noiseConfig) {
            this.noiseConfig = noiseConfig;
        }

        @Override
        public DensityFunction apply(DensityFunction densityFunction) {
            if (densityFunction instanceof InterpolatedNoiseSampler interpolatedNoiseSampler) {
                return interpolatedNoiseSampler.copyWithRandom(noiseConfig.randomDeriver.split("terrain"));
            } else {
                return densityFunction;
            }
        }

        @Override
        public DensityFunction.Noise apply(DensityFunction.Noise noiseDensityFunction) {
            RegistryEntry<DoublePerlinNoiseSampler.NoiseParameters> registryEntry = noiseDensityFunction.noiseData();
            DoublePerlinNoiseSampler sampler = noiseConfig.getOrCreateSampler(registryEntry.getKey().orElseThrow());
            return new DensityFunction.Noise(registryEntry, sampler);
        }
    }

    private record Toggle(DensityFunction density, double minValue, double maxValue, int minY, int maxY) {
        public static final Codec<Toggle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DensityFunction.FUNCTION_CODEC.fieldOf("density").forGetter(Toggle::density),
                Codec.DOUBLE.fieldOf("min_value").forGetter(Toggle::minValue),
                Codec.DOUBLE.fieldOf("max_value").forGetter(Toggle::maxValue),
                Codec.INT.fieldOf("min_y").forGetter(Toggle::minY),
                Codec.INT.fieldOf("max_y").forGetter(Toggle::maxY)
        ).apply(instance, Toggle::new));
    }
}
