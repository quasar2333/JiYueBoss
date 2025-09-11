package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Supplier;

public class GeoItemExtensions<T extends Item & GeoItem> implements IClientItemExtensions {

    private GeoItemRenderer<T> renderer;
    private final Supplier<String> id;

    public GeoItemExtensions(String id) {
        this(() -> id);
    }

    public GeoItemExtensions(Supplier<String> id) {
        this.id = id;
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (this.renderer == null) {
            this.renderer = new GeoItemRenderer<>(new DefaultedItemGeoModel<>(new ResourceLocation(JiYueBoss.MODID, id.get())));
        }

        return this.renderer;
    }
}
