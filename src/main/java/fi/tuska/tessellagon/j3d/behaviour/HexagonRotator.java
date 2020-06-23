package fi.tuska.tessellagon.j3d.behaviour;

import java.util.Enumeration;

import javax.media.j3d.Alpha;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.vecmath.AxisAngle4d;

import org.apache.log4j.Logger;

import fi.tuska.tessellagon.data.HexGrid;
import fi.tuska.tessellagon.data.Hexagon;
import fi.tuska.tessellagon.j3d.Hexagon3d;

public class HexagonRotator {

    public static final long ROTATION_TIME_MILLIS = 200;

    private static final Logger log = Logger.getLogger(HexagonRotator.class);

    private static final Alpha zeroAlpha = new Alpha(0, Long.MAX_VALUE);

    private volatile Alpha alpha;
    private RotationInterpolator rotInt;
    private Hexagon3d hexagon;
    private static final Transform3D zeroRotate = new Transform3D();
    private boolean nextRotationClockwise = false;
    private volatile boolean rotating = false;
    private RotateCompletedBehavior rotateCompletedBehavior;

    static {
        zeroRotate.setRotation(new AxisAngle4d(0d, 0d, 1d, 0d));
        zeroAlpha.setMode(0);
    }

    public HexagonRotator(TransformGroup targetTG, Hexagon3d hexagon) {
        assert zeroAlpha.finished();
        this.hexagon = hexagon;
        this.rotateCompletedBehavior = new RotateCompletedBehavior(hexagon.getHexagon());
        // this.targetTG = targetTG;
        targetTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        alpha = new Alpha(1, ROTATION_TIME_MILLIS);
        rotInt = new RotationInterpolator(zeroAlpha, targetTG);
        rotInt.setSchedulingBounds(new BoundingSphere());
        rotInt.setMinimumAngle(0);
        rotInt.setMaximumAngle((float) (Math.PI / 3f));
        targetTG.addChild(rotateCompletedBehavior);
    }

    public RotationInterpolator getInterpolator() {
        return rotInt;
    }

    public void rotate(boolean clockwise) {
        synchronized (HexGrid.HEX_LOCK) {
            if (rotating) {
                return;
            }
            rotating = true;
            assert rotInt.getAlpha().finished();

            log.debug("Rotating " + hexagon + (clockwise ? " clockwise" : " counterclockwise"));
            hexagon.startRotation(clockwise);
            alpha.setStartTime(System.currentTimeMillis());
            rotInt.setAlpha(alpha);
            rotInt.setMaximumAngle((clockwise ? -1f : 1f) * (float) (Math.PI / 3f));
            rotateCompletedBehavior.schedule();
            nextRotationClockwise = clockwise;
        }
    }

    @Override
    public String toString() {
        return String.format("Hexagon rotator for rotator of %s", hexagon);
    }

    private void finishRotation() {
        synchronized (HexGrid.HEX_LOCK) {
            rotInt.setAlpha(zeroAlpha);
            hexagon.finishRotation(nextRotationClockwise);
            rotating = false;
        }
    }

    private class RotateCompletedBehavior extends Behavior {

        private final int myID;
        private WakeupCriterion postSchedulingStart;
        private volatile WakeupCriterion timeElapsed = new WakeupOnElapsedTime(
            ROTATION_TIME_MILLIS);

        public RotateCompletedBehavior(Hexagon hexagon) {
            myID = hexagon.getY() * 10000 + hexagon.getX();
            postSchedulingStart = new WakeupOnBehaviorPost(this, myID);
            setSchedulingBounds(new BoundingSphere());
        }

        @Override
        public void initialize() {
            wakeupOn(postSchedulingStart);
        }

        public void schedule() {
            if (log.isDebugEnabled())
                log.debug("Rotation started, scheduling completion for hexagon " + hexagon.getX()
                    + "," + hexagon.getY());

            // wakeupOn(timeElapsed);
            postId(myID);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void processStimulus(Enumeration criteria) {
            if (timeElapsed.hasTriggered()) {
                synchronized (HexGrid.HEX_LOCK) {
                    if (log.isDebugEnabled())
                        log.debug("Rotation complete for hexagon " + hexagon.getX() + ","
                            + hexagon.getY());
                    timeElapsed = new WakeupOnElapsedTime(ROTATION_TIME_MILLIS);
                    finishRotation();
                }
            } else if (postSchedulingStart.hasTriggered()) {
                if (log.isDebugEnabled())
                    log.debug("Noted that we should wait for rotation to complete for hexagon "
                        + hexagon.getX() + "," + hexagon.getY());
                wakeupOn(timeElapsed);
                return;
            }
            wakeupOn(postSchedulingStart);
        }
    };

}
