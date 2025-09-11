package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.client.screen.widget.GetItemsButton;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    @Unique
    private GetItemsButton jiYueBoss$button;
    @Unique
    private int jiYueBoss$lastLeftPos;

    public InventoryScreenMixin(InventoryMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        jiYueBoss$button = new GetItemsButton(this.leftPos + this.imageWidth, this.topPos + this.imageHeight - 20, 20, 20);
        addRenderableWidget(jiYueBoss$button);
        this.jiYueBoss$lastLeftPos = this.leftPos;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        if (this.leftPos != this.jiYueBoss$lastLeftPos) {
            if (jiYueBoss$button != null) {
                jiYueBoss$button.setPosition(this.leftPos + this.imageWidth, jiYueBoss$button.getY());
            }
            this.jiYueBoss$lastLeftPos = this.leftPos;
        }
    }
}
