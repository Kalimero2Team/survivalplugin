package com.kalimero2.team.survivalplugin.util;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;

public class ExtraPlayerData implements Serializable {

    public final HashSet<SerializableChunk> chunks;
    public Integer maxclaims;
    public boolean vip;

    public ExtraPlayerData(HashSet<SerializableChunk> chunks, Integer maxclaims, boolean vip) {
        this.chunks = chunks;
        this.maxclaims = maxclaims;
        this.vip = vip;
    }

    public ExtraPlayerData(ExtraPlayerData loadedData) {
        this.chunks = loadedData.chunks;
        this.maxclaims = loadedData.maxclaims;
        this.vip = loadedData.vip;
    }

    public boolean saveData(String filePath) {
        try {
            Gson gson = new Gson();
            Writer writer = new FileWriter(filePath);

            gson.toJson(this, writer);

            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ExtraPlayerData loadData(String filePath) {
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Path.of(filePath));

            ExtraPlayerData data = gson.fromJson(reader, ExtraPlayerData.class);
            reader.close();

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtraPlayerData that = (ExtraPlayerData) o;
        return vip == that.vip && Objects.equals(chunks, that.chunks) && Objects.equals(maxclaims, that.maxclaims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunks, maxclaims, vip);
    }
}