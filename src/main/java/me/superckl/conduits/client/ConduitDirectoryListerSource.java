package me.superckl.conduits.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import me.superckl.conduits.Conduits;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ConduitDirectoryListerSource implements SpriteSource {
    public static final MapCodec<ConduitDirectoryListerSource> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Codec.STRING.fieldOf("source").forGetter(source -> source.sourcePath),
                            Codec.STRING.fieldOf("prefix").forGetter(source -> source.idPrefix)
                    )
                    .apply(builder, ConduitDirectoryListerSource::new)
    );
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);
    private final String sourcePath;
    private final String idPrefix;

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Conduits.LOG.info("Loading conduits directory");
        FileToIdConverter filetoidconverter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
        filetoidconverter.listMatchingResourcesFromNamespace(resourceManager, Conduits.MOD_ID).forEach((loc, resource) -> {
            ResourceLocation resourcelocation = filetoidconverter.fileToId(loc).withPrefix(this.idPrefix);
            output.add(resourcelocation, resource);
            Conduits.LOG.info("Loaded texture at " + loc + " to " + resourcelocation);
        });
    }

    @Override
    public @NotNull SpriteSourceType type() {
        return TYPE;
    }
}
