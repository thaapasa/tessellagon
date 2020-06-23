package fi.tuska.tessellagon.j3d;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.PickInfo;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.apache.log4j.Logger;

import fi.tuska.tessellagon.data.HexGrid;
import fi.tuska.tessellagon.data.Hexagon;
import fi.tuska.tessellagon.data.World.PatternType;
import fi.tuska.tessellagon.j3d.behaviour.HexagonRotator;

/**
 * Grid layout:
 * 
 * <pre>
 *       (0,4)       (1,4)       (2,4)
 * (0,3)<  |  >(1,3)<  |  >(2,3)<
 *       (0,2)   |   (1,2)   |   (2,2)
 * (0,1)<  |  >(1,1)<  |  >(2,1)<
 *       (0,0)       (1,0)       (2,0)
 * </pre>
 * 
 * Or, to put it in another way:
 * 
 * <pre>
 *         _____     _____     _____
 *     ___/     \___/     \___/     \
 *    /   \ 0,4 /   \ 1,4 /   \ 2,4 /
 *   < 0,3 >---< 1,3 >---< 2,3 >---<
 *    \___/     \___/     \___/     \
 *    /   \ 0,2 /   \ 1,2 /   \ 2,2 /
 *   < 0,1 >---< 1,1 >---< 2,1 >---<
 *    \___/ 0,0 \___/ 1,0 \___/ 2,0 \
 *        \_____/   \_____/   \_____/
 * </pre>
 * 
 * @author Tuukka Haapasalo
 */
public class HexGrid3d extends Object3dImpl implements ObjectPickListener, ObjectWheelListener {

    private static final Logger log = Logger.getLogger(HexGrid3d.class);

    private final Map<Point, HexagonRotator> rotators;
    private final Map<Point, Hexagon3d> hexagons;
    private final World3d world;
    private final int width;
    private final int height;

    private final HexGrid hexGrid;

    public HexGrid3d(World3d world, HexGrid hexGrid) {
        this.hexGrid = hexGrid;
        this.width = hexGrid.getWidth();
        this.height = hexGrid.getHeight();
        this.world = world;
        this.rotators = new HashMap<Point, HexagonRotator>(2 * width * height);
        this.hexagons = new HashMap<Point, Hexagon3d>(2 * width * height);
        world.registerSimulatorStepListener(hexGrid);
    }

    public double getGridWidthInUnits() {
        return width * 1.5d * Hexagon3d.getWidth();
    }

    public double getGridHeightInUnits() {
        return (height + .5d) * Hexagon3d.getHalfHeight();
    }

