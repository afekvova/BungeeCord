package ru.afek.auth.utils;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class WhiteList {

    @Getter
    private Set<String> players;
    private final Gson gson = new GsonBuilder().create();
    private File cdFile;

    public WhiteList() {
        this.players = Sets.newConcurrentHashSet();
        this.load();
    }

    public boolean isPlayer(String playerName) {
        return this.players.contains(playerName.toLowerCase());
    }

    public void addPlayer(String playerName) {
        this.players.add(playerName.toLowerCase());
    }

    public void removePlayer(String playerName) {
        this.players.remove(playerName.toLowerCase());
    }

    private void load() {
        File cdFile = new File("Auth",
                "data.json");
        if (!cdFile.exists()) {
            try {
                cdFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.cdFile = cdFile;
        Type type = new TypeToken<Set<String>>() {
        }.getType();
        Set<String> tempMap;
        try {
            tempMap = gson.fromJson(
                    new String(Files.readAllBytes(cdFile.toPath())), type);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (tempMap == null)
            return;
        if (tempMap.isEmpty())
            return;

        tempMap.forEach(player -> this.players.add(player.toLowerCase()));
    }

    public void saveUsers() {
        try {
            Files.write(cdFile.toPath(),
                    gson.toJson(this.players).getBytes(
                            StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
