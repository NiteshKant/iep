/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.iep.http;

import java.util.Iterator;

final class SLists {

  private static final SList<?> EMPTY = new SList.Nil<>();

  private SLists() {
  }

  @SuppressWarnings("unchecked")
  static <T> SList<T> empty() {
    return (SList<T>) EMPTY;
  }

  static <T> SList<T> create(Iterator<T> iter) {
    SList<T> data = empty();
    while (iter.hasNext()) {
      data = data.prepend(iter.next());
    }
    return data;
  }

  static <T> SList<T> create(Iterable<T> vs) {
    return create(vs.iterator());
  }
}