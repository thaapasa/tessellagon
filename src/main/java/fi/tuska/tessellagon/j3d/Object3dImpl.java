package fi.tuska.tessellagon.j3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

public abstract class Object3dImpl implements Object3d {

    private BranchGroup cachedGroup;

    /**
     * Creates the object's branch group
     * 
     * @param group
     * @return
     */
    protected abstract BranchGroup createBranchGroup(BranchGroup group);

    /**
     * Creates the object's 3d visualization.
     * 
     * @param group optional parameter; if present, the object will be added
     * as a child here.
     * @return the object's branch group. This will be the same as the group
     * parameter, if it was specified.
     */
    @Override
    public BranchGroup getBranchGroup(BranchGroup group) {
        if (group != null) {
            // Because a group was specified, always create a new presentation
            // for the object and do not cache the result
            group = createBranchGroup(group);
            return group;
        }
        // No parameter specified, created a new cached group if necessary
        if (cachedGroup == null) {
            cachedGroup = createBranchGroup(group);
        }
        return cachedGroup;
    }

    public BranchGroup getBranchGroup() {
        return getBranchGroup(null);
    }

    protected int addTexturedTriangle(TriangleArray array, int position, Point3f p1, Point3f p2,
        Point3f p3, TexCoord2f tp1, TexCoord2f tp2, TexCoord2f tp3) {
        array.setCoordinate(position, p1);
        array.setTextureCoordinate(0, position, tp1);
        ++position;
        array.setCoordinate(position, p2);
        array.setTextureCoordinate(0, position, tp2);
        ++position;
        array.setCoordinate(position, p3);
        array.setTextureCoordinate(0, position, tp3);
        ++position;
        return position;
    }
}
