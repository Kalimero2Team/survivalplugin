package com.kalimero2.team.survivalplugin.database.pojo;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class MinecraftUser {
    private ObjectId id;
    private String uuid;
    private String name;
    private boolean bedrock;
    private boolean rulesAccepted;
    private DiscordUser discordUser;

    public MinecraftUser() {
    }

    public MinecraftUser(final String uuid, final String name, final boolean bedrock, boolean rulesAccepted) {
        this.uuid = uuid;
        this.name = name;
        this.bedrock = bedrock;
        this.rulesAccepted = rulesAccepted;
        this.discordUser = null;
    }

    public MinecraftUser(final String uuid, final String name, final boolean bedrock, boolean rulesAccepted, final DiscordUser discordUser) {
        this.uuid = uuid;
        this.name = name;
        this.bedrock = bedrock;
        this.rulesAccepted = rulesAccepted;
        this.discordUser = discordUser;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBedrock() {
        return bedrock;
    }

    public void setBedrock(final boolean bedrock) {
        this.bedrock = bedrock;
    }

    public boolean isRulesAccepted() {
        return rulesAccepted;
    }

    public void setRulesAccepted(boolean rulesAccepted) {
        this.rulesAccepted = rulesAccepted;
    }

    public void setDiscordUser(DiscordUser discordUser) {
        this.discordUser = discordUser;
    }

    @Nullable
    public DiscordUser getDiscordUser() {
        return discordUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftUser that = (MinecraftUser) o;
        return bedrock == that.bedrock && rulesAccepted == that.rulesAccepted && Objects.equals(id, that.id) && Objects.equals(uuid, that.uuid) && Objects.equals(name, that.name) && Objects.equals(discordUser, that.discordUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, name, bedrock, rulesAccepted, discordUser);
    }
}
