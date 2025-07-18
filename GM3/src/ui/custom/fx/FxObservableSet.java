package ui.custom.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Copy of ObservableSetWrapper that calls events in the Fx thread and uses synchronized methods for modification.
 * @param <E>
 */
public class FxObservableSet<E> implements ObservableSet<E> {
    private final Set<E> backingSet;
    private final List<SetChangeListener<? super E>> setChangeListeners = new ArrayList<>();
    private final List<InvalidationListener> invalidationListeners = new ArrayList<>();

    public FxObservableSet() {
        this(new java.util.concurrent.ConcurrentSkipListSet<>());
    }
    /**
     * Creates new instance of ObservableSet that wraps
     * the particular set specified by the parameter set.
     *
     * @param set the set being wrapped
     */
    public FxObservableSet(Set<E> set) {
        this.backingSet = set;
    }

    private class SimpleAddChange extends SetChangeListener.Change<E> {
        private final E added;

        public SimpleAddChange(E added) {
            super(FxObservableSet.this);
            this.added = added;
        }

        @Override
        public boolean wasAdded() {
            return true;
        }

        @Override
        public boolean wasRemoved() {
            return false;
        }

        @Override
        public E getElementAdded() {
            return added;
        }

        @Override
        public E getElementRemoved() {
            return null;
        }

        @Override
        public String toString() {
            return "added " + added;
        }

    }

    private class SimpleRemoveChange extends SetChangeListener.Change<E> {

        private final E removed;

        public SimpleRemoveChange(E removed) {
            super(FxObservableSet.this);
            this.removed = removed;
        }

        @Override
        public boolean wasAdded() {
            return false;
        }

        @Override
        public boolean wasRemoved() {
            return true;
        }

        @Override
        public E getElementAdded() {
            return null;
        }

        @Override
        public E getElementRemoved() {
            return removed;
        }

        @Override
        public String toString() {
            return "removed " + removed;
        }

    }

