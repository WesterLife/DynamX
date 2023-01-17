package fr.dynamx.common.contentpack.parts;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.part.IDrawablePart;
import fr.dynamx.api.contentpack.object.part.InteractivePart;
import fr.dynamx.api.contentpack.registry.*;
import fr.dynamx.api.entities.IModuleContainer;
import fr.dynamx.api.entities.VehicleEntityProperties;
import fr.dynamx.api.events.PhysicsEntityEvent;
import fr.dynamx.api.events.VehicleEntityEvent;
import fr.dynamx.client.renders.RenderPhysicsEntity;
import fr.dynamx.client.renders.model.renderer.ObjModelRenderer;
import fr.dynamx.client.renders.vehicle.RenderBaseVehicle;
import fr.dynamx.common.DynamXContext;
import fr.dynamx.common.contentpack.type.vehicle.ModularVehicleInfo;
import fr.dynamx.common.contentpack.type.vehicle.PartWheelInfo;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.WheelsModule;
import fr.dynamx.common.physics.entities.modules.WheelsPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.wheel.WheelState;
import fr.dynamx.utils.debug.DynamXDebugOption;
import fr.dynamx.utils.debug.DynamXDebugOptions;
import fr.dynamx.utils.optimization.GlQuaternionPool;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@RegisteredSubInfoType(name = "wheel", registries = SubInfoTypeRegistries.WHEELED_VEHICLES, strictName = false)
public class PartWheel extends InteractivePart<BaseVehicleEntity<?>, ModularVehicleInfo> implements IDrawablePart<BaseVehicleEntity<?>> {
    @IPackFilePropertyFixer.PackFilePropertyFixer(registries = SubInfoTypeRegistries.WHEELED_VEHICLES)
    public static final IPackFilePropertyFixer PROPERTY_FIXER = (object, key, value) -> {
        if ("isRight".equals(key))
            return new IPackFilePropertyFixer.FixResult("IsRight", true);
        return null;
    };

    @PackFileProperty(configNames = "IsRight")
    private boolean isRight;
    @PackFileProperty(configNames = "IsSteerable")
    private boolean wheelIsSteerable;
    @PackFileProperty(configNames = "MaxTurn")
    private float wheelMaxTurn;
    @PackFileProperty(configNames = "DrivingWheel")
    private boolean drivingWheel;
    @PackFileProperty(configNames = "HandBrakingWheel", required = false)
    private boolean handBrakingWheel;
    @PackFileProperty(configNames = "AttachedWheel")
    private String defaultWheelName;
    @PackFileProperty(configNames = "MudGuard", required = false)
    private String mudGuardPartName;
    @PackFileProperty(configNames = "RotationPoint", required = false, type = DefinitionType.DynamXDefinitionTypes.VECTOR3F_INVERSED_Y)
    private Vector3f rotationPoint;
    @PackFileProperty(configNames = "SuspensionAxis", required = false)
    private Quaternion suspensionAxis = new Quaternion();

    private PartWheelInfo defaultWheelInfo;

    public PartWheel(ModularVehicleInfo owner, String partName) {
        super(owner, partName, 0.75f, 0.75f);
    }

    @Override
    public void appendTo(ModularVehicleInfo owner) {
        super.appendTo(owner);
        if (getRotationPoint() == null)
            rotationPoint = getPosition();
        else
            getRotationPoint().multLocal(getScaleModifier(owner));
        owner.arrangeWheelID(this);
    }

    @Override
    public void addPart(BaseVehicleEntity<?> vehicle) {
        if (!(vehicle instanceof IModuleContainer.IPropulsionContainer) || !(((IModuleContainer.IPropulsionContainer) vehicle).getPropulsion() instanceof WheelsModule))
            throw new IllegalStateException("The entity " + vehicle + " has PartWheels, but does not implement IHavePropulsion or the propulsion is not a WheelsModule !");
        ((WheelsPhysicsHandler) ((IModuleContainer.IPropulsionContainer) vehicle).getPropulsion().getPhysicsHandler()).addWheel(this, getDefaultWheelInfo());
    }

    @Override
    public void removePart(BaseVehicleEntity<?> vehicle) {
        ((WheelsPhysicsHandler) ((IModuleContainer.IPropulsionContainer) vehicle).getPropulsion().getPhysicsHandler()).removeWheel(getId());
    }

