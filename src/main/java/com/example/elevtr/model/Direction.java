package com.example.elevtr.model;

public enum Direction {
    UP,
    DOWN;

    public Direction opposite() {
        return values()[(ordinal() + 1) % 2];
    }

    public int step() {
        return this == UP ? 1 : -1;
    }   
}
