package net.luko.bestia.screen.side;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luko.bestia.client.ClientBestiaryData;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.leaderboard.LeaderboardEntry;
import net.luko.bestia.screen.BestiaryScreen;
import net.luko.bestia.screen.widget.CustomButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BestiaryLeaderboardScreenComponent extends BestiarySideScreenComponent{
    protected final ResourceLocation mobId;
    protected final List<LeaderboardEntry> leaderboard;
    protected final List<LeaderboardEntryDisplay> leaderboardDisplay;
    protected CustomButton backButton;

    protected static final float TEXT_SCALE = 1.5F;
    protected static final float TITLE_SCALE = 3F;
    protected static final float TITLE_PADDING = 10F;

    protected static final String RANK_FORMAT = "%d. ";
    protected static final String LEVEL_FORMAT = " - Level %d";

    protected static final String NO_ENTRIES = "Leaderboard is empty!";
    protected static final String NO_MINIMUM = "No minimum level.";
    protected static final String MINIMUM = "Level %d needed to qualify!";

    public BestiaryLeaderboardScreenComponent(int x, int y, int width, BestiaryScreen parentScreen, ResourceLocation mobId, List<LeaderboardEntry> leaderboard) {
        super(x, y, width, parentScreen);

        this.mobId = mobId;
        this.leaderboard = this.sortLeaderboard(leaderboard);
        this.leaderboardDisplay = this.cacheDisplay();

        this.finalizeLayout();
    }

    private List<LeaderboardEntry> sortLeaderboard(List<LeaderboardEntry> leaderboard){
        return leaderboard.stream()
                .sorted(Comparator.comparingInt(LeaderboardEntry::level).reversed())
                .toList();
    }

    private List<LeaderboardEntryDisplay> cacheDisplay(){
        List<LeaderboardEntryDisplay> leaderboardDisplay = new ArrayList<>();
        for(int i = 0; i < this.leaderboard.size(); i++){
            LeaderboardEntry entry = this.leaderboard.get(i);
            LeaderboardEntryDisplay display = new LeaderboardEntryDisplay(i + 1, entry);
            leaderboardDisplay.add(display);
        }
        return leaderboardDisplay;
    }

    public static int getNeededWidth(){
        // Expected max values as strings
        int maxRank = 999;
        String maxPlayerName = "WWWWWWWWWWWWWWWW";
        int maxLevel = BestiaCommonConfig.MAX_LEVEL.get();

        Font font = Minecraft.getInstance().font;

        int maxRankWidth = (int)(TEXT_SCALE * font.width(String.format(RANK_FORMAT, maxRank)));
        int maxPlayerNameWidth = (int)(TEXT_SCALE * font.width(maxPlayerName));
        int maxLevelWidth = (int)(TEXT_SCALE * font.width(String.format(LEVEL_FORMAT, maxLevel)));

        return maxRankWidth + maxPlayerNameWidth + maxLevelWidth;
    }

    public static int getEntryHeightUnscaled(){
        return FONT.lineHeight + PADDING;
    }

    public static int getTitleHeight(){
        return (int)(TITLE_SCALE * FONT.lineHeight + TITLE_PADDING);
    }

    public static int getEntryHeightScaled(){
        return (int)(TEXT_SCALE * (FONT.lineHeight + PADDING));
    }

    @Override
    public int getNeededHeight() {
        // getEntryHeightScaled() is used for leaderboard entry lines, no-entries note, and footnote, as they use the same scale
        return (int)(getTitleHeight()
                + (leaderboard.isEmpty()
                    ? getEntryHeightScaled() + 2 * PADDING
                    : TITLE_PADDING + leaderboard.size() * getEntryHeightScaled()))
                + getEntryHeightScaled();
    }

    @Override
    public void finalizeLayout(){
        super.finalizeLayout();
        this.backButton = new CustomButton(
                this.x, this.y, BUTTON_SIZE, BUTTON_SIZE, Component.literal("<"),
                btn -> this.parentScreen.openFocusedEntryScreenComponent(mobId, ClientBestiaryData.getFor(mobId))
        );
    }

    @Override
    public void moveX(int x){
        super.moveX(x);
        this.backButton.setX(x);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY){
        super.render(guiGraphics, mouseX, mouseY);

        this.backButton.render(guiGraphics, mouseX, mouseY, 0F);
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack poseStack = guiGraphics.pose();
        float nextY = this.y + BORDER_SIZE + TITLE_PADDING;
        float minX = this.x + BORDER_SIZE + PADDING;
        float maxX = this.x + BORDER_SIZE + PADDING + this.availableWidth;

        nextY = this.drawTitle(guiGraphics, minX, maxX, nextY);
        nextY += TITLE_PADDING;



        if(this.leaderboard.isEmpty()){
            nextY = this.drawNoEntriesText(guiGraphics, minX, maxX, nextY);
        } else {
            poseStack.pushPose();
            poseStack.scale(TEXT_SCALE, TEXT_SCALE, 1F);

            for (LeaderboardEntryDisplay display : this.leaderboardDisplay) {
                poseStack.pushPose();
                poseStack.translate(scaled(minX, TEXT_SCALE), scaled(nextY, TEXT_SCALE), 0F);
                nextY += display.render(guiGraphics, 0, 0, this.availableWidth);
                poseStack.popPose();
            }

            poseStack.popPose();
        }

        nextY += PADDING;
        nextY = this.drawFootnote(guiGraphics, minX, maxX, nextY);
    }

    private float drawTitle(GuiGraphics guiGraphics, float minX, float maxX, float y) {
        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.scale(TITLE_SCALE, TITLE_SCALE, 1F);

        float nextY = y;

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(mobId);
        String title = entityType != null ? entityType.getDescription().getString() : mobId.toString();

        float titleWidth = FONT.width(title) * TITLE_SCALE;
        float titleX = minX + (maxX - minX) / 2F - (titleWidth / 2F);

        poseStack.pushPose();
        poseStack.translate(scaled(titleX, TITLE_SCALE), scaled(nextY, TITLE_SCALE), 0F);
        guiGraphics.drawString(FONT, title, 0, 0, 0xFFFFFF);
        nextY += (FONT.lineHeight * TITLE_SCALE + PADDING);
        poseStack.popPose();

        poseStack.popPose();

        return nextY;
    }

    private float drawNoEntriesText(GuiGraphics guiGraphics, float minX, float maxX, float y){
        float nextY = y;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.scale(TEXT_SCALE, TEXT_SCALE, 1F);

        float textWidth = FONT.width(NO_ENTRIES) * TEXT_SCALE;
        float textX = minX + (maxX - minX) / 2F - (textWidth / 2F);

        poseStack.translate(scaled(textX, TEXT_SCALE), scaled(nextY, TEXT_SCALE), 0F);

        guiGraphics.drawString(FONT, NO_ENTRIES, 0, 0, 0xFFFFFF);
        nextY += TEXT_SCALE * FONT.lineHeight + PADDING;

        poseStack.popPose();

        return nextY;
    }

    private float drawFootnote(GuiGraphics guiGraphics, float minX, float maxX, float y){
        float nextY = y;
        int minLevel = BestiaCommonConfig.MIN_LEADERBOARD_LEVEL.get();
        String footnote = minLevel == 0 ? NO_MINIMUM : String.format(MINIMUM, minLevel);

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.scale(TEXT_SCALE, TEXT_SCALE, 1F);

        float textWidth = FONT.width(footnote) * TEXT_SCALE;
        float textX = minX + (maxX - minX) / 2F - (textWidth / 2F);

        poseStack.translate(scaled(textX, TEXT_SCALE), scaled(nextY, TEXT_SCALE), 0F);

        guiGraphics.drawString(FONT, footnote, 0, 0, 0xAAAAAA);
        nextY += TEXT_SCALE * FONT.lineHeight;

        poseStack.popPose();

        return nextY;
    }

    private float scaled(float coordinate, float scale){
        return coordinate / scale;
    }

    protected record LeaderboardEntryDisplay (int rank, LeaderboardEntry entry){
        public Component entryText(int maxWidth){
            String rankText = String.format(RANK_FORMAT, rank);
            String playerText = entry.name();
            String levelText = String.format(LEVEL_FORMAT, entry.level());

            int rankTextWidth = (int)(TEXT_SCALE * FONT.width(rankText));
            int playerTextWidth = (int)(TEXT_SCALE * FONT.width(playerText));
            int levelTextWidth = (int)(TEXT_SCALE * FONT.width(levelText));

            int nameMaxWidth = maxWidth;
            nameMaxWidth -= rankTextWidth;
            nameMaxWidth -= levelTextWidth;

            boolean shaved = false;
            if(playerTextWidth > nameMaxWidth){
                shaved = true;
                while(playerTextWidth > nameMaxWidth){
                    playerText = playerText.substring(0, playerText.length() - 1);
                    playerTextWidth = (int)(TEXT_SCALE * FONT.width(playerText));
                }
            }

            if(shaved) playerText = (playerText.length() < 3
                    ? ""
                    : playerText.substring(0, playerText.length() - 3))
                    + "...";

            return Component.literal(rankText + playerText + levelText);
        }

        public int render(GuiGraphics guiGraphics, int x, int y, int maxWidth){
            guiGraphics.drawString(FONT, this.entryText(maxWidth), x, y, 0xFFFFFF);
            return getEntryHeightScaled();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(this.backButton.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
