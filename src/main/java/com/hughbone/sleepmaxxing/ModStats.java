package com.hughbone.sleepmaxxing;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class ModStats {

  // Times this player kicked someone else out of bed
  public static Identifier KICKS_GIVEN;
  // Times this player was kicked out of bed by someone else
  public static Identifier TIMES_KICKED_OUT;
  // Times this player won a sleep war (kicked 3+ times in a minute, then slept)
  public static Identifier SLEEP_WAR_WINS;

  public static void register() {
    KICKS_GIVEN = makeCustomStat("sleep_kicks_given", StatFormatter.DEFAULT);
    TIMES_KICKED_OUT = makeCustomStat("sleep_kicked_out", StatFormatter.DEFAULT);
    SLEEP_WAR_WINS = makeCustomStat("sleep_war_wins", StatFormatter.DEFAULT);
  }

  private static Identifier makeCustomStat(String name, StatFormatter formatter) {
    Identifier id = Identifier.fromNamespaceAndPath("sleepmaxxing", name);
    Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
    Stats.CUSTOM.get(id, formatter);
    return id;
  }
}
