package fr.dynamx.common.contentpack.type.vehicle;

import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.subinfo.SubInfoType;
import fr.dynamx.api.contentpack.registry.DefinitionType;
import fr.dynamx.api.contentpack.registry.PackFileProperty;
import fr.dynamx.api.contentpack.registry.RegisteredSubInfoType;
import fr.dynamx.api.contentpack.registry.SubInfoTypeRegistries;
import fr.dynamx.api.entities.modules.ModuleListBuilder;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.PackPhysicsEntity;
import fr.dynamx.common.entities.modules.TrailerAttachModule;

/**
 * Info of the trailer attach point of a vehicle
 */
@RegisteredSubInfoType(name = "trailer", registries = SubInfoTypeRegistries.WHEELED_VEHICLES)
public class TrailerAttachInfo extends SubInfoType<ModularVehicleInfo> {
    @PackFileProperty(configNames = "AttachPoint", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F_INVERSED_Y)
    private Vector3f trailerAttachPoint;
    @PackFileProperty(configNames = "AttachStrength", required = false)
    private int trailerAttachStrength = 1000;

    public TrailerAttachInfo(ModularVehicleInfo owner) {
        super(owner);
    }

    @Override
    public void appendTo(ModularVehicleInfo owner) {
        if (trailerAttachPoint == null)
            throw new IllegalArgumentException("AttachPoint not configured ! In trailer of " + owner.toString());
        owner.addSubProperty(this);
    }

    @Override
    public void addModules(PackPhysicsEntity<?, ?> entity, ModuleListBuilder modules) {
        modules.add(new TrailerAttachModule((BaseVehicleEntity<?>) entity, this));
    }

    public Vector3f getAttachPoint() {
        return trailerAttachPoint;
    }

    public int getAttachStrength() {
        return trailerAttachStrength;
    }

    @Override
    public String getName() {
        return "TrailerAttachInfo";
    }
}