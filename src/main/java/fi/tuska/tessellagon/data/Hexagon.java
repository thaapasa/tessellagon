package fi.tuska.tessellagon.data;

import java.awt.Point;

import org.apache.log4j.Logger;

import fi.tuska.tessellagon.data.HexGrid.Direction;
import fi.tuska.util.Bug;

public class Hexagon {

    private static final Logger log = Logger.getLogger(Hexagon.class);

    public static final int NUMBER_OF_CELLS = 109;

    public Cell[][][] cells;
    private final int hexX;
    private final int hexY;
    private final boolean alive;
    private volatile boolean active = true;
    private volatile boolean rotating = false;
    private HexGrid grid;

    private static final Cell deadCell = new Cell.DeadCell();

    private DataChangedListener listener;

    private int curH;
    private int otherH;

    public Hexagon(HexGrid grid, int x, int y, boolean alive) {
        this.grid = grid;
        this.cells = new Cell[2][7][21];
        this.hexX = x;
        this.hexY = y;
        this.alive = alive;
        this.curH = 0;
        this.otherH = 1;
        createCells();
    }

    public Hexagon(HexGrid grid, int x, int y) {
        this(grid, x, y, true);
    }

    public Hexagon(int x, int y, boolean alive) {
        this(null, x, y, alive);
    }

    public void setGrid(HexGrid grid) {
        if (this.grid != null)
            throw new IllegalStateException("Grid already set");

        this.grid = grid;
    }

    public boolean isActive() {
        return alive && active && !rotating;
    }

    public void toggleActive() {
        active = !active;
    }

    public void setPattern(Hexagon pattern) {
        for (int x = 0; x < 7; ++x) {
            for (int y = 0; y < 21; ++y) {
                if (cells[curH][x][y] != null) {
                    cells[curH][x][y].set(pattern.cells[pattern.curH][x][y]);
                    cells[otherH][x][y].set(pattern.cells[pattern.curH][x][y]);
                }
            }
        }
        invalidate();
    }

    public void addListener(DataChangedListener listener) {
        if (!alive)
            return;
        if (this.listener != null)
            throw new IllegalStateException("Listener has already been set");
        this.listener = listener;
    }

    public void invalidate() {
        if (listener != null) {
            listener.dataChanged();
        }
    }

    public int getX() {
        return hexX;
    }

    public int getY() {
        return hexY;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setStone() {
        log.info("Setting hexagon at " + hexX + "," + hexY + " to be a stone hexagon");
        for (int x = 0; x < 7; ++x) {
            for (int y = 0; y < 21; ++y) {
                if (cells[curH][x][y] != null && !isSharedCell(x, y)) {
                    cells[curH][x][y].setStone();
                    cells[otherH][x][y].setStone();
                }
            }
        }
    }

    public void rotate(boolean clockwise) {
        assert curH != otherH;
        HexagonRotator.rotate(cells[curH], cells[otherH], clockwise);
        flipCellArrays();
        enforceSharedCells();
    }

    public boolean isRotating() {
        return rotating;
    }

    public void setRotating(boolean rotating) {
        this.rotating = rotating;
    }

    public void stepSimulation() {
        assert (isActive());
        for (int x = 0; x < 7; ++x) {
            for (int y = 0; y < 21; ++y) {
                if (cells[curH][x][y] != null)
                    calculateSimulationForCell(x, y);
            }
        }
    }

    public void flipCellArrays() {
        curH = otherH;
        otherH = (curH == 0) ? 1 : 0;
        assert curH != otherH;
        invalidate();
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || y < 0 || x > 6 || y > 20)
            return null;

        Cell cell = cells[curH][x][y];
        if (cell == null)
            return null;

        // Dead hexagons and rotating hexagons always return dead cells
        return (alive && !rotating) ? cell : deadCell;
    }