    private void callObservers(SetChangeListener.Change<E> change) {
        for (SetChangeListener<? super E> listener : setChangeListeners) {
            listener.onChanged(change);
        }
        for (InvalidationListener listener : invalidationListeners) {
            listener.invalidated(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(InvalidationListener listener) {
        invalidationListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(InvalidationListener listener) {
        invalidationListeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(SetChangeListener<?super E> observer) {
        setChangeListeners.add(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(SetChangeListener<?super E> observer) {
        setChangeListeners.remove(observer);
    }

    /**
     * Returns number of elements contained in this set.
     *
     * @see java.util.Set in JDK API documentation
     * @return number of elements contained in the set
     */
    @Override
    public int size() {
        return backingSet.size();
    }

    /**
     * Returns true if this set contains no elements.
     *
     * @see java.util.Set in JDK API documentation
     * @return true if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return backingSet.isEmpty();
    }

    /**
     * Returns true if this set contains specified element.
     *
     * @see java.util.Set in JDK API documentation
     * @param o an element that is being looked for
     * @return true if this set contains specified element
     */
    @Override
    public boolean contains(Object o) {
        return backingSet.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.
     * If the iterator's <code>remove()</code> method is called then the
     * registered observers are called as well.
     *
     * @see java.util.Set in JDK API documentation
     * @return an iterator over the elements in this set
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private final Iterator<E> backingIt = backingSet.iterator();
            private E lastElement;

            @Override
            public boolean hasNext() {
                return backingIt.hasNext();
            }

            @Override
            public E next() {
                lastElement = backingIt.next();
                return lastElement;
            }

            @Override
            public void remove() {
                backingIt.remove();
                callObservers(new SimpleRemoveChange(lastElement));
            }
        };
    }

    /**
     * Returns an array containing all of the elements in this set.
     *
     * @see java.util.Set in JDK API documentation
     * @return an array containing all of the elements in this set
     */
    @Override
    public Object[] toArray() {
        return backingSet.toArray();
    }

    /**
     * Returns an array containing all of the elements in this set.
     * The runtime type of the returned array is that of the specified array.
     *
     * @see java.util.Set in JDK API documentation
     * @param a the array into which the elements of this set are to be stored, if it is big enough;
     * otherwise, a new array of the same runtime type is allocated
     * @return an array containing all of the elements in this set
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return backingSet.toArray(a);
    }

    /**
     * Adds the specific element into this set and call all the
     * registered observers unless the set already contains the element.
     * Returns true in the case the element was added to the set.
     *
     * @see java.util.Set in JDK API documentation
     * @param o the element to be added to the set
     * @return true if the element was added
     */
    @Override
    public synchronized boolean add(E o) {
        boolean ret = backingSet.add(o);
        if (ret) {
            callObservers(new SimpleAddChange(o));
        }
        return ret;
    }

    /**
     * Removes the specific element from this set and call all the
     * registered observers if the set contained the element.
     * Returns true in the case the element was removed from the set.
     *
     * @see java.util.Set in JDK API documentation
     * @param o the element to be removed from the set
     * @return true if the element was removed
     */
    @Override
    public synchronized boolean remove(Object o) {
        boolean ret = backingSet.remove(o);
        if (ret) {
            //noinspection unchecked
            callObservers(new SimpleRemoveChange((E)o));
        }
        return ret;
    }

    /**
     * Test this set if it contains all the elements in the specified collection.
     * In such case returns true.
     *
     * @see java.util.Set in JDK API documentation
     * @param c collection to be checked for containment in this set
     * @return true if the set contains all the elements in the specified collection
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return backingSet.containsAll(c);
    }

    /**
     * Adds the elements from the specified collection.
     * Observers are called for each elements that was not already
     * present in the set.
     *
     * @see java.util.Set in JDK API documentation
     * @param c collection containing elements to be added to this set
     * @return true if this set changed as a result of the call
     */
    @Override
    public boolean addAll(Collection<?extends E> c) {
        boolean ret = false;
        for (E element : c) {
            ret |= add(element);
        }
        return ret;
    }

    /**
     * Keeps only elements that are included the specified collection.
     * All other elements are removed. For each removed element all the
     * observers are called.
     *
     * @see java.util.Set in JDK API documentation
     * @param c collection containing elements to be kept in this set
     * @return true if this set changed as a result of the call
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return removeRetain(c, false);
    }

    /**
     * Removes all the elements that are contained in the specified
     * collection. Observers are called for each removed element.
     *
     * @see java.util.Set in JDK API documentation
     * @param c collection containing elements to be removed from this set
     * @return true if this set changed as a result of the call
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return removeRetain(c, true);
    }

    private synchronized boolean removeRetain(Collection<?> c, boolean remove) {
        boolean removed = false;
        for (Iterator<E> i = backingSet.iterator(); i.hasNext();) {
            E element = i.next();
            if (remove == c.contains(element)) {
                removed = true;
                i.remove();
                callObservers(new SimpleRemoveChange(element));
            }
        }
        return removed;
    }

    /**
     * Removes all the elements from this set. Observers are called
     * for each element.
     * @see java.util.Set in JDK API documentation
     */
    @Override
    public synchronized void clear() {
        for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
            E element = i.next();
            i.remove();
            callObservers(new SimpleRemoveChange(element));
        }
    }

    /**
     * Returns the String representation of the wrapped set.
     * @see java.lang.Object in JDK API documentation
     * @return the String representation of the wrapped set
     */
    @Override
    public String toString() {
        return backingSet.toString();
    }

    /**
     * Indicates whether some other object is "equal to" the wrapped set.
     * @see java.lang.Object in JDK API documentation
     * @param obj the reference object with which to compare
     * @return true if the wrapped is equal to the obj argument
     */
    @Override
    public boolean equals(Object obj) {
        return backingSet.equals(obj);
    }

    /**
     * Returns the hash code for the wrapped set.
     * @see java.lang.Object in JDK API documentation
     * @return the hash code for the wrapped set
     */
    @Override
    public int hashCode() {
        return backingSet.hashCode();
    }
}
