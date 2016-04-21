package com.gradecak.alfresco.mvc;

import org.springframework.util.StringUtils;

public class PaginationParams {

  private Integer start = 0;
  private Integer page = 1;
  private Integer limit = 25;
  private String sort;
  private Direction dir = Direction.ASC;

  public enum Direction {
    ASC, DESC
  }

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    if (start > 0) {
      this.start = start;
    }
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    if (page > 1) {
      this.page = page;
    }
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    if (limit > 0) {
      this.limit = limit;
    }
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    if (StringUtils.hasText(sort)) {
      this.sort = sort;
    }
  }

  public Direction getDir() {
    return dir;
  }

  public void setDir(Direction dir) {
    if(dir != null) {
      this.dir = dir;
    }
  }
}
