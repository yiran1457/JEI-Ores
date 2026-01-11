package net.yiran.jei_ores.compat.jei.widget;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TextureTooltipWidget  implements IRecipeWidget {
    private final ScreenPosition position;   // 绝对位置（相对配方背景）
    private final ResourceLocation texture;

    private final int width;
    private final int height;

    private final int u;
    private final int v;

    private final int textureWidth;  // 整张贴图尺寸
    private final int textureHeight;

    private final List<Component> tooltipLines;

    public TextureTooltipWidget(ResourceLocation texture,
                                int x, int y,
                                int width, int height,
                                int u, int v,
                                int textureWidth, int textureHeight,
                                List<Component> tooltipLines) {
        this.position = new ScreenPosition(x, y);
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.tooltipLines = tooltipLines;
    }

    @Override
    public ScreenPosition getPosition() {
        return position;
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 画裁剪纹理：等价 EMI 的 addTexture(DISTRIBUTION, x, y, 32, 16, 0, type.v)
        guiGraphics.blit(
                texture,
                position.x(), position.y(),
                u, v,
                width, height,
                textureWidth, textureHeight
        );
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            tooltip.addAll(tooltipLines);/*
            for (Component line : tooltipLines) {
                tooltip.add(line);
            }*/
        }
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        double rx = mouseX - position.x();
        double ry = mouseY - position.y();
        boolean absolute =
                rx >= 0 && rx < width &&
                        ry >= 0 && ry < height;

        return  absolute;
    }
}