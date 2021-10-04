package oldEngine.model;

import VMF.VMFLoader;

import java.util.Arrays;

public class BlockMesh{
    public final float[] positions;
    public final float[] UVCoords;
    public final int[] indices;

    public final byte[] removableTriangles;
    public final byte blockedFaces;

    public BlockMesh(float[] positions, float[] UVCoords, int[] indices) {
        this.positions = positions;
        this.UVCoords = UVCoords;
        this.indices = indices;
        this.removableTriangles = new byte[0];
        this.blockedFaces = 0;
    }
    //blockedFaces: [top (+y), bottom(-y), (-z / +z), -x, +x]
    public BlockMesh(float[] positions, float[] UVCoords, int[] indices, byte[] removableTriangles, byte blockedFaces) {
        this.positions = positions;
        this.UVCoords = UVCoords;
        this.indices = indices;
        this.removableTriangles = removableTriangles;
        this.blockedFaces = blockedFaces;
    }

    public BlockMesh(VMFLoader loader){
        this.positions = loader.getVertices();
        this.UVCoords = loader.getTextureCoordinates();
        this.indices = loader.getIndices();
        this.removableTriangles = loader.getRemovableTriangles();
        this.blockedFaces = loader.getBlockedFaces();
    }

    public BlockMesh clone(){
        return new BlockMesh(Arrays.copyOf(positions, positions.length), Arrays.copyOf(UVCoords, UVCoords.length), Arrays.copyOf(indices, indices.length), Arrays.copyOf(removableTriangles, removableTriangles.length), blockedFaces);
    }

}