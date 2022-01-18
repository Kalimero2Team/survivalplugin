package com.kalimero2.team.survivalplugin.util;

import java.io.Serializable;
import java.util.Objects;

public class SerializableChunk implements Serializable {

    public String world;
    public Integer x;
    public Integer z;

    public SerializableChunk(String world, Integer x, Integer z){
        this.world = world;
        this.x = x;
        this.z = z;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableChunk that = (SerializableChunk) o;
        return Objects.equals(world, that.world) && Objects.equals(x, that.x) && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return "SerializableChunk{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", z=" + z +
                '}';
    }
}
