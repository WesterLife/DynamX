package fr.dynamx.common.contentpack.type.objects;

import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.IInfoOwner;
import fr.dynamx.api.contentpack.object.part.BasePart;
import fr.dynamx.api.contentpack.object.subinfo.ISubInfoType;
import fr.dynamx.api.contentpack.registry.DefinitionType;
import fr.dynamx.api.contentpack.registry.PackFileProperty;
import fr.dynamx.api.events.CreatePackItemEvent;
import fr.dynamx.common.blocks.DynamXBlock;
import fr.dynamx.common.contentpack.loader.ObjectLoader;
import fr.dynamx.common.contentpack.type.ParticleEmitterInfo;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class BlockObject<T extends BlockObject<?>> extends AbstractProp<T> implements ParticleEmitterInfo.IParticleEmitterContainer {
    /**
     * List of owned {@link ISubInfoType}s
     */
    protected final List<ISubInfoType<T>> subProperties = new ArrayList<>();
    protected PropObject<?> propObject;

    @PackFileProperty(configNames = "Rotate", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F, required = false, defaultValue = "0 0 0")
    @Getter
    @Setter
    protected Vector3f rotation = new Vector3f(0, 0, 0); //Not supported by props

    @PackFileProperty(configNames = "LightLevel", defaultValue = "0", required = false)
    @Getter
    protected float lightLevel;

    @PackFileProperty(configNames = "Material", required = false, defaultValue = "ROCK")
    @Getter
    protected Material material;

    private final List<ParticleEmitterInfo<?>> particleEmitters = new ArrayList<>();

    public BlockObject(String packName, String fileName) {
        super(packName, fileName);
        this.itemIcon = "Block";
    }

    @Override
    @SuppressWarnings("unchecked")
    public IInfoOwner<T> createOwner(ObjectLoader<T, ?, ?> loader) {
        CreatePackItemEvent.CreateSimpleBlockEvent event = new CreatePackItemEvent.CreateSimpleBlockEvent((ObjectLoader<BlockObject<?>, DynamXBlock<BlockObject<?>>, BlockObject<?>>) loader, this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isOverridden()) {
            return (IInfoOwner<T>) event.getSpawnItem();
        } else {
            return new DynamXBlock<>((T) this, material != null ? material : Material.ROCK);
        }
    }

    @Override
    public String getTranslationKey(IInfoOwner<T> item, int itemMeta) {
        return super.getTranslationKey(item, itemMeta).replace("item", "tile");
    }

    @Override
    public void addSubProperty(ISubInfoType<T> property) {
        subProperties.add(property);
    }

    @Override
    public List<ISubInfoType<T>> getSubProperties() {
        return subProperties;
    }

    public CompoundCollisionShape getCompoundCollisionShape() {
        return compoundCollisionShape;
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
        return "BlockObject named " + getFullName();
    }

    public boolean isObj() {
        return getModel().endsWith(".obj");
    }

    @Override
    public void addPart(BasePart<T> tBasePart) {

    }
}
