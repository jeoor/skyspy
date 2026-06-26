package com.kayro.dungeon.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonGeneratorContractTest {
    @Test
    void sameSeedProducesSameMapSignature() {
        DungeonMap first = new DungeonGenerator(20260625L).generate();
        DungeonMap second = new DungeonGenerator(20260625L).generate();

        assertEquals(DungeonMapAnalyzer.signature(first), DungeonMapAnalyzer.signature(second));
    }

    @Test
    void representativeSeedsProduceReachableFloatingIslands() {
        for (long seed = 1L; seed <= 40L; seed++) {
            DungeonMap map = new DungeonGenerator(seed).generate();
            int walkable = DungeonMapAnalyzer.walkableCount(map);

            assertTrue(walkable >= 900, "walkable count too small, seed=" + seed);
            assertTrue(DungeonMapAnalyzer.exitReachable(map), "exit unreachable, seed=" + seed);
            assertEquals(walkable, DungeonMapAnalyzer.reachableWalkableCount(map),
                    "disconnected walkable island, seed=" + seed);
            assertTrue(map.rooms.size >= 2, "missing room anchors, seed=" + seed);
        }
    }
}
