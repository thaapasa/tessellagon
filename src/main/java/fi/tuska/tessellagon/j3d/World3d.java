package fi.tuska.tessellagon.j3d;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.PickInfo;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedTime;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.log4j.Logger;

import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import fi.tuska.tessellagon.data.SimulatorStepListener;
import fi.tuska.tessellagon.data.World;
import fi.tuska.util.Pair;

/**
 * Reminder: Coordinate system: +x is to right; +y is gravitational up, +z is
 * towards the viewer.
 */
public class World3d {

    public static final int SIMULATION_STEP_MILLIS = 100;

    private static final Logger log = Logger.getLogger(World3d.class);

    private final SimpleUniverse universe;
    private final Canvas3D canvas;
    private final JFrame frame;
    private List<SimulatorStepListener> simulatorStepListeners = new ArrayList<SimulatorStepListener>();
    private List<Pair<PickCanvas, ObjectPickListener>> objectPickListeners = new ArrayList<Pair<PickCanvas, ObjectPickListener>>();
    private List<Pair<PickCanvas, ObjectWheelListener>> objectWheelListeners = new ArrayList<Pair<PickCanvas, ObjectWheelListener>>();
    private TimedBehavior timedBehavior = new TimedBehavior();

    private World world;

    public World3d(World world) {
        this.world = world;

        GraphicsConfiguration c = getGraphicsConfiguration();
        log.debug("Using graphics configuration: " + c);
        frame = new JFrame("Tessellagon");
        canvas = new Canvas3D(c);
        frame.getContentPane().add(canvas);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        universe = new SimpleUniverse(canvas);
        frame.setSize(1200, 900);
        canvas.addMouseListener(mouseListener);
        canvas.addKeyListener(keyListener);
        canvas.addMouseWheelListener(mouseWheelListener);

        BranchGroup timerBG = new BranchGroup();
        timerBG.addChild(timedBehavior);
        universe.addBranchGraph(timerBG);
    }

    public void quit() {
        log.info("Exiting the game");
        frame.setVisible(false);
        frame.dispose();

        System.exit(0);
    }

    public void run() {
        log.info("Starting to run the world");
        frame.setVisible(true);
    }

    public Canvas3D getCanvas() {
        return canvas;
    }

    public World getWorld() {
        return world;
    }

    public void registerSimulatorStepListener(SimulatorStepListener listener) {
        simulatorStepListeners.add(listener);
    }

    public void registerObjectPickListener(BranchGroup group, ObjectPickListener listener) {
        PickCanvas pickCanvas = new PickCanvas(getCanvas(), group);
        pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
        pickCanvas.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT);
        pickCanvas.setTolerance(0.0f);

        objectPickListeners.add(new Pair<PickCanvas, ObjectPickListener>(pickCanvas, listener));
    }

    public void registerObjectWheelListener(BranchGroup group, ObjectWheelListener listener) {
        PickCanvas pickCanvas = new PickCanvas(getCanvas(), group);
        pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
        pickCanvas.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT);
        pickCanvas.setTolerance(0.0f);

        objectWheelListeners.add(new Pair<PickCanvas, ObjectWheelListener>(pickCanvas, listener));
    }

    public void setNominalViewingTransform() {
        universe.getViewingPlatform().setNominalViewingTransform();
    }

    public void addDrawableObject(Object3d object) {
        universe.addBranchGraph(object.getBranchGroup());
    }

    public void addBranchGroup(BranchGroup group) {
        universe.addBranchGraph(group);
    }

    public void setCameraPosition() {
        ViewingPlatform ourView = universe.getViewingPlatform();
        Transform3D locator = new Transform3D();
        Point3d eye = new Point3d(0f, 1.1f, .8f);
        // Point3d eye = new Point3d(0f, 1.1f, 0.0001f);
        Point3d target = new Point3d(0f, 0f, 0.06f);
        Vector3d up = new Vector3d(0f, 1f, 0f);
        locator.lookAt(eye, target, up);
        locator.invert();

        ourView.getViewPlatformTransform().setTransform(locator);
    }

    public Texture2D loadTexture(String textureFile) {
        TextureLoader loader = new TextureLoader(textureFile, canvas);
        ImageComponent2D image = loader.getImage();
        log.debug("Loading texture " + textureFile + ": " + image.getWidth() + ","
            + image.getHeight());

        Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, image.getWidth(),
            image.getHeight());
        texture.setImage(0, image);

        return texture;
    }

    protected GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsConfigTemplate3D templ = new GraphicsConfigTemplate3D();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (GraphicsDevice gd : gs) {
            GraphicsConfiguration conf = gd.getBestConfiguration(templ);
            if (conf != null)
                return conf;
        }
        return null;
    }

    private MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            log.debug("Mouse wheel scrolled at " + e.getPoint());
            for (Pair<PickCanvas, ObjectWheelListener> i : objectWheelListeners) {
                PickCanvas pickCanvas = i.getFirst();
                ObjectWheelListener listener = i.getSecond();

                log.debug("Picking object with " + pickCanvas);

                pickCanvas.setShapeLocation(e);
                PickInfo result = pickCanvas.pickClosest();
                if (result == null) {
                    log.debug("Nothing picked");
                } else {
                    listener.objectWheel(result, e);
                }
            }

        }
    };
    private MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
            log.debug("Mouse pressed at " + e.getPoint());
            for (Pair<PickCanvas, ObjectPickListener> i : objectPickListeners) {
                PickCanvas pickCanvas = i.getFirst();
                ObjectPickListener listener = i.getSecond();

                log.debug("Picking object with " + pickCanvas);

                pickCanvas.setShapeLocation(e);
                PickInfo result = pickCanvas.pickClosest();
                if (result == null) {
                    log.debug("Nothing picked");
                } else {
                    listener.objectPicked(result, e);
                }
            }
        }
    };

    private KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            switch (e.getKeyChar()) {
            case 'q':
            case 'Q':
                quit();
                return;
            case KeyEvent.VK_ESCAPE:
                quit();
                return;
            }
        }
    };

    private class TimedBehavior extends Behavior {

        private WakeupCondition wakeupCriteria = new WakeupOnElapsedTime(SIMULATION_STEP_MILLIS);

        public TimedBehavior() {
            setSchedulingBounds(new BoundingSphere());
        }

        @Override
        public void initialize() {
            this.wakeupOn(wakeupCriteria);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void processStimulus(Enumeration criteria) {
            // log.info("Timer tick");

            for (SimulatorStepListener listener : simulatorStepListeners) {
                listener.stepSimulation();
            }

            this.wakeupOn(wakeupCriteria);
        }
    }

}
