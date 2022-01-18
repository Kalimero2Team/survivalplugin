package com.kalimero2.team.survivalplugin.database;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.kalimero2.team.survivalplugin.database.pojo.DiscordUser;
import com.kalimero2.team.survivalplugin.database.pojo.MinecraftUser;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MongoDB {

    private MongoCollection<MinecraftUser> collection;
    private List<MinecraftUser> users;

    public MongoDB(String uri, String db) {
        ConnectionString connectionString = new ConnectionString(uri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        MongoClient client = MongoClients.create(settings);

        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        MongoDatabase database = client.getDatabase(db).withCodecRegistry(pojoCodecRegistry);

        this.collection = database.getCollection("minecraftUser", MinecraftUser.class);

    }

    public MongoCollection<MinecraftUser> getCollection() {
        return collection;
    }

    public List<MinecraftUser> getUsers() {
        if (users == null) {
            return getUsers(true);
        }
        return getUsers(false);
    }

    public List<MinecraftUser> getUsers(boolean force) {
        List<MinecraftUser> minecraftUsers = new ArrayList<>();

        collection.find().forEach(minecraftUsers::add);

        users = minecraftUsers;

        return minecraftUsers;
    }

    public MinecraftUser getUser(UUID uuid) {
        return collection.find(eq("uuid", uuid.toString())).first();
    }

    public List<MinecraftUser> getUsers(String id) {
        List<MinecraftUser> users = new ArrayList<>();
        collection.find(eq("discordUser.discordId", id)).forEach(users::add);
        return users;
    }

    public List<MinecraftUser> getUserAlts(MinecraftUser user){
        List<MinecraftUser> users = new ArrayList<>();
        DiscordUser discordUser = user.getDiscordUser();
        for(MinecraftUser minecraftUser:this.getUsers()){
            if(minecraftUser.getDiscordUser() != null){
                if(minecraftUser.getDiscordUser().getDiscordId().equals(discordUser.getDiscordId())){
                    users.add(minecraftUser);
                }
            }
        }
        return users;
    }

    public void addUser(MinecraftUser user) {
        if(this.users == null)
            this.getUsers();
        this.users.add(user);
        collection.insertOne(user);
    }

    public void updateUser(MinecraftUser user) {
        List<MinecraftUser> minecraftUsers = new ArrayList<>();

        for(MinecraftUser minecraftUser:this.getUsers()){
            if(!user.getUuid().equals(minecraftUser.getUuid())){
                minecraftUsers.add(minecraftUser);
            }
        }

        this.users = minecraftUsers;
        collection.replaceOne(eq("uuid",user.getUuid()),user);
    }

}