    public Cell getCell(Point point) {
        if (!alive) {
            return null;
        }
        int x = (int) point.getX();
        int y = (int) point.getY();
        if (x < 0 || y < 0 || x > 6 || y > 20)
            return null;
        return cells[curH][x][y];
    }

    public Hexagon getNeighbor(Direction direction) {
        if (!alive)
            return null;

        return grid.getNeighbor(hexX, hexY, direction);
    }

    public void enforceSharedCells() {
        Hexagon neighbor = null;
        // Enforce cells at the north border
        neighbor = getNeighbor(Direction.North);
        if (neighbor != null && neighbor.isActive()) {
            // Cannot say neighbor.getCell(...).set(getCell(...)) because this
            // hexagon is rotating and getCell() thus always returns a dead
            // cell.
            neighbor.getCell(2, 0).set(cells[curH][2][20]);
            neighbor.getCell(3, 0).set(cells[curH][3][20]);
            neighbor.getCell(4, 0).set(cells[curH][4][20]);
            neighbor.invalidate();
        }
        // Enforce cells at the south border
        neighbor = getNeighbor(Direction.South);
        if (neighbor != null && neighbor.isActive()) {
            neighbor.getCell(2, 20).set(cells[curH][2][0]);
            neighbor.getCell(3, 20).set(cells[curH][3][0]);
            neighbor.getCell(4, 20).set(cells[curH][4][0]);
            neighbor.invalidate();
        }
        // Enforce cells at the north east border
        neighbor = getNeighbor(Direction.NorthEast);
        if (neighbor != null && neighbor.isActive()) {
            neighbor.getCell(0, 8).set(cells[curH][5][18]);
            neighbor.getCell(0, 5).set(cells[curH][5][15]);
            neighbor.getCell(1, 2).set(cells[curH][6][12]);
            neighbor.invalidate();
        }
        // Enforce cells at the south west border
        neighbor = getNeighbor(Direction.SouthWest);
        if (neighbor != null && neighbor.isActive()) {
            neighbor.getCell(5, 18).set(cells[curH][0][8]);
            neighbor.getCell(5, 15).set(cells[curH][0][5]);
            neighbor.getCell(6, 12).set(cells[curH][1][2]);
            neighbor.invalidate();
        }

        // Enforce cells at the north west border
        neighbor = getNeighbor(Direction.NorthWest);
        if (neighbor != null && neighbor.isActive()) {
            neighbor.getCell(6, 8).set(cells[curH][1][18]);
            neighbor.getCell(5, 5).set(cells[curH][0][15]);
            neighbor.getCell(5, 2).set(cells[curH][0][12]);
            neighbor.invalidate();
        }
        // Enforce cells at the south east border
        neighbor = getNeighbor(Direction.SouthEast);
        if (neighbor != null && neighbor.isActive()) {
            neighbor.getCell(1, 18).set(cells[curH][6][8]);
            neighbor.getCell(0, 15).set(cells[curH][5][5]);
            neighbor.getCell(0, 12).set(cells[curH][5][2]);
            neighbor.invalidate();
        }
        invalidate();
    }

    public boolean isSharedCell(int x, int y) {
        switch (y) {
        case 0:
        case 20:
            return true;
        case 2:
        case 18:
            return x == 1 || x == 5;
        case 5:
        case 15:
            return x == 0 || x == 5;
        case 8:
        case 12:
            return x == 0 || x == 6;
        default:
            return false;
        }
    }

    public boolean isPartOfHexagon(int x, int y) {
        if (y < 0 || x < 0 || y > 20 || x > 7)
            return false;
        switch (y) {
        case 8:
        case 10:
        case 12:
            return true;

        case 5:
        case 7:
        case 9:
        case 11:
        case 13:
        case 15:
            return x < 6;

        case 2:
        case 4:
        case 6:
        case 14:
        case 16:
        case 18:
            return x > 0 && x < 6;

        case 1:
        case 3:
        case 17:
        case 19:
            return x > 0 && x < 5;

        case 0:
        case 20:
            return x > 1 && x < 5;

        default:
            throw new Bug("Impossible value!");
        }
    }

