package io.github.orlouge.customoreveins.mixin;

import com.google.common.collect.ImmutableList;
import io.github.orlouge.customoreveins.CustomOreVein;
import io.github.orlouge.customoreveins.HasDimensionType;
import io.github.orlouge.customoreveins.PlatformHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkNoiseSampler.class)
public abstract class ChunkNoiseSamplerMixin {
    @Shadow protected abstract DensityFunction getActualDensityFunction(DensityFunction function);

    @Mutable
    @Shadow @Final private ChunkNoiseSampler.BlockStateSampler blockStateSampler;

    @Inject(method = "<init>", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addCustomOreVeins(int horizontalCellCount, NoiseConfig noiseConfig, int startBlockX, int startBlockZ, GenerationShapeConfig generationShapeConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender, CallbackInfo ci, NoiseRouter noiseRouter, NoiseRouter noiseRouter2, ImmutableList.Builder<ChunkNoiseSampler.BlockStateSampler> builder/*, DensityFunction densityFunction*/) {
        /*
        ImmutableList.Builder<ChunkNoiseSampler.BlockStateSampler> builder = ImmutableList.builder();
        builder.add(this.blockStateSampler);
         */
        RegistryEntry<DimensionType> dimension = null;
        if (((Object) noiseConfig) instanceof HasDimensionType noiseWithDimension) {
            dimension = noiseWithDimension.getDimension();
        }
        for (CustomOreVein vein : PlatformHelper.getCustomOreVeinManager().getCustomOreVeins(dimension)) {
            builder.add(vein.createSampler(
                    d -> this.getActualDensityFunction(d.apply(new CustomOreVein.Visitor(noiseConfig))),
                    noiseRouter2.veinToggle(),
                    noiseRouter2.veinRidged(),
                    noiseRouter2.veinGap(),
                    noiseConfig.getOreRandomDeriver()
            ));
        }
        this.blockStateSampler = new ChainedBlockSource(builder.build());
    }


    /*
    @Inject(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal = 1, shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void addCustomOreVeins(int horizontalCellCount, NoiseConfig noiseConfig, int startBlockX, int startBlockZ, GenerationShapeConfig generationShapeConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender, CallbackInfo ci, NoiseRouter noiseRouter, NoiseRouter noiseRouter2, ImmutableList.Builder<ChunkNoiseSampler.BlockStateSampler> builder, DensityFunction densityFunction) {
        for (CustomOreVein vein : ExampleExpectPlatform.getCustomOreVeinManager().getCustomOreVeins()) {
            builder.add(vein.createSampler(
                    this::getActualDensityFunction,
                    noiseRouter2.veinRidged(),
                    noiseRouter2.veinGap(),
                    noiseConfig.getOreRandomDeriver()
            ));
        }
    }
     */
}
