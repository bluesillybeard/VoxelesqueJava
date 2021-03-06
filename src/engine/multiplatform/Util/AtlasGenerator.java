package engine.multiplatform.Util;
//The code below is a heavily edited version of https://github.com/lukaszdk/texture-atlas-generator
import engine.multiplatform.model.CPUMesh;
import engine.multiplatform.model.CPUModel;

import java.awt.*;
import java.io.PrintStream;
import java.util.*;
import java.awt.image.*;
import java.util.List;

public class AtlasGenerator{

    /**
     * places the textures into an atlas,
     * creates copies of the meshes and edits their UV coordinates to use the newly generated atlas,
     * and uploads all the data to the GPU.
     * the same index of texture and mesh are linked together into a model
     *
     *
     * @param textures the textures to use.
     * @param meshes the meshes to use.
     * @return the output array of CPUModel.
     */
    public static CPUModel[] generateCPUModels(BufferedImage[] textures, CPUMesh[] meshes, PrintStream print){
        if(meshes.length != textures.length){
            throw new IllegalStateException("meshes and textures differ in length.");
        }
        for(int i=0; i< meshes.length; i++){
            meshes[i] = meshes[i].clone();
        }
        int totalWidth = 0;
        int totalHeight = 0;
        for(BufferedImage tex: textures){
            totalWidth += tex.getWidth();
            totalHeight += tex.getHeight();
        }
        return Run(totalWidth*2, totalHeight*2, 0, true, textures, meshes, print);
    }


    /**
     * places the textures into an atlas,
     * creates copies of the meshes and edits their UV coordinates to use the newly generated atlas,
     * and uploads all the data to the GPU.
     * the same index of texture and mesh are linked together into a model
     *
     * @param models the list of models to be atlassed.
     * @return the output array of CPUModel.
     */
    public static CPUModel[] generateCPUModels(CPUModel[] models, PrintStream print){
        for(int i=0; i< models.length; i++){
            models[i] = models[i].clone();
        }
        int totalWidth = 0;
        int totalHeight = 0;
        for(CPUModel mod: models){
            BufferedImage tex = mod.texture;
            totalWidth += tex.getWidth();
            totalHeight += tex.getHeight();
        }
        return Run(totalWidth*2, totalHeight*2, 0, true, models, print);
    }
    /**
     * places the textures into an atlas,
     * creates copies of the meshes and edits their UV coordinates to use the newly generated atlas,
     * and uploads all the data to the GPU.
     * the same index of texture and mesh are linked together into a model
     *
     *
     * @param textures the textures to use.
     * @param meshes the meshes to use.
     * @return the output array of CPUModel.
     */
    public static List<CPUModel> generateCPUModels(List<BufferedImage> textures, List<CPUMesh> meshes, PrintStream print){
        if(meshes.size() != textures.size()){
            throw new IllegalStateException("meshes and textures differ in length.");
        }
        CPUMesh[] newMeshes = new CPUMesh[meshes.size()];
        for(int i=0; i< meshes.size(); i++){
            newMeshes[i] = meshes.get(i).clone();
        }
        int totalWidth = 0;
        int totalHeight = 0;
        for(BufferedImage mod: textures){
            totalWidth += mod.getWidth();
            totalHeight += mod.getHeight();
        }
        return Arrays.asList(Run(totalWidth*2, totalHeight*2, 0, true, textures.toArray(new BufferedImage[0]), newMeshes, print));
    }


    /**
     * places the textures into an atlas,
     * creates copies of the meshes and edits their UV coordinates to use the newly generated atlas,
     * and uploads all the data to the GPU.
     * the same index of texture and mesh are linked together into a model
     *
     * @param models the list of models to be atlassed.
     * @return the output array of CPUModel.
     */
    public static List<CPUModel> generateCPUModels(List<CPUModel> models, PrintStream print){
        CPUModel[] newModels = new CPUModel[models.size()];
        for(int i=0; i< models.size(); i++){
            newModels[i] = models.get(i).clone();
        }
        int totalWidth = 0;
        int totalHeight = 0;
        for(CPUModel mod: models){
            BufferedImage tex = mod.texture;
            totalWidth += tex.getWidth();
            totalHeight += tex.getHeight();
        }
        return Arrays.asList(Run(totalWidth*2, totalHeight*2, 0, true, newModels, print));

    }

    public static CPUModel[] Run(int width, int height, int padding, boolean ignoreErrors, BufferedImage[] images, CPUMesh[] meshes, PrintStream print)
    {
        Set<ImageName> imageNameSet = new TreeSet<>(new ImageNameComparator());

        for(int i=0; i<images.length; i++)
        {
            BufferedImage image = images[i];
            imageNameSet.add(new ImageName(image, i));
        }

        Texture atlas = new Texture(width, height);

        for(ImageName imageName : imageNameSet)
        {
            if(!atlas.AddImage(imageName.image, imageName.index, padding))
            {
                if(!ignoreErrors)
                    throw new RuntimeException("unable to add image " + imageName.index + " to the atlas!");
                else
                    print.println("unable to add image " + imageName.index + " to the atlas!");
            }
        }
        return atlas.Write(width, height, meshes);
    }


