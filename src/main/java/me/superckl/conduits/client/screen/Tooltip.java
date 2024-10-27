package me.superckl.conduits.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

public interface Tooltip<T> {

    void renderTooltip(T widget, PoseStack pose, int mouseX, int mouseY);

}
