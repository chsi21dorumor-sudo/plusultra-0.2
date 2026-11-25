package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Humanoid body with extra cubes attached to existing parts: horns, spiked shoulder pads and a cloak.
 * Extra cubes use otherwise-unused areas of the 64x64 skin: pads (0,32), cloak (20,32), horns (0,48).
 */
public class VillainModel extends HumanoidModel<VillainEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(PlusUltra.id("villain"), "main");

    public VillainModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0F), 0.0F);
        PartDefinition root = mesh.getRoot();

        // horns on the head
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("horn_r",
                CubeListBuilder.create().texOffs(0, 48).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.35F));
        head.addOrReplaceChild("horn_l",
                CubeListBuilder.create().texOffs(0, 48).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.35F));

        // shoulder pads
        root.getChild("right_arm").addOrReplaceChild("pad_r",
                CubeListBuilder.create().texOffs(0, 32).addBox(-3.5F, -3.5F, -2.5F, 5.0F, 2.0F, 5.0F),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("pad_l",
                CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -3.5F, -2.5F, 5.0F, 2.0F, 5.0F),
                PartPose.ZERO);

        // cloak hanging down the back
        root.getChild("body").addOrReplaceChild("cloak",
                CubeListBuilder.create().texOffs(20, 32).addBox(-5.0F, 0.0F, 2.0F, 10.0F, 16.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }
}
