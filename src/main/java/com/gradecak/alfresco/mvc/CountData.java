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
