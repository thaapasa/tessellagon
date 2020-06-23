package fi.tuska.tessellagon.j3d;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

import org.apache.log4j.Logger;

import fi.tuska.tessellagon.data.Cell;
import fi.tuska.tessellagon.data.Hexagon;

public class HexagonTextureDrawer extends CustomTexture {

    private static final String CELL_ALIVE_IMAGE_FILE = "textures/cell.png";
    private static final String CELL_SPAWNER_IMAGE_FILE = "textures/cell-spawner.png";
    private static final String CELL_STONE_IMAGE_FILE = "textures/cell-stone.png";

    private static final String[] BACKGROUND_IMAGE_FILES = new String[] {
        "textures/hexagon-1.png", "textures/hexagon-2.png", "textures/hexagon-3.png",
        "textures/hexagon-4.png", "textures/hexagon-5.png", "textures/hexagon-6.png" };

    private static final Logger log = Logger.getLogger(HexagonTextureDrawer.class);
    public static final int BORDER_WIDTH_X = 13;
    public static final int BORDER_WIDTH_Y = 1;

    private static EnumMap<Cell.Type, BufferedImage> cellImages = new EnumMap<Cell.Type, BufferedImage>(
        Cell.Type.class);

    private final Hexagon hexagon;

    private final int drawHeight;
    private final int drawWidth;

    private int rotation = 0;

    private static BufferedImage[] bgImages = null;

    static {
        loadImages();
    }

    public HexagonTextureDrawer(Hexagon hexagon) {
        super();
        this.hexagon = hexagon;

        setBackgroundImage(bgImages[0], "hexagon");

        drawHeight = getHeight() - 2 * BORDER_WIDTH_Y;
        drawWidth = getWidth() - 2 * BORDER_WIDTH_X;
    }

    private static void loadImages() {
        bgImages = new BufferedImage[BACKGROUND_IMAGE_FILES.length];
        for (int i = 0; i < BACKGROUND_IMAGE_FILES.length; ++i) {
            bgImages[i] = loadImage(BACKGROUND_IMAGE_FILES[i]);
        }
        cellImages.put(Cell.Type.Alive, loadImage(CELL_ALIVE_IMAGE_FILE));
        cellImages.put(Cell.Type.Stone, loadImage(CELL_STONE_IMAGE_FILE));
        cellImages.put(Cell.Type.Spawner, loadImage(CELL_SPAWNER_IMAGE_FILE));
    }

    public void drawHexagon() {
        Graphics g = drawBackground(bgImages[rotation]);

        for (int x = 0; x < 7; ++x) {
            for (int y = 0; y < 21; ++y) {
                Cell cell = hexagon.getCell(x, y);
                if (cell != null && (cell.isAlive() || cell.isStone())) {
                    int xpos = 0;
                    int ypos = BORDER_WIDTH_Y + (20 - y) * drawHeight / 20;
                    if (y % 2 == 0) {
                        xpos = BORDER_WIDTH_X + (int) ((x * 1.5f) * (float) drawWidth / 9f);
                    } else {
                        xpos = BORDER_WIDTH_X
                            + (int) ((x * 1.5f + .75f) * (float) drawWidth / 9f);
                    }
                    log.debug("Drawing cell " + x + "," + y + ": " + cell + " to " + xpos + ","
                        + ypos);
                    drawCell(g, xpos, ypos, cell.getType());
                }
            }
        }

        updateTexture();
    }

    public void rotate(boolean clockwise) {
        rotation = (rotation + (clockwise ? 1 : 5)) % 6;
        if (log.isDebugEnabled())
            log.debug("Rotated hexagon texture " + (clockwise ? "" : "counter")
                + "clockwise; using texture " + rotation);
    }

    public Point cellToTextureCoordinate(int x, int y) {
        int xpos = 0;
        int ypos = BORDER_WIDTH_Y + (21 - y) * drawHeight / 20;
        if (y % 2 == 0) {
            xpos = BORDER_WIDTH_X + (int) ((x * 1.5f) * (float) drawWidth / 9f);
        } else {
            xpos = BORDER_WIDTH_X + (int) ((x * 1.5f + .75f) * (float) drawWidth / 9f);
        }
        return new Point(xpos, ypos);
    }

    public Point unitToCellCoordinate(double x, double y, double xScale, double yScale) {
        log.debug("Converting " + x + "," + y + " to cell coordinates");
        double xp = (x + xScale) / (2d * xScale);
        double yp = (-y + yScale) / (2d * yScale);
        return new Point((int) (xp * 7), (int) (yp * 21));
    }

    protected void drawCell(Graphics g, int xpos, int ypos, Cell.Type type) {
        BufferedImage cellImage = cellImages.get(type);
        assert cellImage != null;

        int dx = xpos - cellImage.getWidth() / 2;
        int dy = ypos - cellImage.getHeight() / 2;

        g.drawImage(cellImage, dx, dy, null);
    }

}