    @Override
    public DynamXDebugOption getDebugOption() {
        return DynamXDebugOptions.WHEELS;
    }

    @Override
    public boolean interact(BaseVehicleEntity<?> entity, EntityPlayer with) {
        return false;
    }

    public void setDefaultWheelInfo(PartWheelInfo partWheelInfo) {
        if (partWheelInfo == null) {
            throw new IllegalArgumentException("Attached wheel info " + getDefaultWheelName() + " was not found !");
        } else {
            defaultWheelInfo = partWheelInfo;
            setBox(new AxisAlignedBB(-partWheelInfo.getWheelWidth(), -partWheelInfo.getWheelRadius(), -partWheelInfo.getWheelRadius(),
                    partWheelInfo.getWheelWidth(), partWheelInfo.getWheelRadius(), partWheelInfo.getWheelRadius()));
        }
    }

    public PartWheelInfo getDefaultWheelInfo() {
        return defaultWheelInfo;
    }

    public boolean isRight() {
        return isRight;
    }

    public boolean isWheelIsSteerable() {
        return wheelIsSteerable;
    }

    public float getWheelMaxTurn() {
        return wheelMaxTurn;
    }

    public boolean isDrivingWheel() {
        return drivingWheel;
    }

    public String getDefaultWheelName() {
        return defaultWheelName;
    }

    public String getMudGuardPartName() {
        return mudGuardPartName;
    }

    public Vector3f getRotationPoint() {
        return rotationPoint;
    }

    public Quaternion getSuspensionAxis() {
        return suspensionAxis;
    }

    public boolean isHandBrakingWheel() {
        return handBrakingWheel;
    }

    public void setHandBrakingWheel(boolean handBrakingWheel) {
        this.handBrakingWheel = handBrakingWheel;
    }

    @Override
    public String getName() {
        return "PartWheel named " + getPartName();
    }

    @Override
    public void drawParts(BaseVehicleEntity<?> entity, RenderPhysicsEntity<?> render, ModularVehicleInfo packInfo, byte textureId, float partialTicks) {
        if (MinecraftForge.EVENT_BUS.post(new VehicleEntityEvent.Render(VehicleEntityEvent.Render.Type.PROPULSION, (RenderBaseVehicle<?>) render, entity, PhysicsEntityEvent.Phase.PRE, partialTicks, null))) {
            return;
        }
        WheelsModule wheelsModule = entity != null ? entity.getModuleByType(WheelsModule.class) : null;
        packInfo.getPartsByType(PartWheel.class).forEach(partWheel -> {
            if (wheelsModule == null || wheelsModule.getWheelsStates()[partWheel.getId()] != WheelState.REMOVED) {
                renderWheel(entity, partWheel, render, wheelsModule, packInfo, textureId, partialTicks);
            }
        });
        MinecraftForge.EVENT_BUS.post(new VehicleEntityEvent.Render(VehicleEntityEvent.Render.Type.PROPULSION, (RenderBaseVehicle<?>) render, entity, PhysicsEntityEvent.Phase.POST, partialTicks, null));
    }

    @Override
    public String[] getRenderedParts() {
        if (getMudGuardPartName() != null)
            return new String[] {getMudGuardPartName()};
        return new String[0];
    }

    @Override
    public void onTexturesChange(BaseVehicleEntity<?> entity) {
        if(!entity.hasModuleOfType(WheelsModule.class))
            return;
        entity.getModuleByType(WheelsModule.class).computeWheelsTextureIds();
    }

