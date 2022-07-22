package fr.dynamx.common.contentpack.parts;

import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.IShapeContainer;
import fr.dynamx.api.contentpack.object.part.BasePart;
import fr.dynamx.api.contentpack.object.part.IShapeInfo;
import fr.dynamx.api.contentpack.object.subinfo.ISubInfoTypeOwner;
import fr.dynamx.api.contentpack.registry.DefinitionType;
import fr.dynamx.api.contentpack.registry.PackFileProperty;
import fr.dynamx.utils.debug.DynamXDebugOption;
import fr.dynamx.utils.debug.DynamXDebugOptions;
import fr.dynamx.utils.optimization.MutableBoundingBox;

public class PartShape<T extends ISubInfoTypeOwner<T>> extends BasePart<T> implements IShapeInfo {
    @PackFileProperty(configNames = "Type", type = DefinitionType.DynamXDefinitionTypes.SHAPE_TYPE, description = "common.shape.type", required = false)
    private final EnumPartType shapeType = EnumPartType.BOX;
    //@PackFileProperty(configNames = "CanPlayersWalkOnTop", required = false)
    //TODO SUPPORT THIS protected boolean canPlayersWalkOnTop;
    protected MutableBoundingBox boundingBox;

    public PartShape(T owner, String partName) {
        super(owner, partName);
    }


    @Override
    public void appendTo(T vehicleInfo) {
        super.appendTo(vehicleInfo);
        ((IShapeContainer) vehicleInfo).addCollisionShape(this);
        Vector3f min = getPosition().subtract(getScale());
        Vector3f max = getPosition().add(getScale());
        this.boundingBox = new MutableBoundingBox(
                min.x, min.y, min.z,
                max.x, max.y, max.z);
    }

    @Override
    public String getName() {
        return "PartShape named " + getPartName() + " of type " + getShapeType() + " in " + getOwner().getName();
    }

    @Override
    public Vector3f getSize() {
        return getScale();
    }

    public MutableBoundingBox getBoundingBox() {
        return boundingBox;
    }

    public EnumPartType getShapeType() {
        return shapeType;
    }
    @Override
    public DynamXDebugOption getDebugOption() {
        return DynamXDebugOptions.PLAYER_TO_OBJECT_COLLISION_DEBUG;
    }

    public enum EnumPartType {
        BOX, CYLINDER, SPHERE;

        public static EnumPartType fromString(String targetName) {
            for (EnumPartType enumPartType : values()) {
                if (enumPartType.name().equalsIgnoreCase(targetName)) {
                    return enumPartType;
                }
            }
            throw new IllegalArgumentException("Invalid shape type '" + targetName + "'");
        }
    }
}
