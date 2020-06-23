package fi.tuska.tessellagon.data;

import java.awt.Point;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import fi.tuska.util.Bug;

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
public class HexGrid implements SimulatorStepListener {

    public static final Object HEX_LOCK = new Object();

    public enum Direction {
        North, NorthEast, SouthEast, South, SouthWest, NorthWest
    };

    private final int height;
    private final int width;
    private final Map<Point, Hexagon> grid;
    private final Hexagon oobHexagon;
    private static final Comparator<Point> pointComparator = new Comparator<Point>() {
        @Override
        public int compare(Point o1, Point o2) {
            if (o1.x < o2.x)
                return -10;
            if (o1.x > o2.x)
                return 10;
            if (o1.y < o2.y)
                return -1;
            if (o1.y > o2.y)
                return 1;
            return 0;
        }
    };

    public HexGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new TreeMap<Point, Hexagon>(pointComparator);
        this.oobHexagon = new Hexagon(this, -1, -1, false);
        createHexagons(null);
    }

    public HexGrid(int width, int height, Hexagon[][] hexagons) {
        this.width = width;
        this.height = height;
        assert hexagons.length == width;
        assert hexagons[0].length == height;

        this.grid = new TreeMap<Point, Hexagon>(pointComparator);
        this.oobHexagon = new Hexagon(this, -1, -1, false);
        createHexagons(hexagons);
    }

    private void createHexagons(Hexagon[][] hexagons) {
        for (int y = 0; y < height; ++y) {
            int maxX = (y % 2 == 1) ? width : width - 1;
            for (int x = 0; x < maxX; ++x) {
                Hexagon hex = null;
                if (hexagons != null) {
                    hex = hexagons[x][y];
                    if (hex != null) {
                        hex.setGrid(this);
                        assert hex.getX() == x;
                        assert hex.getY() == y;
                    }
                } else {
                    hex = new Hexagon(this, x, y, true);
                }
                // Missing hexagons are inserted as left missing from the grid
                if (hex != null) {
                    grid.put(new Point(x, y), hex);
                }
            }
        }
    }

    public Hexagon getNeighbor(int x, int y, Direction direction) {
        switch (direction) {
        case North:
            return getHexagon(x, y + 2);
        case South:
            return getHexagon(x, y - 2);
        case NorthEast:
            return (y % 2 == 0) ? getHexagon(x + 1, y + 1) : getHexagon(x, y + 1);
        case SouthEast:
            return (y % 2 == 0) ? getHexagon(x + 1, y - 1) : getHexagon(x, y - 1);
        case NorthWest:
            return (y % 2 == 0) ? getHexagon(x, y + 1) : getHexagon(x - 1, y + 1);
        case SouthWest:
            return (y % 2 == 0) ? getHexagon(x, y - 1) : getHexagon(x - 1, y - 1);
        default:
            throw new Bug("Invalid direction: " + direction);
        }
    }

    public Hexagon getHexagon(int x, int y) {
        Hexagon hex = grid.get(new Point(x, y));
        if (hex == null)
            return oobHexagon;

        return hex;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public void stepSimulation() {
        // Step the simulation!
        synchronized (HEX_LOCK) {
            for (Entry<Point, Hexagon> entry : grid.entrySet()) {
                Hexagon hexagon = entry.getValue();
                if (hexagon.isActive()) {
                    hexagon.stepSimulation();
                }
            }

            for (Entry<Point, Hexagon> entry : grid.entrySet()) {
                Hexagon hexagon = entry.getValue();
                if (hexagon.isActive()) {
                    hexagon.flipCellArrays();
                }
            }
        }
    }

}
