package com.hughbone.sleepmaxxing.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fabric's registry sync sends every entry of every SYNCED registry to the
// client at login. Our custom stats live in the (vanilla, SYNCED) custom_stat
// registry, so they'd be sent too -> vanilla clients don't have them and
// disconnect with a RemapException ("registry entries unknown to this client").
//
// Instead of stripping SYNCED off the whole custom_stat registry (which would
// stop ALL custom stats from syncing and needs impl-internal API), this strips
// only our "sleepmaxxing" entries out of the sync payload after Fabric builds
// it. Vanilla custom stats still sync normally; ours just stay server-side.
//
// The companion ServerStatsCounterMixin filters the same entries out of the
// per-player ClientboundAwardStatsPacket so the stats screen doesn't crash.
@Mixin(value = RegistrySyncManager.class, remap = false)
public class RegistrySyncManagerMixin {

  @Inject(method = "createAndPopulateRegistryMap", at = @At("RETURN"))
  private static void sleepmaxxing$dropCustomStats(
      CallbackInfoReturnable<Map<Identifier, Object2IntMap<Identifier>>> cir) {
    Map<Identifier, Object2IntMap<Identifier>> map = cir.getReturnValue();
    if (map == null) {
      return;
    }
    Identifier customStatRegistryId = Registries.CUSTOM_STAT.identifier();
    Object2IntMap<Identifier> customStats = map.get(customStatRegistryId);
    if (customStats == null) {
      return;
    }
    customStats.keySet().removeIf(id -> id.getNamespace().equals("sleepmaxxing"));
  }
}