    @Override
    protected BranchGroup createBranchGroup(BranchGroup group) {
        if (group == null)
            group = new BranchGroup();

        double maxUnits = Math.max(getGridWidthInUnits(), getGridHeightInUnits());

        // TransformGroup tg = new TransformGroup();

        TransformGroup scaleGroup = new TransformGroup();
        Transform3D scaler = new Transform3D();
        scaler.setScale(1d / maxUnits);
        scaleGroup.setTransform(scaler);

        TransformGroup moveGroup = new TransformGroup();
        Transform3D mover = new Transform3D();
        // mover.setTranslation(new Vector3d(-.5d, 0, -.5d));
        mover.setTranslation(new Vector3d(-getGridWidthInUnits() * .5d + Hexagon3d.getWidth()
            * .75d, 0d, -getGridHeightInUnits() * .5d + Hexagon3d.getHalfHeight() * .75d));
        moveGroup.setTransform(mover);

        scaleGroup.addChild(moveGroup);
        group.addChild(scaleGroup);

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                TransformGroup hexagon = createHexagonTransformGroup(x, y);
                if (hexagon != null) {
                    moveGroup.addChild(hexagon);
                }
            }
        }

        // pickCanvas = new PickCanvas(world.getCanvas(), group);
        // pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
        // pickCanvas.setFlags(PickInfo.NODE |
        // PickInfo.CLOSEST_INTERSECTION_POINT);
        // pickCanvas.setTolerance(0.0f);

        world.registerObjectPickListener(group, this);
        world.registerObjectWheelListener(group, this);

        group.compile();

        // Let Java 3D perform optimizations on this scene graph.
        return group;
    }

    private TransformGroup createHexagonTransformGroup(int x, int y) {
        Hexagon hex = hexGrid.getHexagon(x, y);
        if (!hex.isAlive())
            return null;
        Hexagon3d shape = new Hexagon3d(hex);
        hexagons.put(new Point(x, y), shape);

        TransformGroup scaledHexagonTG = createScaledHexagon(shape);

        // Translation group
        BranchGroup objRoot = new BranchGroup();

        TransformGroup objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRotate.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

        objRoot.addChild(objRotate);
        objRotate.addChild(scaledHexagonTG);

        // Bounds bounds = new BoundingSphere();

        HexagonRotator rotator = new HexagonRotator(objRotate, shape);
        rotators.put(new Point(x, y), rotator);
        objRoot.addChild(rotator.getInterpolator());

        TransformGroup t1 = getLocationTransform(x, y);
        t1.addChild(objRoot);

        return t1;
    }

    private TransformGroup createScaledHexagon(Hexagon3d hexagon) {
        TransformGroup tg = new TransformGroup();
        Transform3D trans = new Transform3D();
        // trans.setScale(0.97);
        trans.setScale(1);
        tg.setTransform(trans);

        BranchGroup hexShape = hexagon.createBranchGroup(null);

        tg.addChild(hexShape);

        return tg;
    }

    private TransformGroup getLocationTransform(int x, int y) {
        float xpos = (float) (x * 1.5f) * Hexagon3d.getWidth();
        if (y % 2 == 0)
            xpos += Hexagon3d.getWidth() * 0.75f;

        float zpos = (height - y - 1) * Hexagon3d.getHalfHeight();
        log.debug("Creating location transform for hexagon " + x + ", " + y + ": pos " + xpos
            + ", " + zpos);
        TransformGroup g = new TransformGroup();
        Transform3D f = new Transform3D();
        float yoffs = getYTranslation(x, y);
        f.setTranslation(new Vector3f(xpos, yoffs, zpos));
        g.setTransform(f);
        return g;
    }

    private float getYTranslation(int x, int y) {
        return (x % 3) * .02f + ((y - 1) % 5) * .01f;
    }

    @Override
    public void objectPicked(PickInfo result, MouseEvent event) {
        boolean leftButton = (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0;
        Shape3D s = (Shape3D) result.getNode();
        if (s != null) {
            Object userData = s.getUserData();
            if (userData != null && userData instanceof Point) {
                Point point = (Point) userData;
                log.debug("HexGrid3d object picked: " + point);

                Hexagon hexagon = hexagons.get(point).getHexagon();
                synchronized (HexGrid.HEX_LOCK) {
                    if (hexagon.isActive()) {
                        Hexagon pat = world.getWorld().getPattern(
                            leftButton ? PatternType.Glider : PatternType.Corner);
                        hexagon.setPattern(pat);
                        // WorldPopulator.addRandomCells(hexagon, 0.4);
                    }
                }
            }
        }
    }

    @Override
    public void objectWheel(PickInfo result, MouseWheelEvent event) {
        boolean clockwise = event.getWheelRotation() < 0;
        // Primitive p = (Primitive) result.getNode(PickResult.PRIMITIVE);
        Shape3D s = (Shape3D) result.getNode();
        if (s != null) {
            Object userData = s.getUserData();
            if (userData != null && userData instanceof Point) {
                Point point = (Point) userData;
                log.debug("HexGrid3d object wheeled: " + point);

                HexagonRotator rot = rotators.get(point);
                assert rot != null;
                Hexagon hexagon = hexagons.get(point).getHexagon();
                synchronized (HexGrid.HEX_LOCK) {
                    if (!hexagon.isRotating()) {
                        rot.rotate(clockwise);
                    }
                }
            }
        }
    }

}
