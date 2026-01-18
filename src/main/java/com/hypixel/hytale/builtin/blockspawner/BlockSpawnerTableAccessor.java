package com.hypixel.hytale.builtin.blockspawner;

import com.hypixel.hytale.common.map.IWeightedMap;

public class BlockSpawnerTableAccessor {
  public static void setEntries (BlockSpawnerTable table, IWeightedMap<BlockSpawnerEntry> entries) {
    table.entries = entries;
  }
}
