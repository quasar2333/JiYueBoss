package com.imoonday.ji_yue_boss.client;

import com.imoonday.ji_yue_boss.character.CharacterReceiver;
import com.imoonday.ji_yue_boss.client.screen.CharacterSelectionScreen;
import net.minecraft.client.Minecraft;

public class ClientUtil {

    public static void updateCharacter(CharacterReceiver.Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CharacterReceiver receiver) {
            receiver.updateCharacter(context);
        }
    }

    public static void openCharacterSelectionScreen(CharacterReceiver.Context context) {
        CharacterSelectionScreen screen = new CharacterSelectionScreen(true);
        Minecraft.getInstance().setScreen(screen);
        screen.updateCharacter(context);
    }

    public static void closeCharacterSelectionScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CharacterSelectionScreen) {
            mc.setScreen(null);
        }
    }
}
