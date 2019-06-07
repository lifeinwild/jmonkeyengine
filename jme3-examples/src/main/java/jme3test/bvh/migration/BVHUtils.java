/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.bvh.migration;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.TransformTrack;
import com.jme3.anim.interpolator.AnimInterpolators;
import com.jme3.anim.interpolator.FrameInterpolator;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nehon
 */
public class BVHUtils {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(BVHUtils.class.getName());
    private static final Vector3f DEFAULT_SCALE = new Vector3f(Vector3f.UNIT_XYZ);

    public static AnimClip reTarget2(Spatial src, Spatial target, Armature targetArmature, AnimClip srcAnim, Armature srcArmature, float timePerFrame, SkeletonMapping skMap, boolean skipFirstKey) {
        AnimClip r = new AnimClip(srcAnim.getName());
        AnimTrack[] rTracks = new AnimTrack[skMap.size()];
        int frame = 0;
        for (AnimTrack t : srcAnim.getTracks()) {
            if (!(t instanceof TransformTrack)) {
                continue;
            }
            TransformTrack tt = (TransformTrack) t;
            if (!(tt.getTarget() instanceof Joint)) {
                continue;
            }
            Joint srcJ = (Joint) tt.getTarget();
            BoneMapping bm = skMap.getInv(srcJ.getName());
            if (bm == null) {
                System.out.println("BoneMapping not found " + srcJ.getName());
                continue;
            }
            Joint targetJ = targetArmature.getJoint(bm.getTargetName());

            TransformTrack rtt = new TransformTrack();
            //dbg start
            Vector3f[] translations = new Vector3f[tt.getTranslations().length];
            for (int j = 0; j < translations.length; j++) {
                translations[j] = Vector3f.ZERO;
            }

            rtt.setKeyframes(tt.getTimes(), translations, tt.getRotations(), tt.getScales());
            //dbg end
            rtt.setTarget(targetJ);
            rTracks[frame++] = rtt;
        }
        r.setTracks(rTracks);

        printAnimClip(r);
        return r;
    }

    public static AnimClip reTarget(Spatial src, Spatial target, Armature targetArmature, AnimClip srcAnim, Armature srcArmature, SkeletonMapping skMap, boolean skipFirstKey) {
        TransformTrack track = getFirstBoneTrack(srcAnim);
        if (track == null) {
            throw new IllegalArgumentException("Animation must contain a boneTrack to be retargeted");
        }
        float timePerFrame = track.getTimes().length / (float) srcAnim.getLength();
        return reTarget(src, target, targetArmature, srcAnim, srcArmature, timePerFrame, skMap, skipFirstKey);
    }

    /**
     * This method was created instead of Joint#getBindTransform(). That
     * decision may not be fully integrated. by lifeinwild
     *
     * @param ori bind pose state
     * @return bind pose armature
     */
    public static Armature cloneArmature(Armature ori) {
        Joint[] joints = new Joint[ori.getJointCount()];
        for (Joint root : ori.getRoots()) {
            Joint rootClone = clone(root);
            joints[rootClone.getId()] = rootClone;
            recursiveClone(root, rootClone, joints);
        }
        Armature r = new Armature(joints);
        return r;
    }

    /**
     * @param ori
     * @return clone Joint without parent
     */
    private static Joint clone(Joint ori) {
        Joint rj = new Joint(ori.getName());
        rj.setId(ori.getId());
        rj.setInverseModelBindMatrix(ori.getInverseModelBindMatrix());
        rj.setLocalRotation(ori.getLocalRotation());
        rj.setLocalScale(ori.getLocalScale());
        rj.setLocalTranslation(ori.getLocalTranslation());
        return rj;
    }

    private static void recursiveClone(Joint parent, Joint parentClone, Joint[] joints) {
        for (Joint c : parent.getChildren()) {
            Joint cClone = clone(c);
            parentClone.addChild(cClone);
            joints[cClone.getId()] = cClone;
            recursiveClone(c, cClone, joints);
        }
    }

