package fi.tuska.tessellagon.j3d;

import java.awt.event.MouseWheelEvent;

import javax.media.j3d.PickInfo;

public interface ObjectWheelListener {

    void objectWheel(PickInfo result, MouseWheelEvent event);

}
