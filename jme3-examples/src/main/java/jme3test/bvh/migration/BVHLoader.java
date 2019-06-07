/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.bvh.migration;

import com.jme3.anim.AnimClip;
import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.TransformTrack;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 *
 * @author Nehon
 */
public class BVHLoader implements AssetLoader {

    private AssetManager owner;
    private Scanner scan;
    private String fileName;
    BVHAnimData data;
    BVHAnimation animation;

    public Object load(AssetInfo info) throws IOException {
        this.owner = info.getManager();
        fileName = info.getKey().getName();

        InputStream in = info.openStream();
        try {
            scan = new Scanner(in);
            scan.useLocale(Locale.US);
            this.fileName = info.getKey().getName();
            loadFromScanner();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return data;
    }

    private BVHBone readBone(String name) {
        BVHBone bone = new BVHBone(name);
//        if(!name.equals("Site")){
//            System.out.println(name);
//        }
        String token = scan.next();
        if (token.equals("{")) {
            token = scan.next();
            if (token.equals("OFFSET")) {
                bone.getOffset().setX(scan.nextFloat());
                bone.getOffset().setY(scan.nextFloat());
                bone.getOffset().setZ(scan.nextFloat());
                token = scan.next();
            }
            if (token.equals("CHANNELS")) {
                bone.setChannels(new ArrayList<BVHChannel>());
                int nbChan = scan.nextInt();
                for (int i = 0; i < nbChan; i++) {
                    bone.getChannels().add(new BVHChannel(scan.next()));
                }
                token = scan.next();
            }
            while (token.equals("JOINT") || token.equals("End")) {
                if (bone.getChildren() == null) {
                    bone.setChildren(new ArrayList<BVHBone>());
                }
                bone.getChildren().add(readBone(scan.next()));
                token = scan.next();
            }
        }

        return bone;
    }

    private void loadFromScanner() throws IOException {

        animation = new BVHAnimation();
        String token = scan.next();
        if (token.equals("HIERARCHY")) {
            token = scan.next();
            if (token.equals("ROOT")) {
                token = scan.next();
                animation.setHierarchy(readBone(token));
                token = scan.next();
            }
        }
        if (token.equals("MOTION")) {
            scan.next();
            animation.setNbFrames(scan.nextInt());
            scan.next();
            scan.next();
            animation.setFrameTime(scan.nextFloat());
            for (int i = 0; i < animation.getNbFrames(); i++) {
                readChanelsValue(animation.getHierarchy());
            }

        }

        //    System.out.println(animation.getHierarchy().toString());
        compileData();
    }

    private void compileData() {
        Joint[] bones = new Joint[animation.getHierarchy().getNbBones()];
        index = 0;
        TransformTrack[] tracks = new TransformTrack[animation.getHierarchy().getNbBones()];
        populateBoneList(bones, tracks, animation.getHierarchy(), null);

        Armature skeleton = new Armature(bones);
        skeleton.saveBindPose();
        String animName = fileName.substring(fileName.lastIndexOf("/") + 1).replaceAll(".bvh", "");
        //TODO AnimClip has no setter of length 
        float animLength = animation.getFrameTime() * animation.getNbFrames();
        AnimClip boneAnimation = new AnimClip(animName);
        boneAnimation.setTracks(tracks);
        data = new BVHAnimData(skeleton, boneAnimation, animation.getFrameTime());
    }
    int index = 0;

    private void populateBoneList(Joint[] bones, TransformTrack[] tracks, BVHBone hierarchy, Joint parent) {
//        if (hierarchy.getName().equals("Site")) {
//            return;
//        }
        Joint joint = new Joint(hierarchy.getName());
        joint.setLocalTranslation(hierarchy.getOffset());
        joint.setLocalRotation(new Quaternion().IDENTITY);
        joint.setLocalScale(Vector3f.UNIT_XYZ);
        joint.setId(index);

        if (parent != null) {
            parent.addChild(joint);
        }
        bones[index] = joint;
        tracks[index] = getBoneTrack(joint, hierarchy);
        index++;
        if (hierarchy.getChildren() != null) {
            for (BVHBone bVHBone : hierarchy.getChildren()) {
                populateBoneList(bones, tracks, bVHBone, joint);
            }
        }

    }

    private TransformTrack getBoneTrack(Joint joint, BVHBone bone) {
        float[] times = new float[animation.getNbFrames()];
        Vector3f[] translations = new Vector3f[animation.getNbFrames()];
        Quaternion[] rotations = new Quaternion[animation.getNbFrames()];
        Vector3f[] scales = new Vector3f[animation.getNbFrames()];
        for (int i = 0; i < scales.length; i++) {
            scales[i] = Vector3f.UNIT_XYZ;
        }
        float time = 0;

        Quaternion rx = new Quaternion();
        Quaternion ry = new Quaternion();
        Quaternion rz = new Quaternion();
        for (int i = 0; i < animation.getNbFrames(); i++) {
            times[i] = time;

            Vector3f t = new Vector3f(Vector3f.ZERO);
            Quaternion r = new Quaternion(Quaternion.IDENTITY);
            rx.set(Quaternion.IDENTITY);
            ry.set(Quaternion.IDENTITY);
            rz.set(Quaternion.IDENTITY);
            if (bone.getChannels() != null) {
                for (BVHChannel bVHChannel : bone.getChannels()) {
                    if (bVHChannel.getName().equals(BVHChannel.BVH_CHANNEL_X_POSITION)) {
                        t.setX(bVHChannel.getValues().get(i));
                    }
                    if (bVHChannel.getName().equals(BVHChannel.BVH_CHANNEL_Y_POSITION)) {
                        t.setY(bVHChannel.getValues().get(i));
                    }
                    if (bVHChannel.getName().equals(BVHChannel.BVH_CHANNEL_Z_POSITION)) {
                        t.setZ(bVHChannel.getValues().get(i));
                    }
                    //https://github.com/Nehon/bvhretarget/pull/5/files
                    if (bVHChannel.getName().equals(BVHChannel.BVH_CHANNEL_X_ROTATION)) {
                        rx.fromAngleAxis((bVHChannel.getValues().get(i)) * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
                        r.multLocal(rx);
                    }
                    if (bVHChannel.getName().equals(BVHChannel.BVH_CHANNEL_Y_ROTATION)) {
                        ry.fromAngleAxis((bVHChannel.getValues().get(i)) * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
                        r.multLocal(ry);
                    }
                    if (bVHChannel.getName().equals(BVHChannel.BVH_CHANNEL_Z_ROTATION)) {
                        rz.fromAngleAxis((bVHChannel.getValues().get(i)) * FastMath.DEG_TO_RAD, Vector3f.UNIT_Z);
                        r.multLocal(rz);
                    }
                }
            }
            translations[i] = t;
            rotations[i] = r;
            /*            
            if(!Quaternion.IDENTITY.equals(r)){
                rotations[i] = r;
            }
             */
//            if (i == 1) {
//                float[] angles = new float[3];
//                r.toAngles(angles);
//                System.out.println("Computed rotation : ");
//                System.out.println("rz : " + angles[2] * FastMath.RAD_TO_DEG);
//                System.out.println("rx : " + angles[0] * FastMath.RAD_TO_DEG);
//                System.out.println("ry : " + angles[1] * FastMath.RAD_TO_DEG);
//
//            }

            time += animation.getFrameTime();
        }
//        System.out.println("bone : " + bone.getName());
//        System.out.println("times : ");
//        for (int i = 0; i < times.length; i++) {
//            System.out.print(times[i]+", ");
//
//        }
//        System.out.println();
//        System.out.println("translations : ");
//         for (int i = 0; i < translations.length; i++) {
//            System.out.print(translations[i]+", ");
//
//        }
//        System.out.println();
//        System.out.println("rotations : ");
//         for (int i = 0; i < rotations.length; i++) {
//            System.out.print(rotations[i]+", ");
//
//        }
//        System.out.println();

        return new TransformTrack(joint, times, translations, rotations, null);
    }

    private void readChanelsValue(BVHBone bone) {
        if (bone.getChannels() != null) {
            for (BVHChannel bvhChannel : bone.getChannels()) {
                if (bvhChannel.getValues() == null) {
                    bvhChannel.setValues(new ArrayList<Float>());
                }
                bvhChannel.getValues().add(scan.nextFloat());
            }
            for (BVHBone b : bone.getChildren()) {
                readChanelsValue(b);
            }
        }
    }
}
