package com.kalimero2.team.survivalplugin.util;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Objects;

public class ClaimedChunk {

    private final Chunk chunk;
    private final OfflinePlayer owner;
    private final List<OfflinePlayer> trusted;
    private final String teamClaim;

    public ClaimedChunk(Chunk chunk, OfflinePlayer owner, List<OfflinePlayer> trusted, String teamClaim){
        this.chunk = chunk;
        this.owner = owner;
        this.trusted = trusted;
        this.teamClaim = teamClaim;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public List<OfflinePlayer> getTrusted() {
        return trusted;
    }

    public String getTeamClaim() {
        return teamClaim;
    }

    @Override
    public String toString() {
        return "ClaimedChunk{" +
                "chunk=" + chunk +
                ", owner=" + owner +
                ", trusted=" + trusted +
                ", teamClaim='" + teamClaim + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimedChunk that = (ClaimedChunk) o;
        return Objects.equals(getChunk(), that.getChunk()) && Objects.equals(getOwner(), that.getOwner()) && Objects.equals(getTrusted(), that.getTrusted()) && Objects.equals(getTeamClaim(), that.getTeamClaim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChunk(), getOwner(), getTrusted(), getTeamClaim());
    }
}