    public static CPUModel[] Run(int width, int height, int padding, boolean ignoreErrors, CPUModel[] models, PrintStream print)
    {
        Set<ImageName> imageNameSet = new TreeSet<>(new ImageNameComparator());

        for(int i=0; i<models.length; i++)
        {
            BufferedImage image = models[i].texture;
            imageNameSet.add(new ImageName(image, i));
        }

        Texture atlas = new Texture(width, height);

        for(ImageName imageName : imageNameSet)
        {
            if(!atlas.AddImage(imageName.image, imageName.index, padding))
            {
                if(!ignoreErrors)
                    throw new RuntimeException("unable to add image " + imageName.index + " to the atlas!");
                else
                    print.println("unable to add image " + imageName.index + " to the atlas!");
            }
        }
        return atlas.Write(width, height, models);
    }

    private static class ImageName
    {
        public BufferedImage image;
        public int index;

        public ImageName(BufferedImage image, int name)
        {
            this.image = image;
            this.index = name;
        }
    }

    private static class ImageNameComparator implements Comparator<ImageName>
    {
        public int compare(ImageName image1, ImageName image2)
        {
            int area1 = image1.image.getWidth() * image1.image.getHeight();
            int area2 = image2.image.getWidth() * image2.image.getHeight();

            if(area1 != area2)
            {
                return area2 - area1;
            }
            else
            {
                return image1.index - image2.index;
            }
        }
    }

    public static class Texture
    {
        private static class Node
        {
            public Rectangle rect;
            public Node[] child;
            public BufferedImage image;

            public Node(int x, int y, int width, int height)
            {
                rect = new Rectangle(x, y, width, height);
                child = new Node[2];
                child[0] = null;
                child[1] = null;
                image = null;
            }

            public boolean IsLeaf()
            {
                return child[0] == null && child[1] == null;
            }

            // Algorithm from http://www.blackpawn.com/texts/lightmaps/
            public Node Insert(BufferedImage image, int padding)
            {
                if(!IsLeaf())
                {
                    Node newNode = child[0].Insert(image, padding);

                    if(newNode != null)
                    {
                        return newNode;
                    }

                    return child[1].Insert(image, padding);
                }
                else
                {
                    if(this.image != null)
                    {
                        return null; // occupied
                    }

                    if(image.getWidth() > rect.width || image.getHeight() > rect.height)
                    {
                        return null; // does not fit
                    }

                    if(image.getWidth() == rect.width && image.getHeight() == rect.height)
                    {
                        this.image = image; // perfect fit
                        return this;
                    }

                    int dw = rect.width - image.getWidth();
                    int dh = rect.height - image.getHeight();

                    if(dw > dh)
                    {
                        child[0] = new Node(rect.x, rect.y, image.getWidth(), rect.height);
                        child[1] = new Node(padding + rect.x + image.getWidth(), rect.y, rect.width - image.getWidth() - padding, rect.height);
                    }
                    else
                    {
                        child[0] = new Node(rect.x, rect.y, rect.width, image.getHeight());
                        child[1] = new Node(rect.x, padding + rect.y + image.getHeight(), rect.width, rect.height - image.getHeight() - padding);
                    }

                    return child[0].Insert(image, padding);
                }
            }
        }

        private final BufferedImage image;
        private final Graphics2D graphics;
        private final Node root;
        private final Map<Integer, Rectangle> rectangleMap;

        public Texture(int width, int height)
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            graphics = image.createGraphics();

            root = new Node(0, 0, width, height);
            rectangleMap = new TreeMap<>();
        }

        public boolean AddImage(BufferedImage image, int index, int padding)
        {
            Node node = root.Insert(image, padding);

            if(node == null)
            {
                return false;
            }

            rectangleMap.put(index, node.rect);
            graphics.drawImage(image, null, node.rect.x, node.rect.y);

            return true;
        }

        public CPUModel[] Write(int width, int height, CPUMesh[] meshes)
        {
            CPUModel[] out = new CPUModel[meshes.length];
            for(Map.Entry<Integer, Rectangle> UVMapping : rectangleMap.entrySet())
            {
                Rectangle rect = UVMapping.getValue();
                float rx = (float)rect.x/width;
                float ry = (float)rect.y/height;
                float rw = (float)rect.width/width;
                float rh = (float)rect.height/height;
                int keyVal = UVMapping.getKey();
                for(int i=0; i<meshes[keyVal].UVCoords.length/2; i++){
                    meshes[keyVal].UVCoords[2*i  ] = meshes[keyVal].UVCoords[2*i  ]*rw+rx;
                    meshes[keyVal].UVCoords[2*i+1] = meshes[keyVal].UVCoords[2*i+1]*rh+ry;
                }
                out[keyVal] = new CPUModel(meshes[keyVal], this.image);
            }
            return out;
        }
        public CPUModel[] Write(int width, int height, CPUModel[] models)
        {
            CPUModel[] out = new CPUModel[models.length];
            for(Map.Entry<Integer, Rectangle> UVMapping : rectangleMap.entrySet())
            {
                Rectangle rect = UVMapping.getValue();
                float rx = (float)rect.x/width;
                float ry = (float)rect.y/height;
                float rw = (float)rect.width/width;
                float rh = (float)rect.height/height;
                int keyVal = UVMapping.getKey();
                for(int i=0; i<models[keyVal].mesh.UVCoords.length/2; i++){
                    models[keyVal].mesh.UVCoords[2*i  ] = models[keyVal].mesh.UVCoords[2*i  ]*rw+rx;
                    models[keyVal].mesh.UVCoords[2*i+1] = models[keyVal].mesh.UVCoords[2*i+1]*rh+ry;
                }
                out[keyVal] = new CPUModel(models[keyVal].mesh, this.image);
            }
            return out;
        }
    }
}
