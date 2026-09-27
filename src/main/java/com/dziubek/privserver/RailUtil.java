package com.dziubek.privserver;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.util.Vector;

/** Pomocnicze rzeczy wspólne dla PoweredRailListener i StationManager. */
final class RailUtil {

    private RailUtil() {
    }

    /** Domyślny kierunek jazdy dla danego kształtu szyny - używane, gdy wagonik stoi w miejscu
     * (velocity ~0) i trzeba go "kopnąć" w jakąś stronę, bo z samej prędkości nic nie wynika. */
    static Vector fallbackDirection(BlockData data) {
        if (!(data instanceof Rail rail)) {
            return new Vector(1, 0, 0);
        }
        return switch (rail.getShape()) {
            case NORTH_SOUTH -> new Vector(0, 0, 1);
            case EAST_WEST -> new Vector(1, 0, 0);
            case ASCENDING_EAST -> new Vector(1, 0.5, 0);
            case ASCENDING_WEST -> new Vector(-1, 0.5, 0);
            case ASCENDING_NORTH -> new Vector(0, 0.5, -1);
            case ASCENDING_SOUTH -> new Vector(0, 0.5, 1);
            case SOUTH_EAST -> new Vector(1, 0, 1);
            case SOUTH_WEST -> new Vector(-1, 0, 1);
            case NORTH_WEST -> new Vector(-1, 0, -1);
            case NORTH_EAST -> new Vector(1, 0, -1);
        };
    }
}
