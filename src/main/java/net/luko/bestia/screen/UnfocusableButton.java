package net.luko.bestia.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class UnfocusableButton extends Button {
    protected UnfocusableButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
    }

    @Override
    public void setFocused(boolean f){}

    @Override
    public boolean isFocused(){return false;}
}
