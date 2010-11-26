package org.open2jam.util;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
/**
 *
 * @author fox
 */
public class WeakLinkedList<E> implements Collection<E>, Queue<E>
{
    private LinkedList<WeakReference<E>> list;

    public WeakLinkedList(){
         list = new  LinkedList<WeakReference<E>>();
    }

    public boolean add(E o)
    {
        return list.add(new WeakReference(o));
    }

    public E removeFirst()
    {
        E o = null;
        while(o == null && !list.isEmpty())o = list.removeFirst().get();
        return o;
    }

    public E getFirst()
    {
        E o = null;
        while(!list.isEmpty())
        {
            o = list.getFirst().get();
            if(o == null)list.removeFirst();
            else break;
        }
        return o;
    }

    public boolean offer(E e) {
        return add(e);
    }

    public E remove() {
        return removeFirst();
    }

    public E poll() {
        return removeFirst();
    }

    public E element() {
        return getFirst();
    }

    public E peek() {
        return getFirst();
    }

    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T[] toArray(T[] ts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addAll(Collection<? extends E> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean retainAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        list.clear();
    }
}
