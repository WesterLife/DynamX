package fr.dynamx.common.contentpack.type.objects;

import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.IDynamXItem;
import fr.dynamx.api.contentpack.object.IPhysicsPackInfo;
import fr.dynamx.api.contentpack.object.subinfo.ISubInfoType;
import fr.dynamx.api.contentpack.object.subinfo.ISubInfoTypeOwner;
import fr.dynamx.api.contentpack.registry.DefinitionType;
import fr.dynamx.api.contentpack.registry.PackFileProperty;
import fr.dynamx.api.contentpack.registry.RegisteredSubInfoType;
import fr.dynamx.api.contentpack.registry.SubInfoTypeRegistries;
import fr.dynamx.api.entities.modules.ModuleListBuilder;
import fr.dynamx.api.events.CreatePackItemEvent;
import fr.dynamx.common.contentpack.ContentPackLoader;
import fr.dynamx.common.contentpack.DynamXObjectLoaders;
import fr.dynamx.common.contentpack.loader.InfoList;
import fr.dynamx.common.contentpack.loader.PackFilePropertyData;
import fr.dynamx.common.contentpack.loader.SubInfoTypeAnnotationCache;
import fr.dynamx.common.contentpack.type.MaterialVariantsInfo;
import fr.dynamx.common.contentpack.type.ObjectCollisionsHelper;
import fr.dynamx.common.contentpack.type.ParticleEmitterInfo;
import fr.dynamx.common.entities.PackPhysicsEntity;
import fr.dynamx.common.items.ItemProps;
import fr.dynamx.utils.DynamXUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RegisteredSubInfoType(name = "prop", registries = SubInfoTypeRegistries.BLOCKS_AND_PROPS, strictName = false)
public class PropObject<T extends PropObject<?>> extends AbstractProp<T> implements IPhysicsPackInfo,
        ISubInfoType<BlockObject<?>>, ParticleEmitterInfo.IParticleEmitterContainer {
    private final BlockObject<?> owner;
    @PackFileProperty(configNames = "EmptyMass")
    @Getter
    @Setter
    protected int emptyMass;
    @PackFileProperty(configNames = "CenterOfGravityOffset", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F)
    @Getter
    @Setter
    protected Vector3f centerOfMass;
    @PackFileProperty(configNames = "SpawnOffset", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F, required = false, defaultValue = "0 0.65 0")
    @Getter
    @Setter
    protected Vector3f spawnOffset = new Vector3f(0, 0.65f, 0);
    @PackFileProperty(configNames = "ContinuousCollisionDetection", required = false, defaultValue = "false")
    @Getter
    @Setter
    protected boolean isCCDEnabled;
    @PackFileProperty(configNames = "Friction", required = false, defaultValue = "0.5")
    @Getter
    @Setter
    protected float friction = 0.5f;
    @PackFileProperty(configNames = "Margin", required = false, defaultValue = "0.04")
    @Getter
    @Setter
    protected float margin = 0.04f;
    @PackFileProperty(configNames = "DespawnTime", required = false, defaultValue = "\"-1\" (disabled)")
    @Getter
    @Setter
    protected float despawnTime = -1;
    @PackFileProperty(configNames = "LinearDamping", required = false, defaultValue = "0")
    @Getter
    @Setter
    protected float linearDamping;
    @PackFileProperty(configNames = "AngularDamping", required = false, defaultValue = "0")
    @Getter
    @Setter
    protected float angularDamping;
    @PackFileProperty(configNames = "Bounciness", required = false, defaultValue = "0")
    @Getter
    @Setter
    protected float restitutionFactor;

    private List<ParticleEmitterInfo<?>> particleEmitters = new ArrayList<>();

    public PropObject(ISubInfoTypeOwner<BlockObject<?>> owner, String fileName) {
        super(owner.getPackName(), fileName);
        this.itemIcon = "Prop";
        BlockObject<?> block = (BlockObject<?>) owner;
        this.owner = block;
        this.setDefaultName(block.getDefaultName());
        this.setDescription(block.getDescription());
        this.model = block.getModel();
        this.itemScale = block.getItemScale();
        this.itemTranslate = block.getItemTranslate();
        this.itemRotate = block.getItemRotate();
        this.item3DRenderLocation = block.get3DItemRenderLocation();
        this.translation = block.getTranslation();
        this.scaleModifier = block.getScaleModifier();
        this.renderDistance = block.getRenderDistance();
        this.creativeTabName = block.getCreativeTabName();
        this.useComplexCollisions = block.useComplexCollisions();
        this.particleEmitters = block.getParticleEmitters();
        this.collisionsHelper = block.getCollisionsHelper().copy();
    }

    @Override
    public List<PackFilePropertyData<?>> getInitiallyConfiguredProperties() {
        //Don't require properties of the block
        return SubInfoTypeAnnotationCache.getOrLoadData(BlockObject.class).values().stream().filter(PackFilePropertyData::isRequired).collect(Collectors.toList());
    }

    @Override
    public boolean postLoad(boolean hot) {
        collisionsHelper.loadCollisions(this, DynamXUtils.getModelPath(getPackName(), model), "", centerOfMass, 0, useComplexCollisions, scaleModifier, ObjectCollisionsHelper.CollisionType.PROP);
        collisionsHelper.getPhysicsCollisionShape().setMargin(margin);
        return super.postLoad(hot);
    }

    @Override
    public void appendTo(BlockObject<?> owner) {
        owner.propObject = this;
        DynamXObjectLoaders.PROPS.loadItems(this, ContentPackLoader.isHotReloading);
    }

    @Nullable
    @Override
    public BlockObject<?> getOwner() {
        return owner;
    }

    @Override
    public IDynamXItem<T> createItem(InfoList<T> loader) {
        CreatePackItemEvent.PropsItem<T, ?> event = new CreatePackItemEvent.PropsItem(loader, this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isOverridden()) {
            return event.getObjectItem();
        } else {
            return new ItemProps(this);
        }
    }

    @Override
    public MaterialVariantsInfo<?> getVariants() {
        return owner != null ? owner.getVariants() : null;
    }

    @Override
    public ItemStack getPickedResult(int metadata) {
        return new ItemStack((Item) getItems()[0], 1, metadata);
    }

    @Override
    public void addSubProperty(ISubInfoType<T> property) {
    }

    @Override
    public List<ISubInfoType<T>> getSubProperties() {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldRegisterModel() {
        return owner == null || !model.equals(owner.getModel()); //Don't register the model twice if there is a block owning this prop
    }

    @Override
    public void addParticleEmitter(ParticleEmitterInfo<?> emitterInfo) {
        particleEmitters.add(emitterInfo);
    }

    @Override
    public List<ParticleEmitterInfo<?>> getParticleEmitters() {
        return particleEmitters;
    }

    @Override
    public String toString() {
        return "PropObject named " + getFullName();
    }

    @Override
    public void addModules(PackPhysicsEntity<?, ?> entity, ModuleListBuilder modules) {
        //TODO SUPPORT PARTS WITH MODULES
        if (getOwner() != null)
            getOwner().getLightSources().values().forEach(compoundLight -> compoundLight.addModules(entity, modules));
    }
}