    @SideOnly(Side.CLIENT)
    protected static void renderWheel(@Nullable BaseVehicleEntity<?> entity, PartWheel partWheel, RenderPhysicsEntity<?> render, @Nullable WheelsModule wheelsModule, ModularVehicleInfo packInfo, byte textureId, float partialTicks) {
        int index;
        Quaternion baseRotation = partWheel.getSuspensionAxis();
        PartWheelInfo info = wheelsModule != null ? wheelsModule.getWheelInfo(partWheel.getId()) : partWheel.getDefaultWheelInfo();
        if (info.isModelValid()) {
            GlStateManager.pushMatrix();
            {
                /* Translation to the wheel rotation point */
                GlStateManager.translate(partWheel.getRotationPoint().x, partWheel.getRotationPoint().y, partWheel.getRotationPoint().z);

                /* Apply wheel base rotation */
                if (baseRotation.getW() != 0)
                    GlStateManager.rotate(GlQuaternionPool.get(baseRotation));

                if(wheelsModule != null) {
                    /* Suspension translation */
                    index = VehicleEntityProperties.getPropertyIndex(partWheel.getId(), VehicleEntityProperties.EnumVisualProperties.SUSPENSIONLENGTH);
                    GlStateManager.translate(0, -(wheelsModule.prevVisualProperties[index] + (wheelsModule.visualProperties[index] - wheelsModule.prevVisualProperties[index]) * partialTicks), 0);

                    /* Steering rotation*/
                    if (partWheel.isWheelIsSteerable()) {
                        index = VehicleEntityProperties.getPropertyIndex(partWheel.getId(), VehicleEntityProperties.EnumVisualProperties.STEERANGLE);
                        GlStateManager.rotate((wheelsModule.prevVisualProperties[index] + (wheelsModule.visualProperties[index] - wheelsModule.prevVisualProperties[index]) * partialTicks), 0.0F, 1.0F, 0.0F);
                    }
                }
                /* Render mudguard */
                if (partWheel.getMudGuardPartName() != null) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0, 0.2, 0);
                    GlStateManager.scale(packInfo.getScaleModifier().x, packInfo.getScaleModifier().y, packInfo.getScaleModifier().z);
                    DynamXContext.getObjModelRegistry().getModel(packInfo.getModel()).renderGroups(partWheel.getMudGuardPartName(), textureId);
                    GlStateManager.popMatrix();
                }

                //Remove wheel base rotation
                if (baseRotation.getW() != 0)
                    GlStateManager.rotate(GlQuaternionPool.get(baseRotation.inverse()));

                // Translate to render pos, from rotation pos
                GlStateManager.translate(partWheel.getPosition().x - partWheel.getRotationPoint().x, partWheel.getPosition().y - partWheel.getRotationPoint().y, partWheel.getPosition().z - partWheel.getRotationPoint().z);

                if(wheelsModule != null) {
                    index = VehicleEntityProperties.getPropertyIndex(partWheel.getId(), VehicleEntityProperties.EnumVisualProperties.ROTATIONANGLE);
                    //Fix sign problems for wheel rotation
                    float prev = wheelsModule.prevVisualProperties[index];
                    if (prev - wheelsModule.visualProperties[index] > 180)
                        prev -= 360;
                    if (prev - wheelsModule.visualProperties[index] < -180)
                        prev += 360;
                    //Then render
                    if (partWheel.isRight()) {
                        /* Wheel rotation (Right-Side)*/
                        GlStateManager.rotate(180, 0, 1, 0);
                        GlStateManager.rotate((prev + (wheelsModule.visualProperties[index] - prev) * partialTicks), -1.0F, 0.0F, 0.0F);
                    } else {
                        /* Wheel rotation (Left-Side)*/
                        GlStateManager.rotate(-(prev + (wheelsModule.visualProperties[index] - prev) * partialTicks), -1.0F, 0.0F, 0.0F);
                    }
                }
                /*Rendering the wheels */
                ObjModelRenderer model = DynamXContext.getObjModelRegistry().getModel(info.getModel());
                //Scale
                GlStateManager.scale(info.getScaleModifier().x, info.getScaleModifier().y, info.getScaleModifier().z);
                //If the wheel is not flattened, or the model does not supports flattening
                if (wheelsModule == null || wheelsModule.getWheelsStates()[partWheel.getId()] != WheelState.ADDED_FLATTENED || !model.renderGroups("rim", wheelsModule.getWheelsTextureId()[partWheel.getId()])) {
                    byte wheelTextureId = wheelsModule != null ? wheelsModule.getWheelsTextureId()[partWheel.getId()] : info.getIdForVariant(packInfo.getVariantName(textureId));
                    render.renderModel(model, entity, wheelTextureId);
                }
            }
            GlStateManager.popMatrix();
        }
    }
}
