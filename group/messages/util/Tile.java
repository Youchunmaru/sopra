package messages.util;

import enums.FieldType;

import java.util.Optional;

public class Tile {

    public final FieldType tileType;
    public Optional<Integer> clientID = Optional.empty();
    public final boolean hasSpice;
    public final boolean isInSandstorm;



    public Tile(FieldType tileType, boolean hasSpice, boolean isInSandstorm) {
        this.tileType = tileType;
        this.hasSpice = hasSpice;
        this.isInSandstorm = isInSandstorm;
    }
    public Tile(FieldType tileType,int clientID, boolean hasSpice, boolean isInSandstorm) {
        this.tileType = tileType;
        this.clientID = Optional.of(clientID);
        this.hasSpice = hasSpice;
        this.isInSandstorm = isInSandstorm;
    }
}
