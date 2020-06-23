package fi.tuska.tessellagon.data;

import org.apache.log4j.Logger;

public class HexagonRotator {

    private static final int[][][] counterClockwiseRotationArray = new int[7][21][2];

    private static final Logger log = Logger.getLogger(HexagonRotator.class);

    /**
     * Rotates the elements in the hexagonal arrays, one step, either
     * clockwise or counterclockwise. The rotated values are placed in the
     * target array, and they are read from the source array.
     * 
     * @param source the source array
     * @param target the target array
     * @param clockwise true to rotate clockwise, false to rotate
     * counterclockwise
     */
    public static void rotate(Cell[][] source, Cell[][] target, boolean clockwise) {
        log.debug("Rotating hexagons " + (clockwise ? "clockwise" : "counterclockwise"));
        for (int x1 = 0; x1 < 7; ++x1) {
            int[][] ccraX = counterClockwiseRotationArray[x1];
            for (int y1 = 0; y1 < 21; ++y1) {
                int[] point = ccraX[y1];
                int x2 = point[0];
                int y2 = point[1];
                if (x2 != 0 || y2 != 0) {
                    if (clockwise) {
                        target[x1][y1].set(source[x2][y2]);
                    } else {
                        target[x2][y2].set(source[x1][y1]);
                    }
                } else {
                    assert source[x1][y1] == null;
                    assert target[x1][y1] == null;
                }
            }
        }
    }

    /**
     * Presets the rotation array.
     */
    static {
        // x1, y1 becomes x2, y2
        // e.g. 2,0 becomes 5,2
        rot(2, 0, 5, 2);
        rot(3, 0, 5, 5);
        rot(4, 0, 6, 8);

        rot(1, 1, 4, 1);
        rot(2, 1, 5, 4);
        rot(3, 1, 5, 7);
        rot(4, 1, 6, 10);

        rot(1, 2, 4, 0);
        rot(2, 2, 4, 3);
        rot(3, 2, 5, 6);
        rot(4, 2, 5, 9);
        rot(5, 2, 6, 12);

        rot(1, 3, 4, 2);
        rot(2, 3, 4, 5);
        rot(3, 3, 5, 8);
        rot(4, 3, 5, 11);

        rot(1, 4, 3, 1);
        rot(2, 4, 4, 4);
        rot(3, 4, 4, 7);
        rot(4, 4, 5, 10);
        rot(5, 4, 5, 13);

        rot(0, 5, 3, 0);
        rot(1, 5, 3, 3);
        rot(2, 5, 4, 6);
        rot(3, 5, 4, 9);
        rot(4, 5, 5, 12);
        rot(5, 5, 5, 15);

        rot(1, 6, 3, 2);
        rot(2, 6, 3, 5);
        rot(3, 6, 4, 8);
        rot(4, 6, 4, 11);
        rot(5, 6, 5, 14);

        rot(0, 7, 2, 1);
        rot(1, 7, 3, 4);
        rot(2, 7, 3, 7);
        rot(3, 7, 4, 10);
        rot(4, 7, 4, 13);
        rot(5, 7, 5, 16);

        rot(0, 8, 2, 0);
        rot(1, 8, 2, 3);
        rot(2, 8, 3, 6);
        rot(3, 8, 3, 9);
        rot(4, 8, 4, 12);
        rot(5, 8, 4, 15);
        rot(6, 8, 5, 18);

        rot(0, 9, 2, 2);
        rot(1, 9, 2, 5);
        rot(2, 9, 3, 8);
        rot(3, 9, 3, 11);
        rot(4, 9, 4, 14);
        rot(5, 9, 4, 17);

        rot(0, 10, 1, 1);
        rot(1, 10, 2, 4);
        rot(2, 10, 2, 7);
        rot(3, 10, 3, 10);
        rot(4, 10, 3, 13);
        rot(5, 10, 4, 16);
        rot(6, 10, 4, 19);

        rot(0, 11, 1, 3);
        rot(1, 11, 2, 6);
        rot(2, 11, 2, 9);
        rot(3, 11, 3, 12);
        rot(4, 11, 3, 15);
        rot(5, 11, 4, 18);

        rot(0, 12, 1, 2);
        rot(1, 12, 1, 5);
        rot(2, 12, 2, 8);
        rot(3, 12, 2, 11);
        rot(4, 12, 3, 14);
        rot(5, 12, 3, 17);
        rot(6, 12, 4, 20);

        rot(0, 13, 1, 4);
        rot(1, 13, 1, 7);
        rot(2, 13, 2, 10);
        rot(3, 13, 2, 13);
        rot(4, 13, 3, 16);
        rot(5, 13, 3, 19);

        rot(1, 14, 1, 6);
        rot(2, 14, 1, 9);
        rot(3, 14, 2, 12);
        rot(4, 14, 2, 15);
        rot(5, 14, 3, 18);

        rot(0, 15, 0, 5);
        rot(1, 15, 1, 8);
        rot(2, 15, 1, 11);
        rot(3, 15, 2, 14);
        rot(4, 15, 2, 17);
        rot(5, 15, 3, 20);

        rot(1, 16, 0, 7);
        rot(2, 16, 1, 10);
        rot(3, 16, 1, 13);
        rot(4, 16, 2, 16);
        rot(5, 16, 2, 19);

        rot(1, 17, 0, 9);
        rot(2, 17, 1, 12);
        rot(3, 17, 1, 15);
        rot(4, 17, 2, 18);

        rot(1, 18, 0, 8);
        rot(2, 18, 0, 11);
        rot(3, 18, 1, 14);
        rot(4, 18, 1, 17);
        rot(5, 18, 2, 20);

        rot(1, 19, 0, 10);
        rot(2, 19, 0, 13);
        rot(3, 19, 1, 16);
        rot(4, 19, 1, 19);

        rot(2, 20, 0, 12);
        rot(3, 20, 0, 15);
        rot(4, 20, 1, 18);
    }

    private static void rot(int x1, int y1, int x2, int y2) {
        counterClockwiseRotationArray[x1][y1][0] = x2;
        counterClockwiseRotationArray[x1][y1][1] = y2;
    }
}
