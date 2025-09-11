package com.imoonday.ji_yue_boss.client.screen;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.character.Character;
import com.imoonday.ji_yue_boss.character.CharacterReceiver;
import com.imoonday.ji_yue_boss.network.CharacterActionC2SRequest;
import com.imoonday.ji_yue_boss.network.Network;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

public class CharacterSelectionScreen extends Screen implements CharacterReceiver {

    private static final ResourceLocation BG_TEXTURE = new ResourceLocation(JiYueBoss.MODID, "textures/gui/character_selection.png");
    private static final int BG_WIDTH = 138;
    private static final int BG_HEIGHT = 135;
    private static final int NAME_AREA_WIDTH = 60;
    private static final int NAME_AREA_HEIGHT = 18;
    private int bgX;
    private int bgY;
    private Character currentCharacter = Character.EMPTY;
    private boolean currentSelectable = false;
    private final boolean manuallyInit;
    private boolean initialized = false;
    private SimpleSoundInstance lastQuote;
    private Button prevButton;
    private Button selectButton;
    private Button nextButton;

    public CharacterSelectionScreen() {
        this(false);
    }

    public CharacterSelectionScreen(boolean manuallyInit) {
        super(Component.empty());
        this.manuallyInit = manuallyInit;
    }

    @Override
    protected void init() {
        super.init();

        bgX = (width - BG_WIDTH) / 2;
        bgY = (height - BG_HEIGHT) / 2 - NAME_AREA_HEIGHT + 4;

        int buttonY = height - 50;
        prevButton = createAndAddActionButton(Component.translatable("screen.character_selection.prev"), button -> sendAction(CharacterActionC2SRequest.Action.PREV), width / 2 - 50 - 10 - 50, buttonY, 50);
        selectButton = createAndAddActionButton(Component.translatable("screen.character_selection.select"), button -> selectCurrentCharacter(), width / 2 - 50, buttonY, 100);
        nextButton = createAndAddActionButton(Component.translatable("screen.character_selection.next"), button -> sendAction(CharacterActionC2SRequest.Action.NEXT), width / 2 + 50 + 10, buttonY, 50);

        if (!initialized) {
            if (!manuallyInit) {
                sendAction(CharacterActionC2SRequest.Action.INIT);
            }
        } else {
            updateButtons();
        }
    }

    private Button createAndAddActionButton(Component text, Button.OnPress onPress, int x, int y, int width) {
        Button button = Button.builder(text, onPress).bounds(x, y, width, 20).build();
        button.active = false;
        addRenderableWidget(button);
        return button;
    }

    private void sendAction(CharacterActionC2SRequest.Action action) {
        Network.sendToServer(new CharacterActionC2SRequest(currentCharacter.id(), action));
    }

    private void selectCurrentCharacter() {
        Component name = currentCharacter.name();
        Object displayName = name != null ? name : currentCharacter.id();
        if (minecraft != null) {
            minecraft.setScreen(new ConfirmScreen(bl -> {
                minecraft.setScreen(this);
                if (bl) {
                    sendAction(CharacterActionC2SRequest.Action.SELECT);
                }
            }, Component.translatable("screen.character_selection.confirm_select", displayName), Component.translatable("screen.character_selection.confirm_select.desc")));
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        PoseStack poseStack = pGuiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(1.25f, 1.25f, 1.0f);
        poseStack.translate(-0.35 * BG_WIDTH, -0.2 * BG_HEIGHT, 0.0f);
        pGuiGraphics.blit(BG_TEXTURE, bgX, bgY, 0, 0, BG_WIDTH, BG_HEIGHT);
        renderCharacterInfo(pGuiGraphics);
        poseStack.popPose();
    }

    private void renderCharacterInfo(GuiGraphics guiGraphics) {
        if (!initialized) {
            guiGraphics.drawCenteredString(font, Component.translatable("screen.character_selection.syncing"), width / 2, height / 2 - 20, 0xFFFFFF);
            return;
        } else if (currentCharacter.isEmpty()) {
            guiGraphics.drawCenteredString(font, Component.translatable("screen.character_selection.empty"), width / 2, height / 2 - 20, 0xFFFFFF);
            return;
        }

        ResourceLocation portraitLocation = currentCharacter.portrait();
        if (portraitLocation != null) {
            guiGraphics.blit(portraitLocation, bgX + 9, bgY + 18, 63, 113, 0, 0, 607, 1080, 607, 1080);
        }


        Component name = currentCharacter.name();
        if (name != null) {
            guiGraphics.drawCenteredString(font, name, bgX + NAME_AREA_WIDTH / 2, bgY + (NAME_AREA_HEIGHT - font.lineHeight) / 2 + 1, 5635925);
        }

        int textX = bgX + 9 + 63 + 5;
        int textY = bgY + 18 + 3;
        Component description = currentCharacter.description();
        drawSplitString(guiGraphics, description, textX, textY, 0xFFFFFF);
    }

    private void drawSplitString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        if (text != null) {
            for (FormattedCharSequence sequence : font.split(text, BG_WIDTH - 63 - 10 - 10)) {
                guiGraphics.drawString(font, sequence, x, y, color);
                y += font.lineHeight + 5;
            }
        }
    }

    @Override
    public void updateCharacter(Context context) {
        if (context.override()) {
            currentCharacter = context.character();

            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            if (lastQuote != null) {
                soundManager.stop(lastQuote);
                lastQuote = null;
            }

            SoundEvent quote = currentCharacter.getRandomQuote(RandomSource.create());
            if (quote != null) {
                SimpleSoundInstance soundInstance = SimpleSoundInstance.forUI(quote, 1.0f);
                soundManager.play(soundInstance);
                lastQuote = soundInstance;
            }
        }

        if (currentCharacter == context.character()) {
            currentSelectable = context.selectable();
            if (!initialized) {
                initialized = true;
            }
            updateButtons();
            updateSelectedState(context.isOwner());
        }
    }

    private void updateSelectedState(boolean isOwner) {
        if (selectButton != null) {
            MutableComponent message = isOwner ? Component.translatable("screen.character_selection.select.owner") : Component.translatable("screen.character_selection.select");
            selectButton.setMessage(message);
        }
    }

    public void updateButtons() {
        if (prevButton != null) {
            prevButton.active = initialized;
        }
        if (selectButton != null) {
            selectButton.active = initialized && currentSelectable;
        }
        if (nextButton != null) {
            nextButton.active = initialized;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        if (lastQuote != null && minecraft != null) {
            minecraft.getSoundManager().stop(lastQuote);
        }
    }
}