    public static AnimClip reTarget(Spatial src, Spatial target, Armature targetArmature, AnimClip srcAnim, Armature srcArmature, float timePerFrame, SkeletonMapping skMap, boolean skipFirstKey) {
        int start = skipFirstKey ? 1 : 0;

//        Animation sourceAnimation = sourceData.getAnimation();
//        Skeleton sourceSkeleton = sourceData.getSkeleton();
        AnimClip rAnim = new AnimClip(srcAnim.getName());

        targetArmature.update();
        float targetHeight = ((BoundingBox) target.getWorldBound()).getYExtent() / target.getWorldScale().y;
        float sourceHeight = ((BoundingBox) src.getWorldBound()).getYExtent() / src.getWorldScale().y;
        float targetWidth = ((BoundingBox) target.getWorldBound()).getXExtent() / target.getWorldScale().x;
        float sourceWidth = ((BoundingBox) src.getWorldBound()).getXExtent() / src.getWorldScale().x;
        float targetDepth = ((BoundingBox) target.getWorldBound()).getZExtent() / target.getWorldScale().z;
        float sourceDepth = ((BoundingBox) src.getWorldBound()).getZExtent() / src.getWorldScale().z;
        Vector3f ratio = new Vector3f(targetHeight / sourceHeight, targetWidth / sourceWidth, targetDepth / sourceDepth);
        ratio = Vector3f.UNIT_XYZ;
        System.out.println(ratio);

        Vector3f rootPos = new Vector3f();
        Quaternion rootRot = new Quaternion();
        /*
        //dbg
        for(Joint j : targetArmature.getJointList()){
            System.out.println(j.getName() + " " + j.getLocalTransform().getRotation());
        }
        //dbg
         */
        targetArmature.applyBindPose();
        //dbg start
        for (Joint j : targetArmature.getJointList()) {
            switch (j.getName()) {
                case "Head":
                case "Ulna.L":
                case "Humerus.L":
                case "Calf.R":
                case "Calf.L":
                case "Foot.L":
                case "Foot.R":
                case "Hand.L":
                case "Hand.R":
                case "Thigh.L":
                    j.getLocalRotation().negate();
                    break;
                default:
            }
            int aaa = 0;
        }
        //dbg end
        /*
        //dbg
        for(Joint j : targetArmature.getJointList()){
            System.out.println(j.getName() + " " + j.getLocalTransform().getRotation());
        }
        //dbg
         */
        Armature targetArmatureBind = cloneArmature(targetArmature);
        srcArmature.applyBindPose();
        Armature srcArmatureBind = cloneArmature(srcArmature);

        TransformTrack track = getFirstBoneTrack(srcAnim);
        if (track == null) {
            throw new IllegalArgumentException("Animation must contain a boneTrack to be retargeted");
        }
        int nbFrames = track.getTimes().length;
        Map<Joint, InnerTrack> tracks = new HashMap<>();

        float[] times = new float[nbFrames];
        System.arraycopy(track.getTimes(), 0, times, 0, nbFrames);
        //for each frame
        for (int frameId = 0; frameId < nbFrames; frameId++) {
            //applying animation for the frame to source skeleton so that model transforms are computed
            for (int i = 0; i < srcAnim.getTracks().length; i++) {
                AnimTrack srcTrackTmp = srcAnim.getTracks()[i];
                if (srcTrackTmp instanceof TransformTrack) {
                    TransformTrack srcJointTrack = (TransformTrack) srcTrackTmp;
                    if (!(srcJointTrack.getTarget() instanceof Joint)) {
                        continue;
                    }
                    Joint srcJointTmp = (Joint) srcJointTrack.getTarget();
                    Joint srcJoint = srcArmature.getJoint(srcJointTmp.getId());
                    //sourceBone.setUserControl(true);
                    // some anims doesn't have scale so using the default scale
                    Vector3f scale = DEFAULT_SCALE;
                    if (srcJointTrack.getScales() != null) {
                        scale = srcJointTrack.getScales()[frameId];
                    }
                    transFromBind(srcJoint, srcArmatureBind.getJoint(srcJoint.getId()).getLocalTransform(), new Transform(srcJointTrack.getTranslations()[frameId], srcJointTrack.getRotations()[frameId], scale));
                }
            }
            srcArmature.update();

            for (Joint bone : targetArmature.getRoots()) {
                computeTransforms(bone, srcArmature, targetArmature, targetArmatureBind, skMap, frameId, tracks, nbFrames, ratio, srcAnim);
            }
        }
        srcArmature.applyBindPose();
        TransformTrack[] boneTracks = new TransformTrack[tracks.size()];

        int i = 0;
        for (Joint j : tracks.keySet()) {
            InnerTrack it = tracks.get(j);
            TransformTrack bt = new TransformTrack(j, times.clone(), it.positions, it.rotations, it.scales);
            boneTracks[i] = bt;
            i++;
        }

        rAnim.setTracks(boneTracks);

        printAnimClip(rAnim);

        targetArmature.applyBindPose();

        return rAnim;
    }

