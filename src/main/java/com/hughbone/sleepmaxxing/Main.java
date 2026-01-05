package com.hughbone.sleepmaxxing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class Main implements ModInitializer {

  public static final ArrayList<String> sleepMsgs = new ArrayList<>();
  public static final String wakeMsg = "Rise and shine :)";

  private void initWakeCommand() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      dispatcher.register(Commands
        .literal("wakeup")
        .then(Commands.argument("player", StringArgumentType.string()).executes((
          ctx -> {
            ServerPlayer waker = ctx.getSource().getPlayer();
            if (waker == null) {
              return 1;
            }
            String sleeperName = StringArgumentType.getString(ctx, "player");
            String wakerName = waker.getScoreboardName();
            Component feedbackMsg;

            if (wakerName.equals(sleeperName)) {
              feedbackMsg = Component
                .literal("imagine waking yourself up \uD83D\uDE02")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC);

              waker.displayClientMessage(feedbackMsg, false);
              return 1;
            }

            ServerPlayer sleeper = ctx
              .getSource()
              .getServer()
              .getPlayerList()
              .getPlayers()
              .stream()
              .filter(player -> sleeperName.equals(player.getScoreboardName()))
              .findFirst()
              .orElse(null);

            if (sleeper == null) {
              feedbackMsg = Component
                .literal(sleeperName + " is offline. Who you trying to wake up?")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC);
            } else if (sleeper.isSleeping()) {
              feedbackMsg = Component
                .literal("you woke up " + sleeperName + "!")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC);

              Component wakeupMsg = Component
                .literal("message from " + sleeperName + ": " + Main.wakeMsg)
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC);
              sleeper.displayClientMessage(wakeupMsg, false);
              sleeper.stopSleeping();
            } else {
              feedbackMsg = Component
                .literal(sleeperName + " is already awake.")
                .withStyle(ChatFormatting.GRAY)
                .withStyle(ChatFormatting.ITALIC);
            }

            waker.displayClientMessage(feedbackMsg, false);
            return 1;
          }
        ))));
    });
  }

  @Override public void onInitialize() {
    // Populate sleepMsgs array by reading config file
    FileReader configReader = null;

    String configFilePath =
      System.getProperty("user.dir") + File.separator + "config" + File.separator + "sleepmaxxing"
        + ".json";
    Path configDirPath = Paths.get(System.getProperty("user.dir") + File.separator + "config");

    try {
      // Make sure config dir exists
      if (Files.notExists(configDirPath)) {
        Files.createDirectory(configDirPath);
      }

      // Get existing config file?
      File existingConfig = new File(configFilePath);

      // Write config file if not exists
      if (!(existingConfig.exists() && !existingConfig.isDirectory())) {
        InputStream inputStream = Main.class.getResourceAsStream("/default_config.json");
        FileOutputStream outputStream = new FileOutputStream(configFilePath);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
        existingConfig = new File(configFilePath);
      }

      configReader = new FileReader(existingConfig);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Parse json + populate sleepMsgs array
    if (configReader != null) {
      Gson gson = new Gson();
      JsonObject jsonObject = gson.fromJson(configReader, JsonObject.class);
      JsonArray loadedSleepMsgs = jsonObject.getAsJsonArray("sleepMsgs");

      for (JsonElement j : loadedSleepMsgs) {
        sleepMsgs.add(j.getAsString());
      }
    }

    initWakeCommand();

    System.out.println("sleepmaxxing loaded!");
  }

}
