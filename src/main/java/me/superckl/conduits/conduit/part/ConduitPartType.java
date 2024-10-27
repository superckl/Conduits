package me.superckl.conduits.conduit.part;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public enum ConduitPartType implements StringRepresentable {
    JOINT("joint", true),
    SEGMENT("segment", true),
    CONNECTION("inventory_connection", false),
    MIXED_JOINT("mixed_joint", false);

    private final String name;
    private final boolean typed;

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public String path(@Nullable final DeferredHolder<?, ?> type) {
        final StringBuilder builder = new StringBuilder("conduit/");
        if (this.typed && type != null)
            builder.append(type.getId().getPath()).append('_');
        return builder.append(this.getSerializedName()).toString();
    }
}