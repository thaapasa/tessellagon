package fi.tuska.tessellagon;

import fi.tuska.tessellagon.data.World;
import fi.tuska.tessellagon.j3d.World3d;
import fi.tuska.tessellagon.proto.WorldPopulator;

public class Tessellagon {

    private final World world;
    private final World3d w3d;

    public Tessellagon(int width, int height) {
        world = new World();
        w3d = new World3d(world);
    }

    public void run() {
        WorldPopulator pop = new WorldPopulator(w3d);
        pop.populateBoard();
        // pop.populateSquare();
        // pop.populateHexagon();
        w3d.run();
    }

    public static void main(String[] args) {
        Tessellagon t = new Tessellagon(10, 10);
        t.run();
    }

}
