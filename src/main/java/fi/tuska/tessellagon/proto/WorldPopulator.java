package fi.tuska.tessellagon.proto;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Material;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import org.apache.log4j.Logger;

import com.sun.j3d.utils.geometry.Sphere;

import fi.tuska.tessellagon.data.Cell;
import fi.tuska.tessellagon.data.HexGrid;
import fi.tuska.tessellagon.data.Hexagon;
import fi.tuska.tessellagon.data.LevelLoader;
import fi.tuska.tessellagon.j3d.HexGrid3d;
import fi.tuska.tessellagon.j3d.Hexagon3d;
import fi.tuska.tessellagon.j3d.TestSquare3d;
import fi.tuska.tessellagon.j3d.World3d;

public class WorldPopulator {

    private static final Logger log = Logger.getLogger(WorldPopulator.class);

    private World3d world;

    public WorldPopulator(World3d world) {
        this.world = world;
    }

    public void populateBoard() {
        addLights();
        // addTestHexagon();
        // addCenterPoint();
        // HexGrid grid = addTestHexGrid();
        HexGrid grid = loadHexLevel();

        for (int x = 0; x < grid.getWidth(); ++x) {
            for (int y = 0; y < grid.getHeight(); ++y) {
                Hexagon hex = grid.getHexagon(x, y);
                addRandomCells(hex, 0);
                hex.invalidate();
            }
        }
        world.setCameraPosition();
        // world.setNominalViewingTransform();
    }

    public void populateSquare() {
        addLights();
        // addTestHexagon();
        addCenterPoint();
        addTestSquare();
        world.setCameraPosition();
        // world.setNominalViewingTransform();
    }

    public static void addRandomCells(Hexagon hexagon, double aliveProbability) {
        for (int x = 0; x < 7; ++x) {
            for (int y = 0; y < 21; ++y) {
                Cell cell = hexagon.getCell(x, y);
                if (cell != null)
                    if (Math.random() < aliveProbability)
                        cell.setAlive();
                    else
                        cell.setDead();
            }
        }
        hexagon.enforceSharedCells();
        hexagon.invalidate();
    }

    public void populateHexagon() {
        addLights();
        // addTestHexagon();
        addCenterPoint();
        Hexagon hex = addTestHexagon();
        world.setCameraPosition();

        addRandomCells(hex, 0.3);

        // for (int x = 0; x < 3; ++x) {
        // for (int y = 0; y < 4; ++y) {
        // Cell cell = hex.getCell(x, y);
        // if (cell != null)
        // cell.setAlive();
        // }
        // }
        // hex.invalidate();

        // world.setNominalViewingTransform();
    }

    public Hexagon addTestHexagon() {
        log.info("Adding test hexagon");
        TransformGroup scalerTG = new TransformGroup();
        Transform3D scaler = new Transform3D();
        scaler.setScale(0.4);
        scalerTG.setTransform(scaler);
        Hexagon hex = new Hexagon(null, 0, 0);
        Hexagon3d hexagon = new Hexagon3d(hex);
        scalerTG.addChild(hexagon.getBranchGroup());
        BranchGroup bg = new BranchGroup();
        bg.addChild(scalerTG);
        world.registerObjectPickListener(bg, hexagon);
        world.registerObjectWheelListener(bg, hexagon);
        world.addBranchGroup(bg);
        return hex;
    }

    public HexGrid addTestHexGrid() {
        log.info("Adding test hexagon grid");
        HexGrid hexes = new HexGrid(4, 11);
        world.addDrawableObject(new HexGrid3d(world, hexes));
        return hexes;
    }

    public HexGrid loadHexLevel() {
        log.info("Loading test hexagon grid");
        HexGrid hexes = LevelLoader.loadLevel("Level1");
        world.addDrawableObject(new HexGrid3d(world, hexes));
        return hexes;
    }

    public void addTestSquare() {
        log.info("Adding test square");
        world.addDrawableObject(new TestSquare3d(world));
    }

    public void addLights() {
        log.info("Adding test lights");
        BranchGroup group = new BranchGroup();
        // Color3f light1Color = new Color3f(1f, 1f, 1f);
        Color3f light1Color = new Color3f(1f, 1f, 1f);

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);

        group.addChild(light1);
        world.addBranchGroup(group);
    }

    public void addCenterPoint() {
        log.info("Adding center point");
        Appearance appearance = new Appearance();
        Color3f color = new Color3f(1f, 0.4f, 0.4f);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
        Texture texture = new Texture2D();
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);
        texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
        Material mat = new Material(color, black, color, white, 70f);
        appearance.setTextureAttributes(texAttr);
        appearance.setMaterial(mat);
        appearance.setTexture(texture);

        BranchGroup group = new BranchGroup();
        Sphere sphere = new Sphere(0.02f, appearance);
        group.addChild(sphere);

        world.addBranchGroup(group);
    }
}
