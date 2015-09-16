package com.gradecak.alfresco.mvc;

import java.util.List;

public class CountData<T> {
  public final Long count;
  public final List<T> dataList;
  public final boolean hasMore;

  public CountData(final Long count, final List<T> dataList) {
    this(count, dataList, false);
  }
  
  public CountData(final Long count, final List<T> dataList, final boolean hasMore) {
    this.count = count;
    this.dataList = dataList; // TODO could become an unmodifiable map at this point 
    this.hasMore = hasMore;
  }
}
