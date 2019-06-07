/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.bvh.migration;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nehon
 */
public class SkeletonMapping implements Savable {

    private Map<String, BoneMapping> mappings = new HashMap<String, BoneMapping>();

    public SkeletonMapping() {
    }
    
    public int size(){
        return mappings.size();
    }

    public void addMapping(BoneMapping mapping) {
        mappings.put(mapping.getTargetName(), mapping);
    }

    /**
     * Builds a BoneMapping with the given bone from the target skeleton and the
     * given bone from the source skeleton.
     *
     * @param targetBone the name of the bone from the target skeleton.
     * @param sourceBone the name of the bone from the source skeleton.
     */
    public BoneMapping map(String targetBone, String sourceBone) {
        BoneMapping mapping = new BoneMapping(targetBone, sourceBone);
        mappings.put(targetBone, mapping);
        return mapping;
    }

    /**
     * Builds a BoneMapping with the given bone from the target skeleton and the
     * given bone from the source skeleton. apply the given twist rotation to
     * the animation data
     *
     * @param targetBone the name of the bone from the target skeleton.
     * @param sourceBone the name of the bone from the source skeleton.
     * @param twist the twist rotation to apply to the animation data
     */
    public BoneMapping map(String targetBone, String sourceBone, Quaternion twist) {
        BoneMapping mapping = new BoneMapping(targetBone, sourceBone, twist);
        mappings.put(targetBone, mapping);
        return mapping;
    }

    /**
     * Builds a BoneMapping witht he given bone from the target skeleton and the
     * given bone from the source skeleton. apply the given twist rotation to
     * the animation data
     *
     * @param targetBone the name of the bone from the target skeleton.
     * @param sourceBone the name of the bone from the source skeleton.
     * @param twistAngle the twist rotation angle to apply to the animation data
     * @param twistAxis the twist rotation axis to apply to the animation data
     */
    public BoneMapping map(String targetBone, String sourceBone, float twistAngle, Vector3f twistAxis) {
        BoneMapping mapping = new BoneMapping(targetBone, sourceBone, twistAngle, twistAxis);
        mappings.put(targetBone, mapping);
        return mapping;
    }

    /**
     * Builds a BoneMapping witht he given bone from the target skeleton
     *
     * @param targetBone the name of the bone from the target skeleton.
     *
     */
    public BoneMapping map(String targetBone) {
        BoneMapping mapping = new BoneMapping(targetBone);
        mappings.put(targetBone, mapping);
        return mapping;
    }

    /**
     * Builds a BoneMapping witht he given bone from the target skeleton apply
     * the given twist rotation to the animation data
     *
     * @param targetBone the name of the bone from the target skeleton.
     * @param twist the twist rotation to apply to the animation data
     */
    public BoneMapping map(String targetBone, Quaternion twist) {
        BoneMapping mapping = new BoneMapping(targetBone, twist);
        mappings.put(targetBone, mapping);
        return mapping;
    }

    /**
     * Builds a BoneMapping witht he given bone from the target skeleton apply
     * the given twist rotation to the animation data
     *
     * @param targetBone the name of the bone from the target skeleton.
     * @param twistAngle the twist rotation angle to apply to the animation data
     * @param twistAxis the twist rotation axis to apply to the animation data
     */
    public BoneMapping map(String targetBone, float twistAngle, Vector3f twistAxis) {
        BoneMapping mapping = new BoneMapping(targetBone, twistAngle, twistAxis);
        mappings.put(targetBone, mapping);
        return mapping;
    }

    public BoneMapping get(String targetBoneName) {
        return mappings.get(targetBoneName);
    }

    public BoneMapping getInv(String srcBoneName) {
        for (BoneMapping bm : mappings.values()) {
            if (bm.getSourceNames() == null || bm.getSourceNames().isEmpty()) {
                continue;
            }
            if (bm.getSourceNames().get(0).equals(srcBoneName)) {
                return bm;
            }
        }
        return null;
    }

    /**
     * Generate an inverse for this mapping.
     *
     * @return a new mapping
     */
    public SkeletonMapping inverse() {
        SkeletonMapping result = new SkeletonMapping();
        for (BoneMapping boneMapping : mappings.values()) {
            Quaternion twist = boneMapping.getTwist();
            Quaternion inverseTwist = twist.inverse();
            String targetName = boneMapping.getTargetName();

            List<String> sourceNames = boneMapping.getSourceNames();
            for (String sourceName : sourceNames) {
                result.map(sourceName, targetName, inverseTwist);
            }
        }

        return result;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.writeStringSavableMap(mappings, "mappings", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        mappings = (Map<String, BoneMapping>) ic.readStringSavableMap("mappings", new HashMap<String, BoneMapping>());
    }
}
