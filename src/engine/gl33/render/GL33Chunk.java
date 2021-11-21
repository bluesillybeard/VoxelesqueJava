package engine.gl33.render;

import engine.gl33.model.GL33Mesh;
import engine.gl33.model.GL33Texture;
import engine.multiplatform.Util.CPUMeshBuilder;
import engine.multiplatform.gpu.GPUChunk;
import engine.multiplatform.model.CPUMesh;
import org.joml.Vector3i;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.Map;

public class GL33Chunk implements GPUChunk{
    private final Vector3i pos;
    private CPUMesh[][][] data;
    private GL33Texture[][][] textures;
    private GL33Shader[][][] shaders;
    private final int size;
    private GL33Entity[] chunkModel;
    private boolean canRender;

    private ArrayList<CPUMeshBuilder> chunkModels = new ArrayList<>();
    private ArrayList<ShaderTexture> shaderTextures = new ArrayList<>();

    public boolean taskRunning;

    public GL33Chunk(int size, CPUMesh[][][] data, GL33Texture[][][] textures, GL33Shader[][][] shaders, int xPos, int yPos, int zPos){
        if(data.length != size || data[0].length != size || data[0][0].length != size){
            throw new IllegalStateException("a chunk's data cannot be any other size than " + size + "," +
                    "\n but the data given to the constructor has dimensions (" + data.length + ", " + data[0].length + ", " + data[0][0].length + ")");
        }
        this.data = data;
        this.textures = textures;
        this.shaders = shaders;
        this.canRender = false;
        taskRunning = false;
        this.size = size;
        this.pos = new Vector3i(xPos, yPos, zPos);
    }

    public void setData(CPUMesh[][][] data, GL33Texture[][][] textures, GL33Shader[][][] shaders){
        if(data != null && (data.length != this.size || data[0].length != this.size || data[0][0].length != this.size)){
            throw new IllegalStateException("a chunk's data cannot be any other size than " + this.size + "," +
                    " but the data given to the constructor has dimensions (" + data.length + ", " + data[0].length + ", " + data[0][0].length + ")");
        }
        if(textures != null && (textures.length != this.size || textures[0].length != this.size || textures[0][0].length != this.size)){
            throw new IllegalStateException("a chunk's data cannot be any other size than " + this.size + "," +
                    " but the data given to the constructor has dimensions (" + textures.length + ", " + textures[0].length + ", " + textures[0][0].length + ")");
        }
        if(shaders != null && (shaders.length != this.size || shaders[0].length != this.size || shaders[0][0].length != this.size)){
            throw new IllegalStateException("a chunk's data cannot be any other size than " + this.size + "," +
                    " but the data given to the constructor has dimensions (" + shaders.length + ", " + shaders[0].length + ", " + shaders[0][0].length + ")");
        }
        this.data = data;
        this.textures = textures;
        this.shaders = shaders;
    }
    public void setBlock(CPUMesh block, GL33Texture texture, GL33Shader shader, int x, int y, int z){
        data[x][y][z] = block;
        textures[x][y][z] = texture;
        shaders[x][y][z] = shader;
    }
    public void render(){
        if(!canRender) return; //don't render if it can't
        for(GL33Entity entity: chunkModel){
            entity.render(); //the entities positions are already set to the right place in the build method
        }
    }

    /**
     * clears the vertex data from the GPU.
     */
    public void clearFromGPU(){
        if(this.chunkModel != null) {
            for (GL33Entity entity : this.chunkModel) {
                entity.getModel().mesh.cleanUp();//DON'T clear the texture.
            }
        }
    }

    public boolean sendToGPU(){
        if(taskRunning){
            return false;
        } else if(chunkModels.size() > 0){
            if(canRender)clearFromGPU();
            GL33Entity[] model = new GL33Entity[shaderTextures.size()];
            for (int i = 0; i < model.length; i++) {
                GL33Entity entity = new GL33Entity(new GL33Mesh(chunkModels.get(i).getMesh()), shaderTextures.get(i).shader, shaderTextures.get(i).texture);
                entity.setPosition(this.pos.x * this.size * 0.288675134595f, this.pos.y * this.size * 0.5f, this.pos.z * this.size * 0.5f);
                entity.setScale(1, 1, 1);
                model[i] = entity;
            }
            chunkModels.clear();
            shaderTextures.clear();
            //chunkModels = new ArrayList<>();
            //shaderTextures = new ArrayList<>();
            this.chunkModel = model;
            this.canRender = true;
            return true;
        }
        return false;
    }

