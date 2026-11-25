package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;

/** Draws the villain's eyes fullbright, like a spider's eyes. */
public class VillainEyesLayer extends EyesLayer<VillainEntity, VillainModel> {
    private static final RenderType EYES = RenderType.eyes(PlusUltra.id("textures/entity/villain_eyes.png"));

    public VillainEyesLayer(RenderLayerParent<VillainEntity, VillainModel> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
