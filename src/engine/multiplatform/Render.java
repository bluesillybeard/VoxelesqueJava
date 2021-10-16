package engine.multiplatform;

import engine.multiplatform.model.CPUMesh;
import engine.multiplatform.model.CPUModel;

import java.awt.image.BufferedImage;
import java.io.PrintStream;

@SuppressWarnings("unused")
public interface Render {

    /**
     * Initializes the Render and anything contained within it.
     * @param width the width of the window (800 if given an invalid width)
     * @param height the height of the window (600 if given an invalid height)
     * @param resourcesPath The path to the resources folder. This path is added at the front of any path to be loaded by the Render.
     * @param VSync Vsync
     * @param warning the warning PrintStream, Any warning will be sent through it.
     * @param error the error PrintStream. Any errors will be sent through it.
     * @param debug the debug PrintStream. Any debug messages will be sent through it.
     * @return false if something went wrong, true if all is good.
     */
    boolean init(int width, int height, String resourcesPath, boolean VSync, PrintStream warning, PrintStream error, PrintStream debug, float fov);

    //settings

    void setResourcesPath(String path);

    void setVSync(boolean sync);

    void setWarning(PrintStream warning);

    void setError(PrintStream error);

    void setDebug(PrintStream debug);

    void setFov(float fov);

    int getWindowHeight();

    int getWindowWidth();

    /**
     * changes the width and height of the desktop window
     * @param width the new width for the window
     * @param height the new height for the window
     * @return true if it succeeded, false if the window cannot be resized for some reason.
     */
    boolean setWindowSize(int width, int height);


    //images and textures

    /**
     * Reads an image from within the resources directory.
     *
     * @param path the path from the resources directory to read
     * @return the image - note that it must be turned into a GPU texture before it can be used for rendering.
     */
    BufferedImage readImage(String path);


    /**
     * sends a CPU-stored image into a GPU texture for rendering.
     * @param image the image to texturize
     * @return the texture refference. Use in methods that require a texture.
     */
    int readTexture(BufferedImage image);

    /**
     * removes a texture from the GPU to free GPU memory.
     * @param texture the reference to the texture to remove
     * @return true if the texture was successfully deleted, false if something went wrong
     */
    boolean deleteTexture(int texture);

    /**
     * combines textures into an atlas and transforms the texture coordinates of the meshes to use the atlas,
     * then generates a correspinding list of CPUModels, each having a transformed mesh and texture atlas.
     * The indices of each list correspond to the others:
     * index n of the images and index n of the meshes will end up in index n of the output list.
     * @param images the input images
     * @param meshes the input meshes - they won't be modified, instead copies will be made and the copies modified.
     * @return the output list of models that all use the same texture.
     */
    CPUModel[] generateImageAtlas(BufferedImage[] images, CPUMesh[] meshes);

    //meshes
    //IMPORTANT: there are no generate CPU mesh methods - that is because CPUMesh has those in constructor form.

    /**
     * loads a CPUMesh from a .VEMF0 file.
     * Note that this can also load a VBMF file, but the block-specific data won't be loaded into the mesh.
     *  Useful for when a block and entity share the same model, which I doubt will ever happen.
     * @param VEMFPath the path within the resources folder to load
     * @return the CPUMesh defined by the file.
     */
    CPUMesh loadEntityMesh(String VEMFPath);

    /**
     * loads a CPUMesh from a .VBMF0 file.
     * a block model cannot be loaded from a VEMF like an entity can a VBMF.
     * @param VBMFPath the path of the model to load form within the resources directory
     * @return the CPUMesh loaded from the file.
     */
    CPUMesh loadBlockMesh(String VBMFPath);

    /**
     * generates a mesh that can be used for rendering.
     *
     * @param mesh The source mesh
     * @return the reference to the GPU mesh.
     */
    int loadGPUMesh(CPUMesh mesh);

    /**
     * returns the GPU mesh of a GPU model
     * Not recommended.
     * @param GPUModel the model to get the mesh from
     * @return the index of the mesh
     */
    int getMesh(int GPUModel);


    //models
    //IMPORTANT: similar to models, only methods not provided by the CPUModel constructor are provided.

