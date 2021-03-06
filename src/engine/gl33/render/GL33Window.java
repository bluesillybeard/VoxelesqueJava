package engine.gl33.render;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GL33Window {

    private final String title;

    private int width, height;

    private long windowHandle;

    private boolean resized, vSync;

    private final int[] keys;
    private final int[] mouseButtons;
    private double scrolls;
    private double cursorXPos, cursorYPos;
    private boolean cursorPosLocked;
    private int frames;

    private static final int numKeys = 348; //there are 348 keys supported by GLFW
    private static final int numMouseButtons = 5; //there are 5 mouse buttons supported by GLFW

    public GL33Window(String title, int width, int height, boolean vSync) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
        this.keys = new int[numKeys-1];
        Arrays.fill(keys, -1);
        this.mouseButtons = new int[numMouseButtons-1];
        Arrays.fill(mouseButtons, -1);
    }

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); //modern OpenGL
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if(action == GLFW_PRESS){
                keys[key] = frames;
            } else if(action == GLFW_RELEASE){
                keys[key] = -frames;
            }
        });
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if(action == GLFW_PRESS){
                mouseButtons[button] = frames;
            } else if(action == GLFW_RELEASE){
                mouseButtons[button] = -frames;
            }
        });
        glfwSetScrollCallback(windowHandle, (window, xOffset, yOffset) ->{
            scrolls += yOffset; // set scroll amount
        });
        glfwSetCursorPosCallback(windowHandle, (window, xPos, yPos) -> {
            cursorXPos = (xPos/width)*2-1; //GLFW gives pixel coordinates, but I want a nice value between -1 and 1, which maps the same way as OpenGL maps it.
            cursorYPos = (yPos/height)*2-1;
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        assert vidmode != null;
        glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);

        if (isvSync()) {
            // Enable v-sync
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }

        // Make the window visible
        glfwShowWindow(windowHandle);

        GL.createCapabilities(); //I'm pretty sure this is a LWJGL thing, not an OpenGL thing.

        // Set the clear color

        //enable depth testing
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        frames = 0;
    }
    public int getKey(int key) {
        int keyValue = keys[key];
        if(keyValue > 0){
            return frames - keyValue;
        } else {
            return keyValue - frames;
        }
    }

    public int getMouseButton(int button) {
        int buttonValue = mouseButtons[button];
        if(buttonValue > 0){
            return frames - buttonValue;
        } else {
            return buttonValue - frames;
        }
    }

    public double getScrolls(){
        return scrolls;
    }

    public void setTitle(String title){
        glfwSetWindowTitle(windowHandle, title);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height){
        glfwSetWindowSize(this.windowHandle, width, height);
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isvSync() {
        return vSync;
    }

    public void setVSync(boolean vSync) {
        this.vSync = vSync;
        if(vSync)
            glfwSwapInterval(1); //Just came back from C++, and I nearly did glfwSwapInterval(vSync)
        else
            glfwSwapInterval(0);
    }

    public void update() {
        ++frames;
        if(cursorPosLocked) {
            glfwSetCursorPos(windowHandle, width / 2., height / 2.);
            cursorXPos = 0;
            cursorYPos = 0;
        }
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    public double getCursorXPos(){
        return cursorXPos;
    }

    public double getCursorYPos(){
        return cursorYPos;
    }

    public void lockMousePos(){
        cursorPosLocked = true;
    }

    public void unlockMousePos(){
        cursorPosLocked = false;
    }

    public boolean mouseLocked(){
        return cursorPosLocked;
    }
}
