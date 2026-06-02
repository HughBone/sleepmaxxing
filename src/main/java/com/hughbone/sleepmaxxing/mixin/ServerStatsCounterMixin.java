package com.hughbone.sleepmaxxing.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// We register custom stats under the "sleepmaxxing" namespace and track them
// server-side. Those stat IDs don't exist on a vanilla client, so when the
// server sends them in a ClientboundAwardStatsPacket (e.g. when the player
// opens the stats screen), the client can't resolve the ID and crashes while
// decoding the packet.
//
// This redirects the packet construction in sendStats and strips out any stat
// whose id is in the sleepmaxxing namespace, so vanilla clients only ever
// receive stats they understand. The values are still stored + persisted
// server-side in world/stats/<uuid>.json.
@Mixin(ServerStatsCounter.class)
public class ServerStatsCounterMixin {

  @Redirect(
    method = "sendStats",
    at = @At(
      value = "NEW",
      target = "net/minecraft/network/protocol/game/ClientboundAwardStatsPacket"))
  private ClientboundAwardStatsPacket sleepmaxxing$filterStats(Object2IntMap<Stat<?>> stats) {
    Object2IntMap<Stat<?>> filtered = new Object2IntOpenHashMap<>();
    for (Object2IntMap.Entry<Stat<?>> entry : stats.object2IntEntrySet()) {
      Stat<?> stat = entry.getKey();
      Object value = stat.getValue();
      if (value instanceof Identifier id && id.getNamespace().equals("sleepmaxxing")) {
        continue;
      }
      filtered.put(stat, entry.getIntValue());
    }
    return new ClientboundAwardStatsPacket(filtered);
  }
}
