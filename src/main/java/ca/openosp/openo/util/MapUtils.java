/**
 * Copyright (c) 2023 WELL EMR Group Inc. This software is made available under the terms of the GNU
 * General Public License, Version 2, 1991 (GPLv2). License details are available via
 * "gnu.org/licenses/gpl-2.0.html".
 */
package ca.kai.util;

import java.util.Map;
import lombok.NonNull;

public class MapUtils {

  /**
   * Returns the value to which the specified key is mapped,
   * or value if this map contains no mapping for the key.
   * @param map the map to check
   * @param key the key for lookup in map
   * @param value the value to return if the key does not exist in the map
   * @return the mapped result, or value if the key does not exist in the map
   */
  public static <K, V> V getOrDefault(final @NonNull Map<K, V> map, final K key, final V value) {
    return map.containsKey(key) ? map.get(key) : value;
  }
}