    private void createCells() {
        for (int c = 0; c < 2; ++c) {
            for (int x = 0; x < 7; ++x) {
                for (int y = 0; y < 21; ++y) {
                    cells[c][x][y] = isPartOfHexagon(x, y) ? new Cell() : null;
                }
            }
        }
    }

    /**
     * Use a single array for collecting the neighbor cells to avoid memory
     * burn.
     */
    private Cell[] simNeighbors = new Cell[6];

    /**
     * Not thread-safe, uses the same simNeighbors array to avoid memory burn.
     * 
     * @param x
     * @param y
     */
    private void calculateSimulationForCell(int x, int y) {
        getCellNeighbors(x, y, simNeighbors);

        cells[otherH][x][y].set(cells[curH][x][y]);

        int aliveCells = 0;
        boolean cellAlive = cells[curH][x][y].isAlive();
        for (Cell cell : simNeighbors) {
            if (cell.isAlive())
                aliveCells++;
        }

        boolean nextAlive = cellAlive ? aliveCells == 3 : aliveCells == 4 || aliveCells == 2
            || aliveCells == 5;

        if (nextAlive) {
            cells[otherH][x][y].setAlive();
        } else {
            cells[otherH][x][y].setDead();
        }
    }

    private void getCellNeighbors(int x, int y, Cell[] neighbors) {
        assert (neighbors.length == 6);

        neighbors[0] = getCellNeighbor(x, y, Direction.North);
        neighbors[1] = getCellNeighbor(x, y, Direction.NorthEast);
        neighbors[2] = getCellNeighbor(x, y, Direction.SouthEast);
        neighbors[3] = getCellNeighbor(x, y, Direction.South);
        neighbors[4] = getCellNeighbor(x, y, Direction.SouthWest);
        neighbors[5] = getCellNeighbor(x, y, Direction.NorthWest);
    }