    public static void printAnimClip(AnimClip a) {
        for (AnimTrack e : a.getTracks()) {
            TransformTrack tt = (TransformTrack) e;
            Joint j = (Joint) tt.getTarget();
            System.out.println(
                    j.getName() + "  " + j.getId()
                    + System.lineSeparator() + tt.getRotations()[0] + "  "
                    + System.lineSeparator() + (tt.getScales() == null ? "" : tt.getScales()[0]) + "  "
                    + System.lineSeparator() + (tt.getTranslations() == null ? "" : tt.getTranslations()[0]) + "  "
                    + System.lineSeparator() + j.getLocalRotation() + "  "
                    + System.lineSeparator() + j.getLocalScale() + "  "
                    + System.lineSeparator() + j.getLocalTranslation() + "  "
                    + System.lineSeparator() + j.getModelTransform().getRotation()
                    + System.lineSeparator() + j.getModelTransform().getScale()
                    + System.lineSeparator() + j.getModelTransform().getTranslation()
            );
        }
    }

    private static TransformTrack findBoneTrack(Joint j, AnimClip anim) {
        for (int i = 0; i < anim.getTracks().length; i++) {
            AnimTrack t = anim.getTracks()[i];
            if (t instanceof TransformTrack) {
                TransformTrack boneTrack = (TransformTrack) t;
                if (((TransformTrack) t).getTarget() == j) {
                    return boneTrack;
                }
            }
        }
        return null;
    }

    /**
     * replacement of Bone#setUserTransforms(Vector3f translation, Quaternion
     * rotation, Vector3f scale)
     *
     * @param targetBone
     * @param bind
     * @param tran
     */
    private static void transFromBind(Joint targetBone, Transform bind, Transform tran) {
        targetBone.setLocalTransform(bind);
        targetBone.getLocalTransform().getRotation().multLocal(tran.getRotation());
        targetBone.getLocalTransform().getTranslation().addLocal(tran.getTranslation());
        targetBone.getLocalTransform().getScale().multLocal(tran.getScale());
    }

