package com.hypixel.hytale.builtin.blockspawner;

import com.hypixel.hytale.common.map.IWeightedMap;

// Split package accessors = extra cursed
// TODO: Just switch to VarHandle or Hyxin
public class BlockSpawnerTableAccessor {
  public static void setEntries (BlockSpawnerTable table, IWeightedMap<BlockSpawnerEntry> entries) {
    table.entries = entries;
  }
}
