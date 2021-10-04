package game.world.block;

import game.world.World;

public class VoidBlock implements Block{

    @Override
    public void onDestroy(int x, int y, int z, World world) {
        world.setBlock(x, y, z, this);
    }

    @Override
    public String getID() {
        return "voxelesque.voidBlock";
    }

    @Override
    public int getMesh() {
        return 0;
    }
}