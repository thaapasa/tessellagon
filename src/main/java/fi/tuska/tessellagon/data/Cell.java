package fi.tuska.tessellagon.data;

import fi.tuska.util.Bug;

public class Cell {

    public enum Type {
        Dead, Alive, Spawner, Stone
    }

    public Type type;

    public Cell() {
        this.type = Type.Dead;
    }

    public void set(Cell other) {
        this.type = other.type;
    }

    public boolean isSpecial() {
        return !(type == Type.Alive || type == Type.Dead);
    }

    public Type getType() {
        return type;
    }

    public boolean isSpawner() {
        return type == Type.Spawner;
    }

    public boolean isStone() {
        return type == Type.Stone;
    }

    public void setStone() {
        type = Type.Stone;
    }

    public void setSpawner() {
        type = Type.Spawner;
    }

    public boolean isAlive() {
        return type == Type.Alive || type == Type.Spawner;
    }

    public void setAlive() {
        if (type == Type.Dead)
            type = Type.Alive;
    }

    public void setDead() {
        if (type == Type.Alive)
            type = Type.Dead;
    }

    public void toggleAlive() {
        switch (type) {
        case Alive:
            type = Type.Dead;
            break;
        case Dead:
            type = Type.Alive;
            break;
        default:
            // No change
        }
    }

    @Override
    public String toString() {
        switch (type) {
        case Alive:
            return "alive";
        case Dead:
            return "dead";
        case Spawner:
            return "spawner";
        case Stone:
            return "stone";
        default:
            throw new Bug("Invalid type: " + type);

        }
    }

    public static class DeadCell extends Cell {
        public DeadCell() {
            super();
        }

        @Override
        public void setAlive() {
        }

        @Override
        public boolean isAlive() {
            return false;
        }

    }

}