    //this method recursively computes the transforms for each bone for a given 
    //frame, from a given sourceSkeleton (with bones updated to that frame)
    //
    //the Bind transforms are the transforms of the bone when it's in the rest 
    //pose (aka T pose). Wrongly called worldBindRotation in Bone implementation 
    //those transforms are expressed in model space
    //
    //the Model space transforms are the transforms of the bone in model space 
    //once the frame transforms has been applied
    private static void computeTransforms(Joint targetBone, Armature sourceSkeleton, Armature targetSkeleton, Armature targetSkeletonBind, SkeletonMapping skMap, int frameId, Map<Joint, InnerTrack> tracks, int animLength, Vector3f ratio, AnimClip anim) {
        //if (targetBone.getId() == 64) {
        //    int aa = 0;
        //}

        Joint targetBindBone = targetSkeletonBind.getJoint(targetBone.getId());
        BoneMapping mapping = skMap.get(targetBone.getName());
        Joint sourceBone = null;
        if (mapping != null) {
            sourceBone = sourceSkeleton.getJoint(mapping.getSourceNames().get(0));
        }

        Quaternion rootRot = new Quaternion();
        targetSkeleton.update();
        //we want the target bone to have the same model transforms as the source Bone (scaled to the correct ratio as models may not have the same scale)
        //the ratio only affects position
        if (sourceBone != null) {
            if (sourceBone.getParent() == null) {
                //case of a root bone, just combine the source model transforms with the inverse target bind transforms                
                InnerTrack t = getInnerTrack(targetBone, tracks, animLength);
                //scaling the modelPosition
                Vector3f scaledPos = sourceBone.getLocalTransform().getTranslation().mult(ratio);
                //subtract target's bind position to the source's scaled model position
                t.positions[frameId] = new Vector3f();//scaledPos.subtractLocal(targetBone.getBindPosition());
                // t.positions[frameId] = new Vector3f();
                //multiplying the source's model rotation witht the target's inverse bind rotation
                TempVars vars = TempVars.get();
                Quaternion q = vars.quat1.set(targetBindBone.getLocalRotation()).inverseLocal();
                t.rotations[frameId] = q.mult(sourceBone.getModelTransform().getRotation());
                rootRot.set(q);

                vars.release();

                //dividing by the target's bind scale
                t.scales[frameId] = sourceBone.getModelTransform().getScale().divide(targetBindBone.getLocalScale());
                //targetBone.setUserControl(true);
                transFromBind(targetBone, targetBindBone.getLocalTransform(), new Transform(t.positions[frameId], t.rotations[frameId], t.scales[frameId]));

                targetBone.updateModelTransforms();
                //targetBone.setUserControl(false);
            } else {
                //general case
                //Combine source model transforms with target's parent inverse model transform and inverse target's bind transforms   

                Joint parentBone = targetBone.getParent();
                InnerTrack t = getInnerTrack(targetBone, tracks, animLength);

                TransformTrack boneTrack = findBoneTrack(sourceBone, anim);
                if (boneTrack != null) {
                    Vector3f animPosition = boneTrack.getTranslations()[frameId];
                    t.positions[frameId] = animPosition.clone();
                } else {
                    t.positions[frameId] = new Vector3f();
                }
                t.positions[frameId] = new Vector3f();

                TempVars vars = TempVars.get();
//                // computing target's parent's inverse model rotation
                Quaternion inverseTargetParentModelRot = vars.quat1;
                inverseTargetParentModelRot.set(parentBone.getModelTransform().getRotation()).inverseLocal().normalizeLocal();
//
//                //ANIMATION POSITION
//                //first we aplly the ratio
//                Vector3f scaledPos = sourceBone.getModelSpacePosition().mult(ratio);
//                //Retrieving target's local pos then subtracting the target's bind pos
//                t.positions[frameId] = inverseTargetParentModelRot.mult(scaledPos)
//                        .multLocal(parentBone.getModelSpaceScale())
//                        .subtract(parentBone.getModelSpacePosition());
//                //made in 2 steps for the sake of readability
//                //here t.positions[frameId] is containing the target's local position (frame position regarding the parent bone).
//                //now we're subtracting target's bind position
//                t.positions[frameId].subtractLocal(targetBone.getBindPosition());
                // now t.positions[frameId] is what we are looking for.

                //ANIMATION ROTATION
                //Computing target's local rotation by multiplying source's model 
                //rotation with target's parent's inverse model rotation and multiplying 
                //with the target's inverse world bind rotation.
                //
                //The twist quaternion is here to fix the twist on Y axis some 
                //bones may have after the rotation in model space has been computed
                //For now the problem is worked around by some constant twist 
                //rotation that you set in the bone mapping.
                //This is probably a predictable behavior that could be detected 
                //and automatically corrected, but as for now I don't have a clue where it comes from.
                //Don't use inverseTargetParentModelRot as is after this point as the 
                //following line compromizes its value. multlocal is used instead of mult for obvious optimization reason.
                Quaternion targetLocalRot = inverseTargetParentModelRot.multLocal(sourceBone.getModelTransform().getRotation()).normalizeLocal();
                Quaternion targetInverseBindRotation = vars.quat2.set(targetBindBone.getLocalRotation()).inverseLocal().normalizeLocal();
                Quaternion twist = skMap.get(targetBone.getName()).getTwist();
                if (twist == null) {
                    twist = getTwist(targetBone);
                }
                //finally computing the animation rotation for the current frame. Note that the first "mult" instanciate a new Quaternion.
                t.rotations[frameId] = targetInverseBindRotation.mult(targetLocalRot).multLocal(twist).normalizeLocal();              //

                //releasing tempVars
                vars.release();

                //ANIMATION SCALE
                // dividing by the target's parent's model scale then dividing by the target's bind scale
                t.scales[frameId] = sourceBone.getModelTransform().getScale().divide(parentBone.getModelTransform().getScale()).divideLocal(targetBindBone.getLocalScale());

                //Applying the computed transforms for the current frame to the bone and updating its model transforms
//                targetBone.setUserControl(true);
                transFromBind(targetBone, targetBindBone.getLocalTransform(), new Transform(t.positions[frameId], t.rotations[frameId], t.scales[frameId]));
                targetBone.updateModelTransforms();
                //             targetBone.setUserControl(false);
            }
        }

        //recurse through children bones
        for (Joint childBone : targetBone.getChildren()) {
            computeTransforms(childBone, sourceSkeleton, targetSkeleton, targetSkeletonBind, skMap, frameId, tracks, animLength, ratio, anim);
        }
    }

