/*
 *
 * Copyright 2015 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.github.wnameless.json.flattener;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * {@link IndexedPeekIterator} is an Iterator which provides user a
 * {@link #peek()} method to peek an element advanced, a {@link #getIndex()}
 * method to get the index of last returned element and a {@link #getCurrent()}
 * method to get the last returned element itself.
 * 
 * @param <E>
 *          the type of elements
 * 
 * @author Wei-Ming Wu
 * 
 */
public final class IndexedPeekIterator<E> implements Iterator<E> {

  /**
   * Creates an {@link IndexedPeekIterator} by given Iterable.
   * 
   * @param iter
   *          any Iterable
   * @return an {@link IndexedPeekIterator}
   */
  public static <T> IndexedPeekIterator<T> newIndexedPeekIterator(
      Iterable<T> iter) {
    return new IndexedPeekIterator<T>(iter.iterator());
  }

  private final Iterator<? extends E> iterator;
  private E peek;
  private boolean hasPeek = false;
  private int index = -1;
  private E current = null;

  /**
   * Creates an {@link IndexedPeekIterator}.
   * 
   * @param iterator
   *          an Iterator
   */
  public IndexedPeekIterator(Iterator<? extends E> iterator) {
    if (iterator == null) throw new NullPointerException();

    this.iterator = iterator;
  }

  private void peeking() {
    peek = iterator.next();
    hasPeek = true;
  }

  /**
   * Returns the index of last returned element. If there is no element has been
   * returned, it returns -1.
   * 
   * @return the index of last returned element
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the last returned element. If {@link #next()} has never been
   * called, it returns null.
   * 
   * @return the last returned element
   */
  public E getCurrent() {
    return current;
  }

  @Override
  public boolean hasNext() {
    return hasPeek || iterator.hasNext();
  }

  @Override
  public E next() {
    if (!hasNext()) throw new NoSuchElementException();

    index++;
    if (hasPeek) {
      hasPeek = false;
      return current = peek;
    } else {
      peeking();
      return next();
    }
  }

  @Override
  public void remove() {
    if (hasPeek) throw new IllegalStateException();

    iterator.remove();
  }

  /**
   * Peeks an element advanced. Warning: remove() is temporarily out of function
   * after a peek() until a next() is called.
   * 
   * @return element
   */
  public E peek() {
    if (!hasPeek && hasNext()) peeking();
    if (!hasPeek) throw new NoSuchElementException();

    return peek;
  }

}
