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
        initHud(animName + ".bvh");
        createLights();

        Node model = (Node) assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        //    Node model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        rootNode.attachChild(model);
        AnimComposer control = model.getControl(AnimComposer.class);
        SkinningControl sinbadSkinCtrl = model.getControl(SkinningControl.class);

        /*
        Node model2 = (Node) assetManager.loadModel("Models/Jaime/Jaime.j3o");
        rootNode.attachChild(model2);
        AnimComposer control2 = model2.getControl(AnimComposer.class);
         */
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
        //sinbadSkinCtrl.getArmature().applyBindPose();
//        final AnimChannel channel = control.createChannel();
//        control.addListener(this);
        //no rotation correction
        
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

        //   SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton(), assetManager, false);
//        debugAppState.addSkeleton("SinbadSkeleton", control.getSkeleton(), false);
        //FIXME it shouldn't be model here as first argument.
        SkeletonMapping skMap2 = null;

        try {
            /*
            MappingCtx targetM = SkeletonMappingUtil.autoSkeletonMappingAsHuman(control.getSkeleton());
            MappingCtx srcM = SkeletonMappingUtil.autoSkeletonMappingAsHuman(animData.getSkeleton());
            MappingCtx targetM2 = SkeletonMappingUtil.autoSkeletonMappingAsHuman(control2.getSkeleton());
            Object o = targetM.getAllBone().get("Waist");
             */
 /*
            skMap2 = SkeletonMappingUtil.mapping(animData.getSkeleton(), srcM, targetM);
            //rotation for gegina
            skMap2.get("Root").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Y));
            //skMap2.map("Stomach", "Chest");
            skMap2.get("Stomach").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Y));
            skMap2.get("Neck").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Y));
            skMap2.get("Head").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Y));
            skMap2.get("Waist").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X));

            skMap2.get("Clavicle.L").setTwist(new Quaternion().fromAngleAxis(-HALF_PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.get("Clavicle.R").setTwist(new Quaternion().fromAngleAxis(HALF_PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));

            skMap2.get("Humerus.L").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.map("Humerus.L", "aaa");
            skMap2.get("Humerus.R").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.map("Humerus.R", "aaa");
            skMap2.get("Ulna.L").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.map("Ulna.L", "aaa");
            skMap2.get("Ulna.R").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.map("Ulna.R", "aaa");
            skMap2.get("Hand.L").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.map("Hand.L", "aaa");
            skMap2.get("Hand.R").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_Z).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            skMap2.map("Hand.R", "aaa");

            skMap2.get("Thigh.L").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
//            skMap2.map("Thigh.L", "aaa");
            skMap2.get("Thigh.R").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            //skMap2.map("Thigh.R", "aaa");
            skMap2.get("Calf.L").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
//            skMap2.map("Calf.L", "aaa");
            skMap2.get("Calf.R").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            //skMap2.map("Calf.R", "aaa");
            skMap2.get("Foot.L").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
//            skMap2.map("Foot.L", "aaa");
            skMap2.get("Foot.R").setTwist(new Quaternion().fromAngleAxis(PI, UNIT_X).mult(new Quaternion().fromAngleAxis(PI, UNIT_Y)));
            //skMap2.map("Foot.R", "aaa");
            //rotation for gegina
             */
            //dbg pring
            /*
            List<String> boneNames = new ArrayList<>();
            boneNames.add("Root");
            boneNames.add("Stomach");
            boneNames.add("Neck");
            boneNames.add("Head");
            boneNames.add("Clavicle.L");
            boneNames.add("Clavicle.R");
            boneNames.add("Humerus.L");
            boneNames.add("Humerus.R");
            boneNames.add("Ulna.L");
            boneNames.add("Ulna.R");
            boneNames.add("Hand.L");
            boneNames.add("Hand.R");
            boneNames.add("Thigh.L");
            boneNames.add("Thigh.R");
            boneNames.add("Calf.L");
            boneNames.add("Calf.R");
            boneNames.add("Foot.L");
            boneNames.add("Foot.R");

            for (String boneName : boneNames) {
                BoneMapping bm1 = skMap.get(boneName);
                BoneMapping bm2 = skMap2.get(boneName);
                if (bm1 == null || bm2 == null) {
                    if (bm1 == null && bm2 == null) {
                        continue;
                    }
                    System.out.println("bm1 or bm2 is null");
                    continue;
                }
                if (!bm1.getSourceNames().get(0).equals(bm2.getSourceNames().get(0))) {
                    System.out.println("not equa name");
                    continue;
                }
                if (!bm1.getTwist().equals(bm2.getTwist())) {
                    System.out.println("not equa twist");
                }

            }
             */
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        /*
        SkeletonDebugAppState3_3 debugAppState = new SkeletonDebugAppState3_3();
        stateManager.attach(debugAppState);
        debugAppState.addSkeleton("SinbadSkeleton", control.getSkeleton(), false);
        //FIXME it shouldn't be model here as first argument.
        control.addAnim(BVHUtils.reTarget(model, model, animData.getAnimation(), animData.getSkeleton(), animData.getTimePerFrame(), skMap2, false));
        final AnimChannel channel = control.createChannel();
        control.addListener(this);
         */
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

    private void initHud(String text) {
        /**
         * Write text on the screen (HUD)
         */
        // guiNode.detachAllChildren();
//        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
//        BitmapText helloText = new BitmapText(guiFont, false);
//        helloText.setSize(guiFont.getCharSet().getRenderedSize());
//        helloText.setText(text);
//        helloText.setLocalTranslation(settings.getWidth() / 2 - helloText.getLineWidth() / 2, helloText.getLineHeight(), 0);
//        guiNode.attachChild(helloText);
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
//        if (animName.equals("ballerina")) {
//            channel.setAnim(poseAnim, 0.50f);
//            channel.setLoopMode(LoopMode.Loop);
//            channel.setSpeed(1f);
//        }
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    private AnimChannel createAnimSkeleton(BVHAnimData animData, float scale, String animName) {
        //      SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", animData.getSkeleton(), assetManager, true);

        //  
        /*
        SkeletonDebugger3_3 skeletonDebug = stateManager.getState(SkeletonDebugAppState3_3.class).addSkeleton("skeleton", animData.getSkeleton(), true);
        skeletonDebug.setLocalScale(scale);

        skeletonDebug.setLocalTranslation(7, 0, 0);

        HashMap<String, Animation> anims = new HashMap<String, Animation>();
        anims.put(animData.getAnimation().getName(), animData.getAnimation());

        AnimControl ctrl = new AnimControl(animData.getSkeleton());
        ctrl.setAnimations(anims);
        skeletonDebug.addControl(ctrl);

        for (String anim : ctrl.getAnimationNames()) {
            System.out.println(anim);
        }

        ctrl.addListener(this);
        AnimChannel channel = ctrl.createChannel();
//        channel.setAnim(animName);
//        channel.setLoopMode(LoopMode.Cycle);
//        channel.setSpeed(1f);
        return channel;
         */
        return null;//dbg 
    }

    private void createBindPose(Mesh mesh) {
        VertexBuffer pos = mesh.getBuffer(Type.Position);
        if (pos == null || mesh.getBuffer(Type.BoneIndex) == null) {
            // ignore, this mesh doesn't have positional data
            // or it doesn't have bone-vertex assignments, so its not animated
            return;
        }

        VertexBuffer bindPos = new VertexBuffer(Type.BindPosePosition);
        bindPos.setupData(Usage.CpuOnly,
                3,
                Format.Float,
                BufferUtils.clone(pos.getData()));
        mesh.setBuffer(bindPos);

        // XXX: note that this method also sets stream mode
        // so that animation is faster. this is not needed for hardware skinning
        pos.setUsage(Usage.Stream);

        VertexBuffer norm = mesh.getBuffer(Type.Normal);
        if (norm != null) {
            VertexBuffer bindNorm = new VertexBuffer(Type.BindPoseNormal);
            bindNorm.setupData(Usage.CpuOnly,
                    3,
                    Format.Float,
                    BufferUtils.clone(norm.getData()));
            mesh.setBuffer(bindNorm);
            norm.setUsage(Usage.Stream);
        }
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
