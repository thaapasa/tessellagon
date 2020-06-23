package fi.tuska.tessellagon.j3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * Reminder: Coordinate system: +x is to right; +y is gravitational up, +z is
 * towards the viewer.
 * 
 * @author Tuukka Haapasalo
 */
public class TestSquare3d extends Object3dImpl {

    private World3d world;

    public TestSquare3d(World3d world) {
        this.world = world;
    }

    @Override
    public BranchGroup createBranchGroup(BranchGroup group) {
        if (group == null)
            group = new BranchGroup();

        GeometryArray square = createSquare3d();
        Appearance appearance = createAppearance();
        Shape3D squareShape = new Shape3D(square, appearance);

        TransformGroup scalerTG = new TransformGroup();
        Transform3D scaler = new Transform3D();
        scaler.setScale(0.4d);
        scalerTG.setTransform(scaler);
        scalerTG.addChild(squareShape);

        group.addChild(scalerTG);
        return group;
    }

    private Appearance createAppearance() {
        Texture2D texture = world.loadTexture("textures/locations.png");

        // yellow appearance
        Appearance appearance = new Appearance();
        Color3f color = new Color3f(1.0f, 1.0f, 1.0f);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        texture.setBoundaryModeS(Texture.WRAP);
        texture.setBoundaryModeT(Texture.WRAP);
        texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
        Material mat = new Material(color, black, color, white, 70f);
        appearance.setTextureAttributes(texAttr);
        appearance.setMaterial(mat);
        appearance.setTexture(texture);
        return appearance;
    }

    private GeometryArray createSquare3d() {
        Point3f ne = new Point3f(1, 0, -1);
        TexCoord2f tne = new TexCoord2f(1, 1);
        Point3f se = new Point3f(1, 0, 1);
        TexCoord2f tse = new TexCoord2f(1, 0);
        Point3f nw = new Point3f(-1, 0, -1);
        TexCoord2f tnw = new TexCoord2f(0, 1);
        Point3f sw = new Point3f(-1, 0, 1);
        TexCoord2f tsw = new TexCoord2f(0, 0);

        TriangleArray hexagon = new TriangleArray(6, TriangleArray.COORDINATES
            | TriangleArray.TEXTURE_COORDINATE_2);

        int c = 0;
        c = addTexturedTriangle(hexagon, c, ne, sw, se, tne, tsw, tse);
        c = addTexturedTriangle(hexagon, c, ne, nw, sw, tne, tnw, tsw);

        assert c == 6;

        GeometryInfo geometryInfo = new GeometryInfo(hexagon);
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(geometryInfo);

        GeometryArray result = geometryInfo.getGeometryArray();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Square");
    }

}
