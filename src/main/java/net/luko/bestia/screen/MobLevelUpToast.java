package net.luko.bestia.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

public class MobLevelUpToast implements Toast {
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/side_panel.png");
    private static final int BACKGROUND_TEXTURE_BORDER_SIZE = 9;
    protected static final int BACKGROUND_TEXTURE_WIDTH = 240;
    protected static final int BACKGROUND_TEXTURE_HEIGHT = 294;
    private static final Font FONT = Minecraft.getInstance().font;
    private static final String LEVEL_UP_TEXT = "Level up!";
    private static final int PADDING = 3;
    private static final int TOP_PADDING = 6;
    private static final float LEVEL_UP_SCALE = 1.5F;
    private static final float LINE_HEIGHT = FONT.lineHeight * LEVEL_UP_SCALE;

    private final BestiaryEntryScreenComponent entry;

    public MobLevelUpToast(ResourceLocation mobId, BestiaryData data){
        this.entry = new BestiaryEntryScreenComponent(mobId, data, null, false);
    }

    public int width(){
        return BACKGROUND_TEXTURE_BORDER_SIZE * 2 + PADDING * 2 + BestiaryEntryScreenComponent.ENTRY_WIDTH;
    }

    public int height(){
        return BACKGROUND_TEXTURE_BORDER_SIZE * 2 + PADDING + TOP_PADDING + (int)LINE_HEIGHT + BestiaryEntryScreenComponent.ENTRY_HEIGHT;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        blitPanel(guiGraphics, 0, 0, width(), height());

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)width() / 2F - ((float)FONT.width(LEVEL_UP_TEXT) / 2F) * LEVEL_UP_SCALE, BACKGROUND_TEXTURE_BORDER_SIZE + TOP_PADDING, 0F);
        poseStack.scale(LEVEL_UP_SCALE, LEVEL_UP_SCALE, 1F);

        guiGraphics.drawString(FONT, LEVEL_UP_TEXT,0, 0, 0xFFFFFF);

        poseStack.popPose();

        this.entry.render(guiGraphics, BACKGROUND_TEXTURE_BORDER_SIZE + PADDING, (int)(BACKGROUND_TEXTURE_BORDER_SIZE + TOP_PADDING + LINE_HEIGHT));

        return timeSinceLastVisible >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    protected void blitPanel(GuiGraphics guiGraphics, int x, int y, int width, int height){
        /* Top-left corner */ guiGraphics.blit(BACKGROUND_TEXTURE,
                x, y,
                0, 0,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        /* Top border */ guiGraphics.blitRepeating(BACKGROUND_TEXTURE,
                x + BACKGROUND_TEXTURE_BORDER_SIZE, y,
                width - BACKGROUND_TEXTURE_BORDER_SIZE * 2, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, 0,
                BACKGROUND_TEXTURE_WIDTH - BACKGROUND_TEXTURE_BORDER_SIZE * 2, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        /* Top-right corner */ guiGraphics.blit(BACKGROUND_TEXTURE,
                x + width - BACKGROUND_TEXTURE_BORDER_SIZE, y,
                BACKGROUND_TEXTURE_WIDTH - BACKGROUND_TEXTURE_BORDER_SIZE, 0,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);

        /* Left border */ guiGraphics.blitRepeating(BACKGROUND_TEXTURE,
                x, y + BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, height - BACKGROUND_TEXTURE_BORDER_SIZE * 2,
                0, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_HEIGHT - BACKGROUND_TEXTURE_BORDER_SIZE * 2,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        /* Middle */ guiGraphics.blitRepeating(BACKGROUND_TEXTURE,
                x + BACKGROUND_TEXTURE_BORDER_SIZE, y + BACKGROUND_TEXTURE_BORDER_SIZE,
                width - BACKGROUND_TEXTURE_BORDER_SIZE * 2, height - BACKGROUND_TEXTURE_BORDER_SIZE * 2,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH - BACKGROUND_TEXTURE_BORDER_SIZE * 2, BACKGROUND_TEXTURE_HEIGHT - BACKGROUND_TEXTURE_BORDER_SIZE * 2,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        /* Right border */ guiGraphics.blitRepeating(BACKGROUND_TEXTURE,
                x + width - BACKGROUND_TEXTURE_BORDER_SIZE, y + BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, height - BACKGROUND_TEXTURE_BORDER_SIZE * 2,
                BACKGROUND_TEXTURE_WIDTH - BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_HEIGHT - BACKGROUND_TEXTURE_BORDER_SIZE * 2,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);


        /* Bottom-left corner */ guiGraphics.blit(BACKGROUND_TEXTURE,
                x, y + height - BACKGROUND_TEXTURE_BORDER_SIZE,
                0, BACKGROUND_TEXTURE_HEIGHT - BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        /* Bottom border */ guiGraphics.blitRepeating(BACKGROUND_TEXTURE,
                x + BACKGROUND_TEXTURE_BORDER_SIZE, y + height - BACKGROUND_TEXTURE_BORDER_SIZE,
                width - BACKGROUND_TEXTURE_BORDER_SIZE * 2, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_HEIGHT - BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH - BACKGROUND_TEXTURE_BORDER_SIZE * 2, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        /* Bottom-right corner */ guiGraphics.blit(BACKGROUND_TEXTURE,
                x + width - BACKGROUND_TEXTURE_BORDER_SIZE, y + height - BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH - BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_HEIGHT - BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_BORDER_SIZE, BACKGROUND_TEXTURE_BORDER_SIZE,
                BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
    }
}
