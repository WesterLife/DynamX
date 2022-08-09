package fr.dynamx.api.obj;

import fr.dynamx.api.contentpack.object.INamedObject;
import fr.dynamx.common.contentpack.PackInfo;
import net.minecraft.util.ResourceLocation;

/**
 * Defines an obj model location <br>
 * Can be used to search it as a mc packs resource, or generate a File to find it on server
 */
public class ObjModelPath implements INamedObject {
    private final PackInfo packInfo;
    private final ResourceLocation modelPath;

    public ObjModelPath(PackInfo packInfo, ResourceLocation modelPath) {
        this.packInfo = packInfo;
        this.modelPath = modelPath;
    }

    public PackInfo getPackInfo() {
        return packInfo;
    }

    /**
     * @return The path of the model inside the pack, excluding the modid (typically models/mymodels/myfirstmodel.obj)
     */
    @Override
    public String getName() {
        return modelPath.getPath();
    }

    @Override
    public String getPackName() {
        return packInfo.getPathName();
    }

    /**
     * @return The path of the model inside of the pack (typically dynamxmod:models/mymodels/myfirstmodel.obj)
     */
    public ResourceLocation getModelPath() {
        return modelPath;
    }

    @Override
    public String toString() {
        return "ObjModelPath{" +
                "packInfo=" + packInfo +
                ", modelPath=" + modelPath +
                '}';
    }
}
