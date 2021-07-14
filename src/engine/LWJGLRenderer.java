package engine;

public class LWJGLRenderer implements Render{
    /**
     * the first method called by the game. It should initialize any engine components, as well as create and show the window.
     *
     * @return true if it was successful, false if it was unsuccessful.
     */
    @Override
    public boolean init() {
        return false;
    }

    /**
     * the Major version of the Rendering engine. Major versions are completely incompatible; no intentional backwards compatibility of any kind.
     * The current latest version is 0.
     *
     * @return the major version of the Render.
     */
    @Override
    public int getVersionMajor() {
        return 0;
    }

    /**
     * each minor version should be mostly backwards compatible with older versions.
     * If the game needs version 2, then version 3, 4, 5, etc need to work as well.
     * the current latest version is 0.
     *
     * @return the minor version of the Render
     */
    @Override
    public int getVersionMinor() {
        return 0;
    }

    /**
     * returns the patch version of the render. Patch versions should only fix bugs, exploits, glitches, etc,
     * and any patch version should be 100% compatible with all other patch versions of a minor/major version.
     * For example, if the game needs patch 2, patch 5 should work as well and vice versa if possible.
     * the current latest version is 0.
     *
     * @return the patch version of the Render.
     */
    @Override
    public int getVersionPatch() {
        return 0;
    }

    /**
     * loads a shader pair within shaders. each shader pair is in the shaders directory, and it is two files:
     * [shader]Vertex.glsl and [shader]Fragment.glsl
     * each entity has its own shader.
     *
     * @param shader the shader pair to be loaded
     * @return the shader's ID - this is used for future methods that require a shader. returns -1 if the loading failed somehow - see String getErrors()
     */
    @Override
    public int loadShader(String shader) {
        return 0;
    }

    /**
     * loads an image file (guarantee .png, it would be nice that if you are creating a Render that you also support other formats as well)
     *
     * @param imagePath the path of the image, within the resources directory.
     * @return the image's ID - this is used for future methods that require an image. returns -1 of the loading failed somehow - see String getErrors()
     */
    @Override
    public int loadImage(String imagePath) {
        return 0;
    }

    /**
     * this function is called if init(), loadShader(), or loadImage() return false / -1
     * The result is then printed to the console, or if the first 4 characters read "fatal" then it will throw an exception and crash the game.
     *
     * @return the error string.
     */
    @Override
    public String getErrors() {
        return null;
    }

    /**
     * sets the camera position
     *
     * @param XPos
     * @param YPos
     * @param ZPos
     * @param XRotation
     * @param YRotation
     * @param ZRotation
     */
    @Override
    public void setCameraPos(float XPos, float YPos, float ZPos, float XRotation, float YRotation, float ZRotation) {

    }

    /**
     * creates a mesh - this is simply the frame of a model.
     *
     * @param positions          the OpenGL positions of the mesh.
     * @param textureCoordinates the texture coordinates, AKA UV
     * @param indices            the indices - aka vertices of the mesh.
     * @return the ID of the mesh - this is used in methods that require a mesh
     */
    @Override
    public int addMesh(float[] positions, float[] textureCoordinates, int[] indices) {
        return 0;
    }

    /**
     * removes a mesh from the engine, clearing space. This is done automatically when closing the Render
     *
     * @param mesh the ID of the mesh to remove.
     */
    @Override
    public void removeMesh(int mesh) {

    }

    /**
     * adds a renderable entity to the render - the entities are the in-game objects that are rendered.
     * they contain a Mesh, Texture, Shader, and a 9 component vector for the position.
     *
     * @param mesh     the mesh of that entity.
     * @param texture  the texture of that entity
     * @param shader   the shader of that entity - yes, entities get their own shader.
     * @param position the location, rotation, and scale of the entity. [XPos, YPos, ZPos, XRotation, YRotation, ZRotation, XScale, YScale, ZScale]
     * @return the ID of the entity - used for methods that require an entity.
     */
    @Override
    public int addEntity(int mesh, int texture, int shader, float[] position) {
        return 0;
    }

    /**
     * removes an entity from the Render, meaning it can no longer be rendered. Note that the Mesh, Texture, and Shader
     * are not deleted, as they are seperate objects.
     *
     * @param entity
     */
    @Override
    public void removeEntity(int entity) {

    }

    /**
     * This takes a bit of explanation...
     * When a key is pressed it calls a callback.
     * That callback changes the value of that key to 2.
     * there is another one for when a key is released, which sets it to 0
     * When this function is called, the key's value is returned, then the key's value is changed based on these rules:
     * 2, 3->3
     * 0, 1->1
     * essentially, 0 means just released, 1 means released, 2 means just pushed, 3 means pushed.
     *
     * @param key the key that you are asking information about. uses the same key codes as in GLFW, whatever those are.
     * @return they key's value - returns 0, 1, 2, or 3.
     */
    @Override
    public int getKey(int key) {
        return 0;
    }

    /**
     * @return a timestamp, in seconds. As long as it counts upwards in seconds, it works.
     */
    @Override
    public double getTime() {
        return 0;
    }

    /**
     * @return true if the render method should be called, false otherwise.
     */
    @Override
    public boolean shouldRender() {
        return false;
    }

    /**
     * renders a frame.
     */
    @Override
    public void render() {

    }

    /**
     * @return true if the window should be closed, false otherwise.
     */
    @Override
    public boolean shouldClose() {
        return false;
    }

    /**
     * clears out everything related to the Render.
     * Entities, Meshes, Shaders, Textures, Window, Threads, memory allocations, etc should be cleared out once upon calling this method.
     */
    @Override
    public void close() {

    }
}