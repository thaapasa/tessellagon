package fi.tuska.tessellagon.j3d;

import java.awt.event.MouseEvent;

import javax.media.j3d.PickInfo;

public interface ObjectPickListener {

    void objectPicked(PickInfo result, MouseEvent event);

}