    /**
     * loads a CPUModel from a .VEMF0 file.
     * Note that this can also load a VBMF file, but the block-specific data won't be loaded into the model.
     *  Useful for when a block and entity share the same model, which I doubt will ever happen.
     * @param VEMFPath the path within the resources folder to load
     * @return the CPUModel defined by the file.
     */
    CPUModel loadEntityModel(String VEMFPath);

    /**
     * loads a CPUModel from a .VBMF0 file.
     * a block model cannot be loaded from a VEMF like an entity can a VBMF.
     * @param VBMFPath the path of the model to load form within the resources directory
     * @return the CPUModel loaded from the file.
     */
    CPUModel loadBlockModel(String VBMFPath);

    /**
     * sends a CPUModel to the GPU so it can be rendered.
     * @param model the model to make renderable
     * @return a reference to the model.
     */
    int loadGPUModel(CPUModel model);

    /**
     * creates a model that can be rendered from an image and a CPUMesh
     * @param image the image
     * @param mesh the mesh
     * @return a reference to the model
     */
    int loadGPUModel(BufferedImage image, CPUMesh mesh);

    /**
     * combines a texture and mesh that has already been sent to the GPU and turns them into a model.
     * @param texture the texture for the model
     * @param mesh the mesh.
     * @return ta reference to the resulting model.
     */
    int loadGPUModel(int texture, int mesh);

    /**
     * deletes a model from the GPU
     * Note that the internal texture and mesh are deleted as well, so be careful.
     * @param model the GPU model to delete
     */
    void deleteGPUModel(int model);

    //shaders

    /**
     * loads a shader program and returns its ID.
     * The shader file system is a bit complicated, as this engine is designed with multiple APIs in mind.
     * possible shader folders:
     * gl33: OpenGL 3.3 shaders (GLSL)
     * gl4? OpenGL 4.? shaders (GLSL) (not implemented)
     * vc?: Vulcan ? shaders (not implemented)
     * dx?: DirectX ? (not implemented)
     * @param path the path to the shaders. The final path is: [resources]/[path]/[shader folder(gl33, dx9)]/[shader].[API shader language name(GLSL, HLSL)]
     * @param shader the shader name.
     * @return the reference to the shader.
     */
    int loadShaderProgram(String path, String shader);

    void deleteShaderProgram(int shaderProgram);

    //entities

    int createEntity(int model, int shader, float xPos, float yPos, float zPos, float xRotation, float yRotation, float zRotation, float xScale, float yScale, float zScale);

    int createEntity(int texture, int mesh, int shader, float xPos, float yPos, float zPos, float xRotation, float yRotation, float zRotation, float xScale, float yScale, float zScale);

    void setEntityPos(int entity, float xPos, float yPos, float zPos, float xRotation, float yRotation, float zRotation, float xScale, float yScale, float zScale);

    void setEntityPos(int entity, float xPos, float yPos, float zPos);

    void setEntityRotation(int entity, float xRotation, float yRotation, float zRotation);

    void setEntityScale(int entity, float xScale, float yScale, float zScale);

    void setEntityShader(int entity, int shader);

    void deleteEntity(int entity);

    //chunks

    /**
     * creates a chunk at the chunk position [x, y, z]
     * @param size how big the chunk is in each dimension
     * @param blocks a 3D array of CPUMeshes that represent that chunk's block data.
     * @return the ID of the new chunk.
     */
    int spawnChunk(int size, CPUMesh[][][] blocks, int x, int y, int z);

    /**
     * sets the block data of a chunk.
     * @param blocks a 3D array of blockModel IDs that represent that chunk's block data.
     * @param chunk the chunk whose data will be set.
     */
    void setChunkData(int chunk, CPUMesh[][][] blocks);

    /**
     * sets a specific block [z, y, x] of a chunk.
     * @param chunk the chunk whose block will be modified
     * @param block the blockModel to be used
     */
    void setChunkBlock(int chunk, CPUMesh block, int x, int y, int z);

    /**
     * deletes a chunk so it is no longer rendered.
     * @param chunk the ID of the chunk to remove
     */
    void deleteChunk(int chunk);

    int getNumChunks();

    int getNumChunkSlots();

    //camera
    void setCameraPos(float xPos, float yPos, float zPos, float xRotation, float yRotation, float zRotation);

    //input

    //

    double render();
}
