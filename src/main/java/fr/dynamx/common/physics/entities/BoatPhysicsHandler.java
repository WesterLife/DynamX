package fr.dynamx.common.physics.entities;

import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import fr.dynamx.common.contentpack.parts.PartFloat;
import fr.dynamx.common.entities.vehicles.BoatEntity;
import fr.dynamx.utils.maths.DynamXGeometry;
import fr.dynamx.utils.optimization.QuaternionPool;
import fr.dynamx.utils.optimization.Vector3fPool;
import fr.dynamx.utils.physics.DynamXPhysicsHelper;

import java.util.ArrayList;
import java.util.List;

public class BoatPhysicsHandler<T extends BoatEntity<?>> extends BaseVehiclePhysicsHandler<T> {
    public List<PartFloat> floatList;
    public List<Vector3f> debugBuoyForces;
    public List<Vector3f> debugDragForces ;

    public BoatPhysicsHandler(T entity) {
        super(entity);
    }

    @Override
    public PhysicsRigidBody createShape(Vector3f position, Quaternion rotation, float spawnRotation) {
        PhysicsRigidBody shape = super.createShape(position, rotation, spawnRotation);
        shape.setAngularDamping(0.6f);
        shape.setLinearDamping(0.6f);
        shape.setEnableSleep(false);
        return shape;
    }

    @Override
    public void onPackInfosReloaded() {
        super.onPackInfosReloaded();
        floatList = packInfo.getPartsByType(PartFloat.class);
        //Debug, to clean
        if(debugBuoyForces == null){
            debugBuoyForces = new ArrayList<>();
        }else{
            debugBuoyForces.clear();
        }
        if(debugDragForces == null){
            debugDragForces = new ArrayList<>();
        }else{
            debugDragForces.clear();
        }
        floatList.forEach(pf -> pf.childrenPositionList.forEach(p -> {
            debugBuoyForces.add(new Vector3f());
            debugDragForces.add(new Vector3f());
        }));
    }
    
    @Override
    public void update() {
        super.update();
        if(floatList == null){
            floatList = packInfo.getPartsByType(PartFloat.class);
        }

        if (!isInLiquid) {
            return;
        }
        int i = 0;
        for (PartFloat partFloat : floatList) {
            for (Vector3f floatCenter : partFloat.childrenPositionList) {
                handleBuoyancy(floatCenter, partFloat, waterLevel, i++);
            }
        }
    }

    private void handleBuoyancy(Vector3f partPos, PartFloat partFloat, float waterLevel, int i) {
        Vector3f rotatedFloatPos = DynamXGeometry.rotateVectorByQuaternion(partPos, handledEntity.physicsRotation);
        Vector3f floatPosInWorldPos = handledEntity.physicsPosition.add(rotatedFloatPos);

        double dy = waterLevel - floatPosInWorldPos.y;

        if (!(dy > 0)) {
            return;
        }
        float area = partFloat.size * partFloat.size;
        dy = Math.min(dy, partFloat.getScale().y);

        Vector3f buoyForce = Vector3fPool.get(0, dy * area * DynamXPhysicsHelper.WATER_DENSITY * DynamXPhysicsHelper.GRAVITY, 0);

        debugBuoyForces.get(i).set(buoyForce.mult(0.001f));
        collisionObject.applyForce(buoyForce.multLocal(0.05f), rotatedFloatPos);

        Vector3f velocityAtPoint = DynamXPhysicsHelper.getVelocityAtPoint(getLinearVelocity(), getAngularVelocity(), rotatedFloatPos);
        float velocityLength = velocityAtPoint.length();
        Vector3f dragDir = velocityAtPoint.normalize();
        Vector3f dragForce = dragDir.multLocal(0.5f * DynamXPhysicsHelper.WATER_DENSITY * velocityLength * velocityLength * partFloat.dragCoefficient * area);

        if (Vector3f.isValidVector(dragForce))
            collisionObject.applyForce(dragForce.multLocal(0.05f), rotatedFloatPos);
        Vector3f nonRotatedDrag = DynamXGeometry.rotateVectorByQuaternion(dragForce, DynamXGeometry.inverseQuaternion(handledEntity.physicsRotation, QuaternionPool.get()));
        debugDragForces.get(i).set(nonRotatedDrag);
    }
}
