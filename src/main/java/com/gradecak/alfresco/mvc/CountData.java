package com.gradecak.alfresco.mvc;

import java.util.List;

public class CountData<T> {
  public final Long count;
  public final List<T> dataList;

  public CountData(final Long count, final List<T> dataList) {
    this.count = count;
    this.dataList = dataList; // TODO could become an unmodifiable map at this point 
  }
}
