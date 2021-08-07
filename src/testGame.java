import engine.LWJGLRenderer;
import engine.Render;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;

public class testGame {
    private static final double MOUSE_SENSITIVITY = 100;
    private static final Vector3f cameraInc = new Vector3f();
    private static final Vector3f cameraPosition = new Vector3f();
    private static final Vector3f cameraRotation = new Vector3f();

    private static ArrayList<Integer> spawnedEntities = new ArrayList<>();

    private static final Runtime jre = Runtime.getRuntime();

    public static void main(String[] args){
        Render render = new LWJGLRenderer();
        if(!render.init("A test of Voxelesque engine")){
            System.err.println(render.getErrors());
            System.exit(-1);
        }
        render.setFov((float)Math.toRadians(90));
        //load "assets"
        int grassBlockModel = render.loadVEMFModel("/home/bluesillybeard/IdeaProjects/Voxelesque/src/test2.vemf0");
        System.err.println(render.getErrors()); //i'm too lazy to add an if statement lol
        int normalShader = render.loadShader("/home/bluesillybeard/IdeaProjects/Voxelesque/src/engine/");
        System.err.println(render.getErrors()); //i'm too lazy to add an if statement lol
        int crazyShader = render.loadShader("/home/bluesillybeard/IdeaProjects/Voxelesque/src/engine/silly");
        System.err.println(render.getErrors()); //i'm too lazy to add an if statement lol
        int guiShader = render.loadShader("/home/bluesillybeard/IdeaProjects/Voxelesque/src/engine/gui");
        System.err.println(render.getErrors()); //i'm too lazy to add an if statement lol

        int entity1 = render.addEntity(grassBlockModel, normalShader, new float[]{0f, 0f, 0f, 0f,  1f, 0.5f, 0.5f, 1.0f, 0.5f});
        int entity2 = render.addEntity(grassBlockModel, normalShader, new float[]{0f, 0f, -2f, 1f,  1f, 0f,   1.0f, 0.5f, 0.5f});
        int entity3 = render.addEntity(grassBlockModel, normalShader, new float[]{0f, 0f, -4f, 0f,  0f, 1f,   0.5f, 0.5f, 1.0f});
        int entity4 = render.addEntity(grassBlockModel, crazyShader, new float[] {0f, 0f, -6f, 0f,  2f, 0f,   0.5f, 0.5f, 0.5f});

        int guiMesh1 = render.addMesh(new float[]{
                -1, -1, 0,
                1, -1, 0,
                -1, 1, 0,
                1, 1, 0,
        },
                new float[]{
                        0, 1,
                        1, 1,
                        0, 0,
                        1, 0,
                },
                new int[]{
                        0, 1, 2,
                        1, 2, 3,
                });
        int happyTexture = render.loadImage("/home/bluesillybeard/Pictures/happy.png");
        int sadTexture = render.loadImage("/home/bluesillybeard/Pictures/sad.png");
        int guiEntity1 = render.addEntity(guiMesh1, happyTexture, guiShader, new float[]{-0.8f, -0.8f, -0.8f,  0, 0, 0,  0.2f, 0.2f, 0.2f});

        double lastStepTime = 0.0;
        double lastFramerateDebugTime = 0.0;
        double lastMouseYPos = render.getMouseYPos();
        double lastMouseXPos = render.getMouseXPos();
        int frames = 0;
        do{
            if(render.shouldRender()){
                render.render();
                frames++;
            }
            if(render.getTime() - lastStepTime > 0.033333){//30 times per second
                lastStepTime = render.getTime();
                if(render.entityContacts(guiEntity1, (float)render.getMouseYPos(), (float)render.getMouseXPos(), false)){
                    render.setEntityModel(guiEntity1, guiMesh1, sadTexture);
                } else {
                    render.setEntityModel(guiEntity1, guiMesh1, happyTexture);
                }
                if(render.getKey(GLFW_KEY_F) >= 2){
                    spawnedEntities.add(render.addEntity(guiMesh1, happyTexture, normalShader, new float[]{cameraPosition.x, cameraPosition.y-1, cameraPosition.z, (float)(Math.random()*Math.PI*2), (float)(Math.random()*Math.PI*2), (float)(Math.random()*Math.PI*2), 1.0f, 1.0f, 1.0f}));
                }
                if(render.getKey(GLFW_KEY_G) == 2){
                    for(int entity: spawnedEntities){
                        render.removeEntity(entity); //remove each entity
                    }
                    spawnedEntities = new ArrayList<>(); //then reset the spawned entities list.
                }


                boolean cameraUpdated = false;
                cameraInc.set(0, 0, 0);
                if (render.getKey(GLFW_KEY_W) >= 2) {
                    cameraInc.z = -1;
                    cameraUpdated = true;
                } else if (render.getKey(GLFW_KEY_S) >= 2) {
                    cameraInc.z = 1;
                    cameraUpdated = true;
                }
                if (render.getKey(GLFW_KEY_A) >= 2) {
                    cameraInc.x = -1;
                    cameraUpdated = true;
                } else if (render.getKey(GLFW_KEY_D) >= 2) {
                    cameraInc.x = 1;
                    cameraUpdated = true;
                }
                if (render.getKey(GLFW_KEY_Z) >= 2) {
                    cameraInc.y = -1;
                    cameraUpdated = true;
                } else if (render.getKey(GLFW_KEY_X) >= 2) {
                    cameraInc.y = 1;
                    cameraUpdated = true;
                }
                double CAMERA_POS_STEP = 1/20d;
                // Update camera position
                if ( cameraInc.z != 0 ) {
                    cameraPosition.x += (float)Math.sin(Math.toRadians(cameraRotation.y)) * -1.0f * cameraInc.z * CAMERA_POS_STEP;
                    cameraPosition.z += (float)Math.cos(Math.toRadians(cameraRotation.y)) * cameraInc.z * CAMERA_POS_STEP;
                }
                if ( cameraInc.x != 0) {
                    cameraPosition.x += (float)Math.sin(Math.toRadians(cameraRotation.y - 90)) * -1.0f * cameraInc.x * CAMERA_POS_STEP;
                    cameraPosition.z += (float)Math.cos(Math.toRadians(cameraRotation.y - 90)) * cameraInc.x * CAMERA_POS_STEP;
                }
                cameraPosition.y += cameraInc.y * CAMERA_POS_STEP;

                // Update camera based on mouse

                if (render.getMouseButton(GLFW_MOUSE_BUTTON_RIGHT) >= 2) {
                    cameraRotation.x += (render.getMouseYPos() - lastMouseYPos) * MOUSE_SENSITIVITY;
                    cameraRotation.y += (render.getMouseXPos() - lastMouseXPos) * MOUSE_SENSITIVITY;
                    cameraUpdated = true;
                }
                lastMouseYPos = render.getMouseYPos();
                lastMouseXPos = render.getMouseXPos();
                //send the camera position to Render
                if(cameraUpdated){
                    render.setCameraPos(cameraPosition.x, cameraPosition.y, cameraPosition.z, cameraRotation.x, cameraRotation.y, cameraRotation.z);
                }

                render.setEntityPosition(entity1, new float[]{0f, 0f, -0f, (float)(render.getTime()/10),  (float)(render.getTime()*5), (float)render.getTime(), 0.5f, 1.0f, 0.5f});
                render.setEntityPosition(entity2, new float[]{0f, 0f, -2f, (float)(render.getTime()*9),  (float)(render.getTime()/7), (float)render.getTime()*1.5f,   1.0f, 0.5f, 0.5f});
                render.setEntityPosition(entity3, new float[]{0f, 0f, -4f, (float)(render.getTime()/3),  (float)(render.getTime()*2), (float)render.getTime()/0.5f,   0.5f, 0.5f, 1.0f});
            }
            if(render.getTime() - lastFramerateDebugTime > 1.0){
                lastFramerateDebugTime = render.getTime();
                System.out.print("rendering " + spawnedEntities.size() + " entities");
                System.out.print(" | framerate:" + frames);
                System.out.println(" | used memory: " + (((jre.totalMemory() - jre.freeMemory()) * 100.0) / jre.maxMemory()) + "%");
                frames = 0;
            }
            try {
                Thread.sleep(1); //this is to keep this thread from eating up 100% after I implement multithreading
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(!render.shouldClose());

        render.close();
    }
}
