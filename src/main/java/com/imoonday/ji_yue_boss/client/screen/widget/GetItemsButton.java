package com.imoonday.ji_yue_boss.client.screen.widget;

import com.imoonday.ji_yue_boss.init.ModItems;
import com.imoonday.ji_yue_boss.network.CharacterActionC2SRequest;
import com.imoonday.ji_yue_boss.network.Network;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GetItemsButton extends AbstractButton {

    public GetItemsButton(int pX, int pY, int pWidth, int pHeight) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        setTooltip(Tooltip.create(Component.translatable("screen.inventory.get_items")));
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.renderItem(new ItemStack(ModItems.CHARACTER_SELECTOR.get()), this.getX() + (this.width - 16) / 2, this.getY() + (this.height - 16) / 2);
    }

    @Override
    public void onPress() {
        Network.sendToServer(new CharacterActionC2SRequest(CharacterActionC2SRequest.Action.GET_ITEMS));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
