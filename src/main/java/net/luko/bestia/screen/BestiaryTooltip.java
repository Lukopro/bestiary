package net.luko.bestia.screen;

import net.minecraft.network.chat.Component;

import java.util.List;

public record BestiaryTooltip(
        int left,
        int right,
        int top,
        int bottom,
        List<Component> tooltip
) {
    public boolean contains(int x, int y){
        return x >= left && x <= right
            && y >= top && y < bottom;
    }

    public BestiaryTooltip scale(float scale, float scrollAmount){
        return new BestiaryTooltip((int)(left * scale), (int)(right * scale),
                (int)(top * scale - scrollAmount), (int)(bottom * scale - scrollAmount), tooltip);
    }
}
