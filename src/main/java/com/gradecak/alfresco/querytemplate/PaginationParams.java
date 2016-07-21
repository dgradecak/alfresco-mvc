/**
 * Copyright gradecak.com

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradecak.alfresco.querytemplate;

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
    if (dir != null) {
      this.dir = dir;
    }
  }
}
