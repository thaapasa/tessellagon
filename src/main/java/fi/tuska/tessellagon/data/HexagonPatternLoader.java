package fi.tuska.tessellagon.data;

import java.io.File;
import java.util.List;

import fi.tuska.util.file.FileUtils;
import fi.tuska.util.iterator.Iterables;

public class HexagonPatternLoader {

    private static final String PATTERN_DIR = "data/patterns";

    private HexagonPatternLoader() {
        // No need to instantiate, just use the static methods
    }

    public static Hexagon loadPattern(String filename) {

        List<String> lines = FileUtils.readFromFile(new File(PATTERN_DIR, filename + ".pat"));
        if (lines == null || lines.size() == 0) {
            throw new IllegalArgumentException("Pattern file " + filename
                + " not found or invalid");
        }

        Cell[] cells = new Cell[Hexagon.NUMBER_OF_CELLS];
        int cpos = 0;
        for (String line : lines) {
            if (line.trim().startsWith("#"))
                continue;
            for (char c : Iterables.get(line)) {
                Cell cell = null;
                switch (c) {
                case 'o':
                case 'O':
                case '0':
                    // Dead cell
                    cell = new Cell();
                    cell.setDead();
                    break;
                case 'x':
                case 'X':
                    // Alive cell
                    cell = new Cell();
                    cell.setAlive();
                    break;
                case 'b':
                case 'B':
                    // Stone cell
                    cell = new Cell();
                    cell.setStone();
                    break;
                case 's':
                case 'S':
                    // Spawner cell
                    cell = new Cell();
                    cell.setSpawner();
                    break;
                default:
                    //
                    continue;
                }

                if (cpos >= cells.length) {
                    throw new IllegalArgumentException("Too many cells specified in pattern file");
                }
                // Add cell
                cells[cpos++] = cell;
            }
        }

        if (cpos != cells.length) {
            throw new IllegalArgumentException("Too few cells specified in pattern file");
        }

        Hexagon hexagon = new Hexagon(-1, -1, true);

        cpos = 0;
        for (int y = 20; y >= 0; --y) {
            for (int x = 0; x < 7; ++x) {
                Cell curCell = hexagon.getCell(x, y);
                if (curCell != null) {
                    curCell.set(cells[cpos++]);
                }
            }
        }

        assert cpos == cells.length;
        assert cells.length == Hexagon.NUMBER_OF_CELLS;

        return hexagon;
    }

}
