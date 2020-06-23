package fi.tuska.tessellagon.data;

import java.util.EnumMap;

import org.apache.log4j.Logger;

public class World {

    private static final Logger log = Logger.getLogger(World.class);

    public enum PatternType {
        Pat1, Glider, Corner
    };

    private EnumMap<PatternType, Hexagon> patterns = new EnumMap<World.PatternType, Hexagon>(
        PatternType.class);

    public World() {
        loadPatterns();
    }

    public Hexagon getPattern(PatternType type) {
        return patterns.get(type);
    }

    private void loadPatterns() {
        for (PatternType t : PatternType.values()) {
            log.debug("Loading pattern " + t);
            Hexagon pattern = HexagonPatternLoader.loadPattern(t.name());
            patterns.put(t, pattern);
        }
    }

}
