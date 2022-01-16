package me.byquanton.survivalplugin.database.pojo;

import org.bson.types.ObjectId;

import java.util.Objects;

public class DiscordUser {
    private ObjectId id;
    private String discordId;

    public DiscordUser() {
    }

    public DiscordUser(final String discordId) {
        this.discordId = discordId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordUser that = (DiscordUser) o;
        return Objects.equals(id, that.id) && Objects.equals(discordId, that.discordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, discordId);
    }
}
