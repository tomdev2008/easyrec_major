package org.easyrec.utils.collection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fkleedorfer on 22.02.2016.
 */
public class CollectionUtils
{

  /**
   * Safe calculation of sublist, catching edge cases with safe defaults.
   * @param items the list to get a sublist of
   * @param offset the start index (0-based)
   * @param count the number of items in the list
   * @return a list containing at most <code>count</code> items.
   */
  public static <T> List<T> getSafeSubList(final List<T> items, final Integer offset, final Integer count) {
    if (items == null) return new ArrayList<>();
    int itemsCount = items.size();
    if (items.size() == 0) return new ArrayList<>();
    int offsetInt = offset == null ? 0 : Math.max(offset, 0);
    int endOffsetInt = count == null ? offsetInt : Math.min(offsetInt + Math.max(count, 0), itemsCount);
    return items.subList(offsetInt, endOffsetInt);
  }

  /**
   * Returns 0 if the specified offset is null or negative.
   * @param offset
   *
   */
  public static int getSafeOffset(final Integer offset) {
    return (offset == null || offset < 0)?0:offset;
  }
}
