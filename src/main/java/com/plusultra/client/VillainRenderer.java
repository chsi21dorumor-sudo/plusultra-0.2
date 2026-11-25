package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VillainRenderer extends HumanoidMobRenderer<VillainEntity, VillainModel> {
    private static final ResourceLocation TEXTURE = PlusUltra.id("textures/entity/villain.png");

    public VillainRenderer(EntityRendererProvider.Context context) {
        super(context, new VillainModel(context.bakeLayer(VillainModel.LAYER)), 0.6f);
        this.addLayer(new VillainEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VillainEntity entity) {
        return TEXTURE;
    }
}
