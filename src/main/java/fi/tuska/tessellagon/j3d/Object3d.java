package fi.tuska.tessellagon.j3d;

import javax.media.j3d.BranchGroup;

public interface Object3d {

    /**
     * Creates the object's 3d visualization.
     * 
     * @param group optional parameter; if present, the object will be added
     * as a child here.
     * @return the object's branch group. This will be the same as the group
     * parameter, if it was specified.
     */
    BranchGroup getBranchGroup(BranchGroup group);
    /** Convenience method; should return the same as getBranchGroup(null); */
    BranchGroup getBranchGroup();

}
