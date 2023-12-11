package io.github.orlouge.customoreveins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CustomOreVeinManager extends JsonDataLoader {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Identifier ID = new Identifier(CustomOreVeinsMod.MOD_ID, "worldgen/custom_ore_veins");
    private Map<Identifier, CustomOreVein> customOreVeins = Map.of();
    public Supplier<DynamicRegistryManager> registryAccess = () -> null;

    public CustomOreVeinManager() {
        super(GSON, ID.getPath());
    }



    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, CustomOreVein> veins = new HashMap<>();
        DynamicRegistryManager registryAccess = this.registryAccess.get();
        RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, registryAccess == null ? BuiltinRegistries.createWrapperLookup() : registryAccess);

        prepared.forEach((identifier, jsonElement) -> {
            if (jsonElement == null) return;
            CustomOreVein vein = CustomOreVein.CODEC.decode(ops, jsonElement).getOrThrow(false, s -> {}).getFirst();
            veins.put(identifier, vein);
            System.out.println("Loaded " + identifier);
        });

        this.customOreVeins = veins;
    }

    public Collection<CustomOreVein> getCustomOreVeins(RegistryEntry<DimensionType> dimension) {
        if (dimension == null || dimension.getKey().isEmpty()) return Collections.emptyList();
        return customOreVeins.values().stream().filter(cov -> cov.dimension().equals(dimension.getKey().get())).toList();
    }
}
