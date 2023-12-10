package io.github.orlouge.customoreveins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
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
        Toggle toggle, Optional<Ridged> ridged, Optional<Gap> gap,
        double minOreThreshold, double maxOreThreshold, double minOreChance, double maxOreChance, double rawOreChance,
        double blockGenerationChance, Optional<Integer> maxDensityIntrusion, double liminalDensityReduction,
        Block ore, Optional<Block> rawOreBlock, Block stone
) {
    public static final Codec<CustomOreVein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Toggle.CODEC.fieldOf("toggle").forGetter(CustomOreVein::toggle),
            Ridged.CODEC.optionalFieldOf("ridged").forGetter(CustomOreVein::ridged),
            Gap.CODEC.optionalFieldOf("gap").forGetter(CustomOreVein::gap),
            Codec.DOUBLE.optionalFieldOf("min_ore_threshold", 0.0).forGetter(CustomOreVein::minOreThreshold),
            Codec.DOUBLE.optionalFieldOf("max_ore_threshold", 0.2).forGetter(CustomOreVein::maxOreThreshold),
            Codec.DOUBLE.optionalFieldOf("min_ore_chance", 0.16).forGetter(CustomOreVein::minOreChance),
            Codec.DOUBLE.optionalFieldOf("max_ore_chance", 0.5).forGetter(CustomOreVein::maxOreChance),
            Codec.DOUBLE.optionalFieldOf("raw_ore_chance", 0.02).forGetter(CustomOreVein::rawOreChance),
            Codec.DOUBLE.optionalFieldOf("block_generation_chance", 0.7).forGetter(CustomOreVein::blockGenerationChance),
            Codec.INT.optionalFieldOf("max_density_intrusion").forGetter(CustomOreVein::maxDensityIntrusion),
            Codec.DOUBLE.optionalFieldOf("liminal_density_reduction", 0.2).forGetter(CustomOreVein::liminalDensityReduction),
            Registries.BLOCK.getCodec().fieldOf("ore").forGetter(CustomOreVein::ore),
            Registries.BLOCK.getCodec().optionalFieldOf("raw_ore").forGetter(CustomOreVein::rawOreBlock),
            Registries.BLOCK.getCodec().fieldOf("stone").forGetter(CustomOreVein::stone)
    ).apply(instance, CustomOreVein::new));

    public ChunkNoiseSampler.BlockStateSampler createSampler(DensityFunction.DensityFunctionVisitor toActual, DensityFunction veinToggleDefault, DensityFunction veinRidgedDefault, DensityFunction veinGapDefault, RandomSplitter randomDeriver) {
        DensityFunction veinToggle = toggle.density.map(d -> d.apply(toActual)).orElse(veinToggleDefault);
        DensityFunction veinRidged = ridged.flatMap(d -> d.density).map(d -> d.apply(toActual)).orElse(veinRidgedDefault);
        DensityFunction veinGap = gap.flatMap(d -> d.density).map(d -> d.apply(toActual)).orElse(veinGapDefault);
        int maxDensityIntrusion = this.maxDensityIntrusion.orElse((this.toggle.maxY - this.toggle.minY) * 20 / 64);
        double gapThreshold = this.gap.map(d -> d.minValue).orElse(-0.3);
        double ridgedThreshold = this.ridged.map(Ridged::minValue).orElse(0.0);
        double valueRange = this.toggle.maxValue.orElse(1.0) - this.toggle.minValue.orElse(-1.0);
        return (pos) -> {
            double rawValue = veinToggle.sample(pos);
            int y = pos.blockY();
            int topDist = this.toggle.maxY - y;
            int bottomDist = y - this.toggle.minY;
            double value = this.toggle.maxValue.map(
                    max -> this.toggle.minValue.map(min -> Math.min(max - rawValue, rawValue - min)).orElse(max - rawValue)
            ).orElse(this.toggle.minValue.map(min -> rawValue - min).orElse(rawValue - 0.67));
            value /= valueRange;
            if (bottomDist >= 0 && topDist >= 0) {
                int minDist = Math.min(topDist, bottomDist);
                double scaledDist = MathHelper.clampedMap(minDist, 0.0, maxDensityIntrusion, -this.liminalDensityReduction, 0.0);
                if (0.6 * value + scaledDist < 0) {
                    return null;
                } else {
                    Random random = randomDeriver.split(pos.blockX(), y, pos.blockZ());
                    if (random.nextFloat() > this.blockGenerationChance) {
                        return null;
                    } else {
                        if (veinRidged.sample(pos) >= ridgedThreshold) {
                            return null;
                        } else {
                            double oreValue = MathHelper.clampedMap(value, this.minOreThreshold, this.maxOreThreshold, this.minOreChance, this.maxOreChance);
                            if (value >= this.minOreThreshold && (double) random.nextFloat() < oreValue && veinGap.sample(pos) > gapThreshold) {
                                return (random.nextFloat() < this.rawOreChance ? this.rawOreBlock.orElse(this.ore) : this.ore).getDefaultState();
                            } else {
                                return this.stone.getDefaultState();
                            }
                        }
                    }
                }
            } else {
                return null;
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

    private record Toggle(Optional<DensityFunction> density, Optional<Double> minValue, Optional<Double> maxValue, int minY, int maxY) {
        public static final Codec<Toggle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DensityFunction.FUNCTION_CODEC.optionalFieldOf("density").forGetter(Toggle::density),
                Codec.DOUBLE.optionalFieldOf("min_value").forGetter(Toggle::minValue),
                Codec.DOUBLE.optionalFieldOf("max_value").forGetter(Toggle::maxValue),
                Codec.INT.fieldOf("min_y").forGetter(Toggle::minY),
                Codec.INT.fieldOf("max_y").forGetter(Toggle::maxY)
        ).apply(instance, Toggle::new));
    }

    private record Ridged(Optional<DensityFunction> density, double minValue) {
        public static final Codec<Ridged> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DensityFunction.FUNCTION_CODEC.optionalFieldOf("density").forGetter(Ridged::density),
                Codec.DOUBLE.optionalFieldOf("min_value", 0.0).forGetter(Ridged::minValue)
        ).apply(instance, Ridged::new));
    }

    private record Gap(Optional<DensityFunction> density, double minValue) {
        public static final Codec<Gap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DensityFunction.FUNCTION_CODEC.optionalFieldOf("density").forGetter(Gap::density),
                Codec.DOUBLE.optionalFieldOf("min_value", -0.3).forGetter(Gap::minValue)
        ).apply(instance, Gap::new));
    }
}