    /**
     *
     * @param chunks the map of chunk positions to chunk objects to get adjacent chunks from
     */
    public void build(Map<Vector3i, GL33Chunk> chunks) {
        /*
        an overview of how chunk building works:
        initialize a list of shaders and models

        for each block:
           get its block model
           if we don't already have its shader:
               add another shader and model to the list
           create a copy of the blocks model (not the chunk model)
           remove the removable indices based on the blocks around it
           add that block model to the chunk model

         */
        ShaderTexture TSP = new ShaderTexture();

        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[x].length; y++) {
                for (int z = 0; z < data[x][y].length; z++) {
                    CPUMesh block = data[x][y][z];
                    if (block == null) continue; //skip rendering this block if it is null (void)
                    GL33Shader program = shaders[x][y][z];
                    GL33Texture texture = textures[x][y][z];
                    int shaderTextureIndex = shaderTextures.indexOf(TSP.s(program).t(texture));
                    if (shaderTextureIndex == -1) {
                        shaderTextureIndex = chunkModels.size();
                        shaderTextures.add(new ShaderTexture(program, texture));
                        chunkModels.add(new CPUMeshBuilder(this.size * this.size * this.size * 10, true));//todo: test optimal factor (currently 10)
                    }
                    //cloning, index removal, and vertex position modification done within the BlockMeshBuilder
                    chunkModels.get(shaderTextureIndex).addBlockMeshToChunk(block, x, y, z, this.getBlockedFaces(x, y, z, chunks));
                    if(Thread.interrupted()){
                        return; //so the game can close if a chunk is still rendering
                    }
                }
            }
        }
        this.taskRunning = false;
    }


    //blockedFaces: [top (+y), bottom(-y), (-z / +z), -x, +x]
    private byte getBlockedFaces(int x, int y, int z, Map<Vector3i, GL33Chunk> chunks){
        byte blockedFaces = 0;

        for(int i=0; i < 5; i++){

            int xM = switch(i){
                case 3 -> x-1;
                case 4 -> x+1;
                default -> x;
            };

            int yM = switch(i){
                case 0 -> y+1;
                case 1 -> y-1;
                default -> y;
            };

            int zM;
            if (i == 2) {
                zM = (z + x & 1) * -2 + 1 + z; //I don't know how I figured this out, but I did.
            } else {
                zM = z;
            }
            GL33Chunk toUse = this;

            //[(-1, 0, 0), (0, -1, 0), (0, 0, -1), (+1, 0, 0), (0, +1, 0), (0, 0, +1)]
            if(xM<0){ //-1, 0, 0
                toUse = chunks.get(new Vector3i(pos.x-1, pos.y, pos.z));
                xM = size-1;
            }
            else if(xM>this.size-1){//+1, 0, 0
                toUse = chunks.get(new Vector3i(pos.x+1, pos.y, pos.z));
                xM = 1;
            }
            else if(yM<0) {//0, -1, 0
                toUse = chunks.get(new Vector3i(pos.x, pos.y-1, pos.z));;
                yM  = size-1;
            }
            else if(yM>this.size-1){ //0, +1, 0
                toUse = chunks.get(new Vector3i(pos.x, pos.y+1, pos.z));
                yM  = 1;
            }
            else if(zM<0) { //0, 0, -1
                toUse = chunks.get(new Vector3i(pos.x, pos.y, pos.z-1));
                zM = size-1;
            }
            else if(zM>this.size-1){ //0, 0, +1
                toUse = chunks.get(new Vector3i(pos.x, pos.y, pos.z+1));
                zM = 1;
            }
            if(toUse == null){
                blockedFaces |= (1 << i); //if the chunk doesn't exist yet, assume it's blocked
                continue;
            }
            CPUMesh mesh = toUse.getBlock(xM, yM, zM);

            if (mesh == null || mesh.blockedFaces == 0) continue; //skip if that mesh doesn't block faces
            blockedFaces |= (mesh.blockedFaces & (1 << i)); //add the blocked face to the bit field.
        }
        return blockedFaces;//blockedFaces;
    }

    /**
     * tells what render backend this came from.
     * supported render APIs:
     * 0:unknown (This should absolutely under no circumstances ever happen. Not in all time and space should this value ever be returned by this function)
     * 1:GL33
     *
     * @return the render backend ID
     */
    @Override
    public int getRenderType() {
        return 1;
    }

    public Vector3i getPosition(){
        return this.pos;
    }

    public CPUMesh getBlock(int x, int y, int z){
        return this.data[x][y][z];
    }


    private static class ShaderTexture{
        public GL33Shader shader;
        public GL33Texture texture;

        public ShaderTexture(){

        }
        public ShaderTexture(GL33Shader s, GL33Texture t){
            this.shader = s;
            this.texture = t;
        }
        public ShaderTexture s(GL33Shader s){
            this.shader = s;
            return this;
        }
        public ShaderTexture t(GL33Texture s){
            this.texture = s;
            return this;
        }

        public boolean equals(Object other){
            if(other instanceof ShaderTexture o) {
                return shader == o.shader && texture == o.texture;
            } else {
                return false;
            }
        }
    }
    public String toString(){
        return "chunk:" + pos + " s:" + taskRunning + " r:" + canRender;
    }
}
