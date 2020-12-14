package HashTable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HashTable<K,V> implements Map<Object, V> {

    private final int TABLE_SIZE;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private int count;
    private Entry<?,?>[] table;
    private float loadFactor;
    private int countMod; // сколько раз таблицу изменяли
    private int threshold; // порог для перехеширования

    public HashTable(int initialCapacity, float loadFactor) {
        this.TABLE_SIZE = initialCapacity;
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load: "+loadFactor);

        if (initialCapacity==0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        table = new Entry<?,?>[initialCapacity];
        threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
        count = 0;
    }

    public HashTable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public HashTable() {
        this(11, 0.75f);
    }

    public HashTable(Map<? extends K, ? extends V> m) {
        this(Math.max(2*m.size(), 11), 0.75f);
        putAll(m);
    }

    private int getPrime() {
        for (int i = TABLE_SIZE - 1; i >= 1; i--)
        {
            int fact = 0;
            for (int j = 2; j <= (int) Math.sqrt(i); j++)
                if (i % j == 0)
                    fact++;
            if (fact == 0)
                return i;
        }
        return 3;
    }

    private int hash1(Object key) {
        return key.hashCode() % TABLE_SIZE;
    }
    private int hash2(Object key) {
        int prime = getPrime();
        return prime - (key.hashCode() % prime);
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    public  Enumeration<V> elements() {
        return this.getEnumeration(VALUES);
    }

    public  Enumeration<K> keys() {
        return this.getEnumeration(KEYS);
    }


    @Override
    public boolean containsKey(Object key) {
        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        int i = 0;
        for (Entry<?,?> e = tab[index1]; e != null && i < count;
             index1 = (index1 + index2) % TABLE_SIZE, e = tab[index1]) {
            if ((e.hash == hash) && e.key.equals(key)) {
                return true;
            }
            i++;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return contains(value);
    }

    @Override
    public V get(Object key) {
        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index = hash1(key);
        int index2 = hash2(key);
        Entry<?, ?> e = tab[index];
        int i = 0;
        while (e != null && i < count){
            if ((e.hash == hash) && e.key.equals(key)) {
                return (V) e.value;
            }
            index = (index + index2) % TABLE_SIZE;
            i++;
            e = tab[index];
        }
        return null;
    }

    @Override
    public V put(Object key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }

        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        int i = 0;
        Entry<K,V> entry = (Entry<K,V>)tab[index1];
        while (entry != null  && i < count) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                entry.value = value;
                return old;
            }
            index1 = (index1 + index2) % TABLE_SIZE;
            i++;
            entry = (Entry<K,V>)tab[index1];
        }

        addEntry(hash, (K) key, value, index1);
        return null;
    }

    protected void rehash() {
        int oldCapacity = table.length;
        Entry<?,?>[] oldMap = table;

        int newCapacity = (oldCapacity << 1) + 1;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (oldCapacity == MAX_ARRAY_SIZE)
                return;
            newCapacity = MAX_ARRAY_SIZE;
        }
        Entry<?,?>[] newMap = new Entry<?,?>[newCapacity];

        countMod++;
        threshold = (int)Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry<K,V> old = (Entry<K,V>)oldMap[i]; old != null ; ) {
                Entry<K,V> e = old;
                old = old.next;

                int index1 = e.hash % newCapacity;
                e.next = (Entry<K,V>)newMap[index1];
                newMap[index1] = e;
            }
        }
    }

    private void addEntry(int hash, K key, V value, int index1) {
        Entry<?, ?>[] tab = table;
        if (count >= threshold) {
            rehash();

            tab = table;
            hash = key.hashCode();
            index1 = hash % tab.length;
        }

        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>) tab[index1];
        tab[index1] = new Entry<>(hash, key, value, e);
        count++;
        countMod++;
    }

    @Override
    public V remove(Object key) {
        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        int i = 0;
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        for(Entry<K,V> prev = null; e != null && i < count; index1 = (index1 + index2) % TABLE_SIZE,
                prev = e, e = (Entry<K,V>)tab[index1], i++) {
            if ((e.hash == hash) && e.key.equals(key)) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index1] = e.next;
                }
                countMod++;
                count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<?, ? extends V> m) {
        for (Map.Entry<?, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    @Override
    public void clear() {
        Entry<?, ?>[] tab = table;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        countMod++;
        count = 0;
    }

    private Set keySet;
    private Set<Map.Entry<Object, V>> entrySet;
    private Collection<V> values;

    @Override
    public Set<Object> keySet() {
        if (keySet == null)
            keySet = Collections.synchronizedSet(new KeySet());
        return keySet;
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return getIterator(KEYS);
        }
        public int size() {
            return count;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            return HashTable.this.remove(o) != null;
        }
        public void clear() {
            HashTable.this.clear();
        }
    }

    @Override
    public Collection<V> values() {
        if (values==null)
            values = Collections.synchronizedCollection(new ValueCollection());
        return values;
    }

    private class ValueCollection extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return getIterator(VALUES);
        }
        public int size() {
            return count;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            HashTable.this.clear();
        }
    }

    @Override
    public Set<Map.Entry<Object, V>> entrySet() {
        if (entrySet==null)
            entrySet = Collections.synchronizedSet(new HashTable.EntrySet());
        return entrySet;
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return getIterator(ENTRIES);
        }

        public boolean add(Map.Entry<K,V> o) {
            return super.add(o);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
            Object key = entry.getKey();
            HashTable.Entry<?,?>[] tab = table;
            int hash = key.hashCode();
            int index1 = hash1(key);
            int index2 = hash2(key);
            int i = 0;
            for (HashTable.Entry<?,?> e = tab[index1]; e != null && i < count;
                 index1 = (index1 + index2) % TABLE_SIZE, e = tab[index1], i++)
                if (e.hash == hash && e.equals(entry))
                    return true;
            return false;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object key = entry.getKey();
            HashTable.Entry<?,?>[] tab = table;
            int hash = key.hashCode();
            int index1 = hash1(key);
            int index2 = hash2(key);

            @SuppressWarnings("unchecked")
            HashTable.Entry<K,V> e = (HashTable.Entry<K,V>)tab[index1];
            HashTable.Entry<K,V> prev = null;
            int i = 0;
            while (e != null && i < count) {
                if (e.hash==hash && e.equals(entry)) {
                    if (prev != null)
                        prev.next = e.next;
                    else
                        tab[(index1 + index2) % TABLE_SIZE] = e.next;

                    e.value = null;
                    countMod++;
                    count--;
                    return true;
                }
                index1 = (index1 + index2) % TABLE_SIZE;
                i++;
                prev = e;
                e = (HashTable.Entry<K,V>)tab[index1];
            }
            return false;
        }

        public int size() {
            return count;
        }

        public void clear() {
            HashTable.this.clear();
        }
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V result = get(key);
        return (null == result) ? defaultValue : result;
    }

    @Override
    public void forEach(BiConsumer<? super Object, ? super V> action) {
        Objects.requireNonNull(action);
        final int expectedCountMod = countMod;

        Entry<?, ?>[] tab = table;
        for (Entry<?, ?> entry : tab) {
            while (entry != null) {
                action.accept((K)entry.key, (V)entry.value);
                entry = entry.next;

                if (expectedCountMod != countMod) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super Object, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        final int expectedCountMod = countMod;
        Entry<K, V>[] tab = (Entry<K, V>[])table;
        for (Entry<K, V> entry : tab) {
            while (entry != null) {
                entry.value = Objects.requireNonNull(
                        function.apply(entry.key, entry.value));
                entry = entry.next;

                if (expectedCountMod != countMod) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    @Override
    public V putIfAbsent(Object key, V value) {
        Objects.requireNonNull(value);

        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> entry = (Entry<K,V>)tab[index1];
        int i = 0;
        while (entry != null && i < count) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                if (old == null) {
                    entry.value = value;
                }
                return old;
            }
            index1 = (index1 + index2) % TABLE_SIZE;
            i++;
            entry = (Entry<K,V>)tab[index1];
        }

        addEntry(hash, (K) key, value, index1);
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        Objects.requireNonNull(value);

        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        Entry<K,V> prev = null;
        int i = 0;
        while (e != null && i < count) {
            if ((e.hash == hash) && e.key.equals(key) && e.value.equals(value)) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index1] = e.next;
                }
                e.value = null;
                countMod++;
                count--;
                return true;
            }
            index1 = (index1 + index2) % TABLE_SIZE;
            i++;
            prev = e;
            e = (Entry<K,V>)tab[index1];
        }
        return false;
    }

    @Override
    public boolean replace(Object key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null) return false;
        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        int i = 0;
        while (e != null && i < count) {
            if ((e.hash == hash) && e.key.equals(key)) {
                if (e.value.equals(oldValue)) {
                    e.value = newValue;
                    return true;
                } else {
                    return false;
                }
            }
            index1 = (index1 + index2) % TABLE_SIZE;
            i++;
            e = (Entry<K,V>)tab[index1];
        }
        return false;
    }

    @Override
    public V replace(Object key, V value) {
        Objects.requireNonNull(value);
        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        int i = 0;
        while (e != null && i < count) {
            if ((e.hash == hash) && e.key.equals(key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
            index1 = (index1 + index2) % TABLE_SIZE;
            i++;
            e = (Entry<K,V>)tab[index1];
        }
        return null;
    }

    @Override
    public V computeIfAbsent(Object key, Function<? super Object, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);

        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        int i =0;
        while (e != null && i < count) {
            if (e.hash == hash && e.key.equals(key)) {
                return e.value;
            }
            index1 = (index2 + index1) % TABLE_SIZE;
            i++;
            e = (Entry<K,V>)tab[index1];
        }

        int mc = countMod;
        V newValue = mappingFunction.apply(key);
        if (mc != countMod) { throw new ConcurrentModificationException(); }
        if (newValue != null) {
            addEntry(hash, (K) key, newValue, index1);
        }

        return newValue;
    }

    @Override
    public V computeIfPresent(Object key, BiFunction<? super Object, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        Entry<K,V> prev = null;
        int i = 0;
        while (e != null && i < count) {
            if (e.hash == hash && e.key.equals(key)) {
                int mc = countMod;
                V newValue = remappingFunction.apply(key, e.value);
                if (mc != countMod) {
                    throw new ConcurrentModificationException();
                }
                if (newValue == null) {
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index1] = e.next;
                    }
                    countMod = mc + 1;
                    count--;
                } else {
                    e.value = newValue;
                }
                return newValue;
            }
            index1 = (index2 + index1) % TABLE_SIZE;
            i++;
            prev = e;
            e = (Entry<K,V>)tab[index1];
        }
        return null;
    }

    @Override
    public V compute(Object key, BiFunction<? super Object, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        Entry<?, ?>[] tab = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        Entry<K,V> prev = null;
        int i = 0;
        while (e != null && i < count) {
            if (e.hash == hash && Objects.equals(e.key, key)) {
                int mc = countMod;
                V newValue = remappingFunction.apply(key, e.value);
                if (mc != countMod) {
                    throw new ConcurrentModificationException();
                }
                if (newValue == null) {
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index1] = e.next;
                    }
                    countMod = mc + 1;
                    count--;
                } else {
                    e.value = newValue;
                }
                return newValue;
            }
            index1 = (index2 + index1) % TABLE_SIZE;
            i++;
            prev = e;
            e = (Entry<K,V>)tab[index1];
        }

        int mc = countMod;
        V newValue = remappingFunction.apply(key, null);
        if (mc != countMod) { throw new ConcurrentModificationException(); }
        if (newValue != null) {
            addEntry(hash, (K) key, newValue, index1);
        }

        return newValue;
    }

    @Override
    public V merge(Object key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        Entry<?,?> tab[] = table;
        int hash = key.hashCode();
        int index1 = hash1(key);
        int index2 = hash2(key);
        @SuppressWarnings("unchecked")
        Entry<K,V> e = (Entry<K,V>)tab[index1];
        Entry<K,V> prev = null;
        int i = 0;
        while (e != null && i < count) {
            if (e.hash == hash && e.key.equals(key)) {
                int mc = countMod;
                V newValue = remappingFunction.apply(e.value, value);
                if (mc != countMod) {
                    throw new ConcurrentModificationException();
                }
                if (newValue == null) {
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index1] = e.next;
                    }
                    countMod = mc + 1;
                    count--;
                } else {
                    e.value = newValue;
                }
                return newValue;
            }
            index1 = (index2 + index1) % TABLE_SIZE;
            i++;
            prev = e;
            e = (Entry<K,V>)tab[index1];
        }

        if (value != null) {
            addEntry(hash, (K) key, value, index1);
        }

        return value;
    }

    public  boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?,?> t = (Map<?,?>) o;
        if (t.size() != size())
            return false;

        try {
            for (Map.Entry<Object, V> e : entrySet()) {
                Object key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(t.get(key) == null && t.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(t.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused)   {
            return false;
        }

        return true;
    }

    public boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        Entry<?,?> []tab = table;
        for (int i = tab.length ; i-- > 0 ;) {
            for (Entry<?,?> e = tab[i]; e != null ; e = e.next) {
                if (e.value.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        int max = size() - 1;
        if (max == -1)
            return "{}";

        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<Object, V>> it = entrySet().iterator();

        sb.append('{');
        for (int i = 0; ; i++) {
            Map.Entry<Object, V> e = it.next();
            Object key = e.getKey();
            V value = e.getValue();
            sb.append(key   == this ? "(this Map)" : key.toString());
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value.toString());

            if (i == max)
                return sb.append('}').toString();
            sb.append(", ");
        }
    }

    public  int hashCode() {
        int h = 0;
        if (count == 0 || loadFactor < 0)
            return h;

        loadFactor = -loadFactor;
        Entry<?,?>[] tab = table;
        for (Entry<?,?> entry : tab) {
            while (entry != null) {
                h += entry.hashCode();
                entry = entry.next;
            }
        }
        loadFactor = -loadFactor;
        return h;
    }

    private <T> Enumeration<T> getEnumeration(int type) {
        if (count == 0) {
            return Collections.emptyEnumeration();
        } else {
            return new Enumerator<>(type, false);
        }
    }

    private <T> Iterator<T> getIterator(int type) {
        if (count == 0) {
            return Collections.emptyIterator();
        } else {
            return new Enumerator<>(type, true);
        }
    }

    static class Entry<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Entry<K,V> next;

        protected Entry(int hash, K key, V value, Entry<K,V> next) {
            this.hash = hash;
            this.key =  key;
            this.value = value;
            this.next = next;
        }

        public K getKey() {
            return key;
        }
        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            if (value == null)
                throw new NullPointerException();

            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

            return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
                    (value==null ? e.getValue()==null : value.equals(e.getValue()));
        }

        public int hashCode() {
            return hash ^ Objects.hashCode(value);
        }

        public String toString() {
            return key.toString()+" = "+value.toString();
        }
    }

    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
        Entry<?,?>[] table = HashTable.this.table;
        int index = table.length;
        Entry<?,?> entry;
        Entry<?,?> last;
        final int type;
        final boolean iterator;
        protected int expectedCountMod = HashTable.this.countMod;

        Enumerator(int type, boolean iterator) {
            this.type = type;
            this.iterator = iterator;
        }

        @Override
        public boolean hasMoreElements() {
            Entry<?,?> e = entry;
            int i = index;
            Entry<?,?>[] t = table;
            while (e == null && i > 0) {
                e = t[--i];
            }
            entry = e;
            index = i;
            return e != null;
        }

        @Override
        public T nextElement() {
            Entry<?,?> et = entry;
            int i = index;
            Entry<?,?>[] t = table;
            while (et == null && i > 0) {
                et = t[--i];
            }
            entry = et;
            index = i;
            if (et != null) {
                Entry<?,?> e = last = entry;
                entry = e.next;
                return type == KEYS ? (T)e.key : (type == VALUES ? (T)e.value : (T)e);
            }
            throw new NoSuchElementException("Hashtable Enumerator");
        }

        @Override
        public boolean hasNext() {
            return hasMoreElements();
        }

        @Override
        public T next() {
            if (HashTable.this.countMod != expectedCountMod)
                throw new ConcurrentModificationException();
            return nextElement();
        }

        @Override
        public void remove() {
            if (!iterator)
                throw new UnsupportedOperationException();
            if (last == null)
                throw new IllegalStateException("Hashtable Enumerator");
            if (countMod != expectedCountMod)
                throw new ConcurrentModificationException();

            synchronized(HashTable.this) {
                Entry<?,?>[] tab = HashTable.this.table;
                int index1 = hash1(last);
                int index2 = hash2(last);

                Entry<K,V> e = (Entry<K,V>)tab[(index1 + index2) % tab.length];
                Entry<K,V> prev = null;
                int i = 0;
                while (e != null && i < count) {
                    if (e == last) {
                        if (prev == null)
                            tab[index1] = e.next;
                        else
                            prev.next = e.next;
                        expectedCountMod++;
                        last = null;
                        HashTable.this.countMod++;
                        HashTable.this.count--;
                        return;
                    }
                    index1 = (index2 + index1) % TABLE_SIZE;
                    i++;
                    prev = e;
                    e = (Entry<K,V>)tab[index1];
                }
                throw new ConcurrentModificationException();
            }
        }
    }
}