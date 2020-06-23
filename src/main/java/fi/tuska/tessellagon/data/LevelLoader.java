package fi.tuska.tessellagon.data;

import java.io.File;
import java.util.List;
import java.util.Properties;

import fi.tuska.util.file.FileUtils;
import fi.tuska.util.iterator.Iterables;

public class LevelLoader {

    private static final String DATA_SEPARATOR = "data";
    private static final String PROP_WIDTH = "level.width";
    private static final String PROP_HEIGHT = "level.height";
    private static final String LEVEL_DIR = "data/levels";

    private LevelLoader() {
        // No need to instantiate, just use the static methods
    }

    private static Properties getDefaultProperties() {
        Properties props = new Properties();
        props.put(PROP_WIDTH, "5");
        props.put(PROP_HEIGHT, "15");
        return props;
    }

    public static HexGrid loadLevel(String filename) {
        List<String> lines = FileUtils.readFromFile(new File(LEVEL_DIR, filename + ".lvl"));
        if (lines == null || lines.size() == 0) {
            throw new IllegalArgumentException("Level file " + filename + " not found or invalid");
        }

        Properties props = getDefaultProperties();

        int i = loadHeader(lines, props);

        int width = Integer.parseInt(props.getProperty(PROP_WIDTH));
        int height = Integer.parseInt(props.getProperty(PROP_HEIGHT));

        Hexagon[][] hexagons = loadHexagons(lines, i, width, height);

        HexGrid level = new HexGrid(width, height, hexagons);

        return level;
    }

    private static Hexagon[][] loadHexagons(List<String> lines, int position, int width,
        int height) {
        Hexagon[][] hexes = new Hexagon[width][height];

        int curY = height - 1;
        int curX = 0;
        int hexesLoaded = 0;
        for (int i = position; i < lines.size(); ++i) {
            String line = lines.get(i).trim();
            if (line.startsWith("#"))
                continue;

            for (char c : Iterables.get(line)) {

                Hexagon hex = null;

                switch (c) {
                case 'o':
                case 'O':
                case '0':
                    hex = new Hexagon(curX, curY, true);
                    break;

                case 'B':
                case 'b':
                    hex = new Hexagon(curX, curY, true);
                    hex.setStone();
                    break;

                case 'm':
                case 'M':
                    // Missing hexagon, leave as null
                    break;

                default:
                    // Comment, continue to next character
                    continue;
                }

                ++hexesLoaded;
                hexes[curX][curY] = hex;

                curX++;
                if (curX >= width) {
                    curX = 0;
                    curY--;
                }
            }
        }

        if (hexesLoaded != width * height) {
            throw new IllegalArgumentException("Level file invalid: expected " + (width * height)
                + " hexagons, got " + hexesLoaded);
        }

        return hexes;
    }

    private static int loadHeader(List<String> lines, Properties props) {
        // Read header
        boolean dataFound = false;
        int i = 0;
        for (; i < lines.size(); ++i) {
            String line = lines.get(i).trim();
            if (line.startsWith("#"))
                continue;
            if (line.equalsIgnoreCase(DATA_SEPARATOR)) {
                dataFound = true;
                break;
            }

            String[] parts = line.split("=");
            if (parts.length == 2) {
                props.put(parts[0].trim(), parts[1].trim());
            }
        }

        if (!dataFound) {
            throw new IllegalArgumentException("Invalid level data: data part not found");
        }
        return i + 1;
    }
}