    private static Quaternion getTwist(Joint targetBone) {
        /*
        if(targetBone.getBindPosition().x){
            
        }
         */
        return null;
    }

    private static InnerTrack getInnerTrack(Joint j, Map<Joint, InnerTrack> tracks, int length) {
        InnerTrack t = tracks.get(j);
        if (t == null) {
            t = new InnerTrack(length);
            tracks.put(j, t);
        }
        return t;
    }

    private static class InnerTrack {

        Vector3f[] positions;
        Quaternion[] rotations;
        Vector3f[] scales;

        public InnerTrack(int length) {
            positions = new Vector3f[length];
            rotations = new Quaternion[length];
            scales = new Vector3f[length];
        }
    }

    public static float getSkeletonHeight(Armature targetSkeleton) {
        float maxy = -100000;
        float miny = +100000;
        targetSkeleton.applyBindPose();
        targetSkeleton.update();

        for (int i = 0; i < targetSkeleton.getJointCount(); i++) {
            Joint bone = targetSkeleton.getJoint(i);
            if (bone.getModelTransform().getTranslation().y > maxy) {
                maxy = bone.getModelTransform().getTranslation().y;
            }
            if (bone.getModelTransform().getTranslation().y < miny) {
                miny = bone.getModelTransform().getTranslation().y;
                //System.out.println(bone.getName() + " " + miny);
            }
        }
        //System.out.println(maxy - miny);
        return maxy - miny;
    }

    private static void outPutRotation(Quaternion q) {

        float[] angles = new float[3];
        q.toAngles(angles);

        System.out.println("rotation x: " + angles[0] * FastMath.RAD_TO_DEG);
        System.out.println("rotation Y: " + angles[1] * FastMath.RAD_TO_DEG);
        System.out.println("rotation Z: " + angles[2] * FastMath.RAD_TO_DEG);

    }

    private static Quaternion invert(Quaternion q) {

        float[] angles = new float[3];
        q.toAngles(angles);

        angles[0] = -angles[0];
        angles[1] = -angles[1];
        angles[2] = -angles[2];
        return new Quaternion().fromAngles(angles);
    }

    public static TransformTrack getFirstBoneTrack(AnimClip animation) {
        for (AnimTrack track : animation.getTracks()) {
            if (track instanceof TransformTrack) {
                return (TransformTrack) track;
            }
        }
        return null;
    }
}
