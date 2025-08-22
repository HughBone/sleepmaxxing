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
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Main implements ModInitializer {

  public static final ArrayList<String> sleepMsgs = new ArrayList<>();
  public static final String wakeMsg = "Rise and shine :)";

  private void initWakeCommand() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      dispatcher.register(CommandManager
        .literal("wakeup")
        .then(CommandManager.argument("player", StringArgumentType.string()).executes((
          ctx -> {
            ServerPlayerEntity waker = ctx.getSource().getPlayer();
            if (waker == null) {
              return 1;
            }
            String sleeperName = StringArgumentType.getString(ctx, "player");
            String wakerName = waker.getNameForScoreboard();
            Text feedbackMsg;

            if (wakerName.equals(sleeperName)) {
              feedbackMsg = Text
                .literal("imagine waking yourself up \uD83D\uDE02")
                .formatted(Formatting.GRAY)
                .formatted(Formatting.ITALIC);

              waker.sendMessage(feedbackMsg, false);
              return 1;
            }

            ServerPlayerEntity sleeper = ctx
              .getSource()
              .getServer()
              .getPlayerManager()
              .getPlayerList()
              .stream()
              .filter(player -> sleeperName.equals(player.getNameForScoreboard()))
              .findFirst()
              .orElse(null);

            if (sleeper == null) {
              feedbackMsg = Text
                .literal(sleeperName + " is offline. Who you trying to wake up?")
                .formatted(Formatting.GRAY)
                .formatted(Formatting.ITALIC);
            } else if (sleeper.isSleeping()) {
              feedbackMsg = Text
                .literal("you woke up " + sleeperName + "!")
                .formatted(Formatting.GRAY)
                .formatted(Formatting.ITALIC);

              Text wakeupMsg = Text
                .literal("message from " + sleeperName + ": " + Main.wakeMsg)
                .formatted(Formatting.GRAY)
                .formatted(Formatting.ITALIC);
              sleeper.sendMessage(wakeupMsg, false);
              sleeper.wakeUp();
            } else {
              feedbackMsg = Text
                .literal(sleeperName + " is already awake.")
                .formatted(Formatting.GRAY)
                .formatted(Formatting.ITALIC);
            }

            waker.sendMessage(feedbackMsg, false);
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
