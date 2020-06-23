package fi.tuska.tessellagon.j3d;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PickInfo;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import org.apache.log4j.Logger;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

import fi.tuska.tessellagon.data.Cell;
import fi.tuska.tessellagon.data.DataChangedListener;
import fi.tuska.tessellagon.data.HexGrid;
import fi.tuska.tessellagon.data.Hexagon;

/**
 * Reminder: Coordinate system: +x is to right; +y is gravitational up, +z is
 * towards the viewer.
 * 
 * @author Tuukka Haapasalo
 */
public class Hexagon3d extends Object3dImpl implements ObjectPickListener, ObjectWheelListener,
    DataChangedListener {

    /** The y-offset of the north/south points is sqrt(3)/2. */
    private static final float ZOFFS = 0.866025404f;

    /** The z-offset of the north/south points is sqrt(3)/4 +- .5. */
    // private static final float TZOFFS = 0.433012702f;

    /** Height is twice the y-offset = sqrt(3). */
    private static final float HEIGHT = 1.73205081f;

    // public static Shape3D hexagonShape = null;

    private static final Logger log = Logger.getLogger(Hexagon3d.class);

    private final int x;
    private final int y;

    private final Hexagon hexagon;

    private HexagonTextureDrawer customTexture;

    public Hexagon3d(Hexagon hexagon) {
        this.hexagon = hexagon;
        this.x = hexagon.getX();
        this.y = hexagon.getY();
        this.hexagon.addListener(this);
    }

    public Hexagon getHexagon() {
        return hexagon;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * The width of the hexagon is exacly 2 units.
     * 
     * @return the width
     */
    public static float getWidth() {
        return 2f;
    }

    /**
     * The height of the hexagon is sqrt(3).
     * 
     * @return the height
     */
    public static float getHeight() {
        return HEIGHT;
    }

    /**
     * The half-height of the hexagon is sqrt(3) / 2. This is the height
     * difference between two rows of hexagons.
     * 
     * @return half of the height
     */
    public static float getHalfHeight() {
        return ZOFFS;
    }

    public void updateTexture() {
        customTexture.drawHexagon();
    }

    @Override
    public BranchGroup createBranchGroup(BranchGroup group) {
        if (group == null)
            group = new BranchGroup();

        GeometryArray hexagon = createHexagon3d();
        Appearance appearance = createAppearance();
        Shape3D hexagonShape = new Shape3D(hexagon, appearance);
        hexagonShape.setUserData(new Point(x, y));

        group.addChild(hexagonShape);
        return group;
    }

    private Color3f getColor() {
        // return new Color3f(.95f, .95f, 1.0f);
        return new Color3f((float) Math.sin(x * .69f) * .3f + .7f,
            (float) Math.sin(y * .25f) * .2f + .8f, (float) Math.cos(x * .81f) * .1f + .9f);
    }

    private Appearance createAppearance() {
        // yellow appearance
        Appearance appearance = new Appearance();
        Color3f color = getColor();
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
        customTexture = new HexagonTextureDrawer(hexagon);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        Material mat = new Material(color, black, color, white, 70f);
        appearance.setTextureAttributes(texAttr);
        appearance.setMaterial(mat);
        appearance.setTexture(customTexture.getTexture());
        return appearance;
    }

    /**
     * Creates the hexagon geometry. The hexagon is laid out around the axis,
     * so that the width of the hexagon is parallel to the x axis, and the
     * height is parallel to the z axis, so the hexagon is lying on the
     * ground. Normals point up. North is away from the viewer (negative z),
     * east is to the left (positive x).
     * 
     * @return the hexagon geometry
     */
    private GeometryArray createHexagon3d() {
        // Points: Origo, East, West, North East, North West, South East,
        // South West
        Point3f o = new Point3f(0, 0, 0);
        TexCoord2f to = new TexCoord2f(.5f, .5f);

        Point3f e = new Point3f(1, 0, 0);
        TexCoord2f te = new TexCoord2f(1f, .5f);
        Point3f w = new Point3f(-1, 0, 0);
        TexCoord2f tw = new TexCoord2f(0f, .5f);

        Point3f ne = new Point3f(.5f, 0, -ZOFFS);
        TexCoord2f tne = new TexCoord2f(0.75f, 1);
        Point3f nw = new Point3f(-.5f, 0, -ZOFFS);
        TexCoord2f tnw = new TexCoord2f(0.25f, 1);
        Point3f se = new Point3f(.5f, 0, ZOFFS);
        TexCoord2f tse = new TexCoord2f(0.75f, 0);
        Point3f sw = new Point3f(-.5f, 0, ZOFFS);
        TexCoord2f tsw = new TexCoord2f(0.25f, 0);

        TriangleArray hexagon = new TriangleArray(18, TriangleArray.COORDINATES
            | TriangleArray.TEXTURE_COORDINATE_2);

        int c = 0;
        c = addTexturedTriangle(hexagon, c, o, e, ne, to, te, tne);
        c = addTexturedTriangle(hexagon, c, o, ne, nw, to, tne, tnw);
        c = addTexturedTriangle(hexagon, c, o, nw, w, to, tnw, tw);
        c = addTexturedTriangle(hexagon, c, o, w, sw, to, tw, tsw);
        c = addTexturedTriangle(hexagon, c, o, sw, se, to, tsw, tse);
        c = addTexturedTriangle(hexagon, c, o, se, e, to, tse, te);

        assert c == 18;

        GeometryInfo geometryInfo = new GeometryInfo(hexagon);
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(geometryInfo);

        GeometryArray result = geometryInfo.getGeometryArray();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Hexagon at %d,%d", x, y);
    }

    @Override
    public void objectPicked(PickInfo result, MouseEvent event) {
        boolean leftButton = (event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0;
        if (leftButton) {
            Point3d intPoint = result.getClosestIntersectionPoint();
            Point cellCoords = customTexture.unitToCellCoordinate(intPoint.getX(),
                intPoint.getZ(), 1d, Hexagon3d.ZOFFS);
            Cell cell = hexagon.getCell(cellCoords);
            log.debug("Closest intersection point: " + intPoint + " is cell at " + cellCoords
                + " = " + cell);
            if (cell != null) {
                cell.toggleAlive();
                hexagon.invalidate();
            }
        }
    }

    public void finishRotation(boolean clockwise) {
        hexagon.rotate(clockwise);
        customTexture.rotate(clockwise);
        hexagon.setRotating(false);
        hexagon.invalidate();
    }

    public void startRotation(boolean clockwise) {
        hexagon.setRotating(true);
    }

    @Override
    public void dataChanged() {
        customTexture.drawHexagon();
    }

    @Override
    public void objectWheel(PickInfo result, MouseWheelEvent event) {
        boolean clockwise = event.getWheelRotation() < 0;
        synchronized (HexGrid.HEX_LOCK) {
            startRotation(clockwise);
            finishRotation(clockwise);
        }
    }

}