    private Cell getCellNeighbor(int x, int y, HexGrid.Direction dir) {
        switch (dir) {
        case North: {
            // Neighbor to the north

            // Shared cells at the NE border
            if ((x == 5 && (y == 18 || y == 15)) || (x == 6 && y == 12)) {
                return getNeighbor(Direction.NorthEast).getCell(x - 5, y - 8);
            }
            // Shared cells at the NW border
            if ((x == 0 && (y == 12 || y == 15)) || (x == 1 && (y == 18))) {
                return getNeighbor(Direction.NorthWest).getCell(x + 5, y - 8);
            }
            // Cells at the N border
            if (y >= 19) {
                return getNeighbor(Direction.North).getCell(x, y - 18);
            }
            // Cell within the same hexagon
            return getCell(x, y + 2);
        }

        case South: {
            // Neighbor to the south

            // Shared cells at the SE border
            if ((x == 5 && (y == 2 || y == 5)) || (x == 6 && y == 8)) {
                return getNeighbor(Direction.SouthEast).getCell(x - 5, y + 8);
            }
            // Shared cells at the SW border
            if ((x == 0 && (y == 8 || y == 5)) || (x == 1 && y == 2)) {
                return getNeighbor(Direction.SouthWest).getCell(x + 5, y + 8);
            }
            // Cells at the S border
            if (y <= 2) {
                return getNeighbor(Direction.South).getCell(x, y + 18);
            }
            // Cell within the same hexagon
            return getCell(x, y - 2);
        }

        case NorthEast: {

            // Shared cells at the N border
            if (y == 20) {
                return getNeighbor(Direction.North).getCell(x, 1);
            }
            // Cells at the NE border
            if ((x == 4 && y == 19) || (x == 5 && (y == 18 || y == 16 || y == 15 || y == 13))
                || (x == 6 && (y == 12 || y == 10))) {
                Hexagon neighbor = getNeighbor(Direction.NorthEast);
                return y % 2 == 0 ? neighbor.getCell(x - 5, y - 9) : neighbor.getCell(x - 4,
                    y - 9);
            }
            // Shared cells at the SE border
            if ((x == 5 && (y == 2 || y == 5)) || (x == 6 && y == 8)) {
                Hexagon neighbor = getNeighbor(Direction.SouthEast);
                return y % 2 == 0 ? neighbor.getCell(x - 5, y + 11) : neighbor.getCell(x - 4,
                    y + 11);
            }

            return y % 2 == 0 ? getCell(x, y + 1) : getCell(x + 1, y + 1);
        }

        case SouthEast: {
            // Shared cells at the S border
            if (y == 0) {
                return getNeighbor(Direction.South).getCell(x, 19);
            }
            // Cells at the SE border
            if ((x == 4 && y == 1) || (x == 5 && (y == 2 || y == 4 || y == 5 || y == 7))
                || (x == 6 && (y == 8 || y == 10))) {
                Hexagon neighbor = getNeighbor(Direction.SouthEast);
                return y % 2 == 0 ? neighbor.getCell(x - 5, y + 9) : neighbor.getCell(x - 4,
                    y + 9);
            }
            // Shared cells at the NE border
            if ((x == 5 && (y == 18 || y == 15)) || (x == 6 && y == 12)) {
                Hexagon neighbor = getNeighbor(Direction.NorthEast);
                return y % 2 == 0 ? neighbor.getCell(x - 5, y - 11) : neighbor.getCell(x - 4,
                    y - 11);
            }

            return y % 2 == 0 ? getCell(x, y - 1) : getCell(x + 1, y - 1);
        }

        case SouthWest: {
            // Shared cells at the S border
            if (y == 0) {
                return getNeighbor(Direction.South).getCell(x - 1, 19);
            }
            // Cells at the SW border
            if ((x == 0 && (y == 10 || y == 8 || y == 7 || y == 5))
                || (x == 1 && (y == 4 || y == 2 || y == 1))) {
                Hexagon neighbor = getNeighbor(Direction.SouthWest);
                return y % 2 == 0 ? neighbor.getCell(x + 4, y + 9) : neighbor.getCell(x + 5,
                    y + 9);
            }
            // Shared cells at the NW border
            if ((x == 0 && (y == 12 || y == 15)) || (x == 1 && y == 18)) {
                Hexagon neighbor = getNeighbor(Direction.NorthWest);
                return y % 2 == 0 ? neighbor.getCell(x + 4, y - 11) : neighbor.getCell(x + 5,
                    y - 11);
            }

            return y % 2 == 0 ? getCell(x - 1, y - 1) : getCell(x, y - 1);
        }

        case NorthWest: {
            // Shared cells at the N border
            if (y == 20) {
                return getNeighbor(Direction.North).getCell(x - 1, 1);
            }
            // Cells at the NW border
            if ((x == 0 && (y == 10 || y == 12 || y == 13 || y == 15))
                || (x == 1 && (y == 16 || y == 18 || y == 19))) {
                Hexagon neighbor = getNeighbor(Direction.NorthWest);
                return y % 2 == 0 ? neighbor.getCell(x + 4, y - 9) : neighbor.getCell(x + 5,
                    y - 9);
            }
            // Shared cells at the SW border
            if ((x == 0 && (y == 8 || y == 5)) || (x == 1 && y == 2)) {
                Hexagon neighbor = getNeighbor(Direction.SouthWest);
                return y % 2 == 0 ? neighbor.getCell(x + 4, y + 11) : neighbor.getCell(x + 5,
                    y + 11);
            }

            return y % 2 == 0 ? getCell(x - 1, y + 1) : getCell(x, y + 1);
        }

        default:
            throw new Bug("Invalid direction: " + dir);
        }
    }

}
