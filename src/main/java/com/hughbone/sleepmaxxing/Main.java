package com.hughbone.sleepmaxxing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {

  public static final ArrayList<String> sleepMsgs = new ArrayList<>();
  public static final String wakeMsg = "Rise and shine :)";

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

    System.out.println("sleepmaxxing loaded!");
  }

}
