package net.luko.bestia.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static Lazy<KeyMapping> KEY_OPEN_BESTIARY = Lazy.of(() ->
        new KeyMapping(
                "key.bestiary.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.bestiary"
        )
    );
}
