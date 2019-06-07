/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3test.bvh;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.Armature;
import com.jme3.anim.SkinningControl;
import com.jme3.app.SimpleApplication;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.debug.*;
import java.util.logging.Logger;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.scene.debug.Arrow;
import com.jme3.util.BufferUtils;
import static com.jme3.math.FastMath.*;
import static com.jme3.math.Vector3f.*;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3test.bvh.migration.BVHAnimData;
import jme3test.bvh.migration.BVHLoader;
import jme3test.bvh.migration.BVHUtils;
import jme3test.bvh.migration.BoneMapping;
import jme3test.bvh.migration.SkeletonMapping;

public class TestAutoRetarget extends SimpleApplication {

    private static final Logger LOGGER = Logger.getLogger(TestAutoRetarget.class.getName());

    public static void main(String[] args) {
        TestAutoRetarget app = new TestAutoRetarget();
        app.start();

    }
    private final String poseAnim = "IdleBase";

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));

        // final String animName="37_01";
        assetManager.registerLoader(BVHLoader.class, "bvh", "BVH");
        final String animName = "ballerina";
        BVHAnimData animData = (BVHAnimData) assetManager.loadAsset("Animations/" + animName + ".bvh");
        createLights();

        Node model = (Node) assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        rootNode.attachChild(model);
        AnimComposer control = model.getControl(AnimComposer.class);
        SkinningControl sinbadSkinCtrl = model.getControl(SkinningControl.class);

        float targetHeight = ((BoundingBox) model.getWorldBound()).getYExtent();//BVHUtils.getSkeletonHeight(control.getSkeleton());
        float sourceHeight = BVHUtils.getSkeletonHeight(animData.getSkeleton());
        float ratio = targetHeight / sourceHeight;
        System.out.println(((BoundingBox) model.getWorldBound()).getYExtent());
        //final AnimChannel animChannel = createAnimSkeleton(animData, ratio, animName);

        SkeletonMapping skMap = new SkeletonMapping();
        //Sinbad - Ballerina
        /*
        skMap.map("Root", "Hips");
        skMap.map("Stomach", "Chest");
        skMap.map("Neck", "Neck");
        skMap.map("Head", "Head");
        skMap.map("Clavicle.L", "LeftCollar", new Quaternion().fromAngleAxis(-HALF_PI, UNIT_Z));
        skMap.map("Clavicle.R", "RightCollar", new Quaternion().fromAngleAxis(HALF_PI, UNIT_Z));
        skMap.map("Humerus.L", "LeftUpArm", new Quaternion().fromAngleAxis(PI, UNIT_Z));
        skMap.map("Humerus.R", "RightUpArm", new Quaternion().fromAngleAxis(-PI, UNIT_Z));
        skMap.map("Ulna.L", "LeftLowArm", new Quaternion().fromAngleAxis(PI, UNIT_Z));
        skMap.map("Ulna.R", "RightLowArm", new Quaternion().fromAngleAxis(PI, UNIT_Z));
        skMap.map("Hand.L", "LeftHand", new Quaternion().fromAngleAxis(PI, UNIT_Z));
        skMap.map("Hand.R", "RightHand", new Quaternion().fromAngleAxis(PI, UNIT_Z));
        skMap.map("Thigh.L", "LeftUpLeg", new Quaternion().fromAngleAxis(HALF_PI / 3, UNIT_Z));
        skMap.map("Thigh.R", "RightUpLeg", new Quaternion().fromAngleAxis(-HALF_PI / 3, UNIT_Z));
        skMap.map("Calf.L", "LeftLowLeg", new Quaternion().fromAngleAxis(HALF_PI / 3, UNIT_Z));
        skMap.map("Calf.R", "RightLowLeg", new Quaternion().fromAngleAxis(-HALF_PI / 3, UNIT_Z));
        skMap.map("Foot.L", "LeftFoot", new Quaternion().fromAngleAxis(HALF_PI / 3, UNIT_X));
        skMap.map("Foot.R", "RightFoot", new Quaternion().fromAngleAxis(HALF_PI / 3, UNIT_X));
*/
        
        skMap.map("Root", "Hips", PI, UNIT_Y);
        skMap.map("Stomach", "Chest", PI, UNIT_Y);
        skMap.map("Neck", "Neck", PI, UNIT_Y);
        skMap.map("Head", "Head", PI, UNIT_Y);
        skMap.map("Clavicle.L", "LeftCollar", new Quaternion().fromAngleAxis(-HALF_PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Clavicle.R", "RightCollar", new Quaternion().fromAngleAxis(HALF_PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Humerus.L", "LeftUpArm", new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Humerus.R", "RightUpArm", new Quaternion().fromAngleAxis(-PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Ulna.L", "LeftLowArm", new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(-HALF_PI, UNIT_Y)));
        skMap.map("Ulna.R", "RightLowArm", new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(HALF_PI, UNIT_Y)));
        skMap.map("Hand.L", "LeftHand", new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Hand.R", "RightHand", new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(HALF_PI, UNIT_Y)));
        skMap.map("Thigh.L", "LeftUpLeg", new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Thigh.R", "RightUpLeg", new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Calf.L", "LeftLowLeg", new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Calf.R", "RightLowLeg", new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Foot.L", "LeftFoot", new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
        skMap.map("Foot.R", "RightFoot", new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
         
        control.addAnimClip(BVHUtils.reTarget2(model, model, sinbadSkinCtrl.getArmature(), animData.getAnimation(), animData.getSkeleton(), animData.getTimePerFrame(), skMap, false));
        control.setCurrentAction(animName);

        SkeletonMapping skMap2 = null;

        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, inputManager);
        chaseCam.setDefaultHorizontalRotation(HALF_PI);
        chaseCam.setMinVerticalRotation(-HALF_PI + 0.01f);
        chaseCam.setInvertVerticalAxis(true);

        final Node chaseCamTarget = new Node("ChaseCamTarget");
        rootNode.attachChild(chaseCamTarget);
        chaseCamTarget.addControl(chaseCam);
        chaseCamTarget.setLocalTranslation(model.getLocalTranslation());

    }

    private boolean pan = false;

    private void createLights() {
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(al);
    }

    private Geometry createAxis(String name, Vector3f extend, ColorRGBA color) {
        Arrow axis = new Arrow(extend);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //   m.getAdditionalRenderState().setDepthWrite(false);
        m.setColor("Color", color);
        Geometry geo = new Geometry(name, axis);
        geo.setMaterial(m);
        return geo;
    }

    protected void setUpCamInput(Node model) {
        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, inputManager);
        chaseCam.setDefaultHorizontalRotation(HALF_PI);
        chaseCam.setMinVerticalRotation(-HALF_PI + 0.01f);
        chaseCam.setInvertVerticalAxis(true);

        final Node chaseCamTarget = new Node("ChaseCamTarget");
        rootNode.attachChild(chaseCamTarget);
        chaseCamTarget.addControl(chaseCam);
        chaseCamTarget.setLocalTranslation(model.getLocalTranslation());

        inputManager.addListener(new ActionListener() {
            public void onAction(String name, boolean isPressed, float tpf) {
                if (name.equals("Pan")) {
                    if (isPressed) {
                        pan = true;
                    } else {
                        pan = false;
                    }
                }
            }
        }, "Pan");
        inputManager.addMapping("Pan", new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        inputManager.addListener(new AnalogListener() {
            public void onAnalog(String name, float value, float tpf) {
                if (pan) {
                    value *= 10f;
                    TempVars vars = TempVars.get();
                    if (name.equals("mouseMoveDown")) {
                        chaseCamTarget.move(cam.getUp().mult(-value, vars.vect1));
                    }
                    if (name.equals("mouseMoveUp")) {
                        chaseCamTarget.move(cam.getUp().mult(value, vars.vect1));
                    }
                    if (name.equals("mouseMoveLeft")) {
                        chaseCamTarget.move(cam.getLeft().mult(value, vars.vect1));
                    }
                    if (name.equals("mouseMoveRight")) {
                        chaseCamTarget.move(cam.getLeft().mult(-value, vars.vect1));
                    }
                    vars.release();
                }
            }
        }, "mouseMoveDown", "mouseMoveUp", "mouseMoveLeft", "mouseMoveRight");
        inputManager.addMapping("mouseMoveDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("mouseMoveUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("mouseMoveLeft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("mouseMoveRight", new MouseAxisTrigger(MouseInput.AXIS_X, true));

    }

}
