import HashTable.HashTable;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestHashTable {

    @Test
    void putTest(){
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        assertEquals(0, currentTable.size(), "Hash Table is not empty.");
        Hashtable<Integer, Integer> controlTable = new Hashtable<>();
        Random random = new Random();
        for (int i = 1; i <= 50; i++) {
            int nextInt = random.nextInt(100);
            assertNull(currentTable.put(i, nextInt));
            controlTable.put(i, nextInt);
            assertEquals(controlTable.size(), currentTable.size(),
                    "The size of the map is not as expected.");
        }
        currentTable.put(5, 23);
        Integer previous = currentTable.get(5);
        assertEquals(currentTable.put(5,45), previous);
        // putIfAbsent
        currentTable.put(51, 0);
        assertEquals(currentTable.get(51), currentTable.putIfAbsent(51, 51));
        assertNull(currentTable.putIfAbsent(52, 222));
    }

    @Test
    void containsTest(){
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        currentTable.put(5, 23);
        assertTrue(currentTable.contains(23));
        assertTrue(currentTable.containsKey(5));
        assertFalse(currentTable.containsKey(3));
        assertFalse(currentTable.contains(12));
        assertTrue(currentTable.containsValue(23));
    }
    @Test
    void getTest(){
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        currentTable.put(5, 45);
        assertEquals(45, currentTable.get(5));
        assertNull(currentTable.get(2));
        assertNull(currentTable.get("1"));
        currentTable.put(2, 85);
        assertEquals(currentTable.getOrDefault(2, 22), 85);
        assertEquals(currentTable.getOrDefault(3, -1), -1);
    }

    @Test
    void clearTest(){
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        for (int i = 1; i <= 10; i++)
            currentTable.put(i, new Random().nextInt(100));
        currentTable.clear();
        assertEquals(currentTable.size(), 0);
    }

    @Test
    void removeTest(){
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        for (int i = 1; i <= 10; i++)
            currentTable.put(i, new Random().nextInt(100));
        Integer element = currentTable.get(2);
        Integer actual = currentTable.remove(2);
        assertEquals(element, actual);
        assertEquals(9, currentTable.size());
        assertNull(currentTable.remove(12));
        assertTrue(currentTable.remove(9, currentTable.get(9)));
        assertFalse(currentTable.remove(1, 12));
    }

    @Test
    void replaceTest() {
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        for (int i = 1; i <= 10; i++)
            currentTable.put(i, new Random().nextInt(100));

        assertFalse(currentTable.replace(0, 0,1));
        Integer old = currentTable.get(1);
        currentTable.put(11, 25);
        assertTrue(currentTable.replace(11, 25, 10));

        assertNull(currentTable.replace(0, 0));
        old = currentTable.get(7);
        assertEquals(old, currentTable.replace(7, 99));

    }

    @Test
    void equalsTest() {
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        Hashtable<Integer, Integer> controlTable = new Hashtable<>();
        HashTable<Integer, Integer> equalsTable = new HashTable<Integer, Integer>();
        Random random = new Random();
        for (int i = 1; i <= 5; i++){
            Integer value = random.nextInt(100);
            currentTable.put(i, value);
            controlTable.put(i, value);
            equalsTable.put(i, value);
        }
        assertTrue(currentTable.equals(equalsTable));
        assertTrue(currentTable.equals(controlTable));
        assertEquals(currentTable.toString(), equalsTable.toString());
        assertEquals(currentTable.hashCode(), equalsTable.hashCode());
        currentTable.remove(3);
        assertFalse(currentTable.equals(equalsTable));

    }

    @Test
    void forEachTest(){
        Hashtable<Integer, Integer> controlTable = new Hashtable<>();
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        for (int i = 1; i <= 5; i++) {
            currentTable.put(i, i);
            controlTable.put(i, i + 1);
        }
        currentTable.forEach((k, v) -> {
            v = v + 1;
            currentTable.replace(k, v);
        });
        assertEquals(controlTable, currentTable);
    }

    @Test
    void keySetTest() {
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        Collection list = Collections.synchronizedSet(new HashSet<>(Arrays.asList(5, 4, 3, 2, 1)));
        for (int i = 1; i <= 5; i++)
            currentTable.put(i, i);
        assertEquals(list, currentTable.keySet());
    }

    @Test
    void valuesTest() {
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        Hashtable<Integer, Integer> expected = new Hashtable();
        for (int i = 1; i <= 3; i++) {
            currentTable.put(i, i * i);
            expected.put(i, i * i);
        }
        assertEquals(expected.values(), currentTable.values());
    }

    @Test
    void entrySet() {
        HashTable<Integer, Integer> currentTable = new HashTable<Integer, Integer>();
        Hashtable<Integer, Integer> expected = new Hashtable();
        for (int i = 1; i <= 3; i++) {
            currentTable.put(i, i * 10 + i);
            expected.put(i, i * 10 + i);
        }
        currentTable.put(8, 88);
        expected.put(8, 88);
        assertEquals(expected.entrySet(), currentTable.entrySet());
    }


    @Test
    void mergeTest() {
        HashTable<Integer, String> currentTable = new HashTable<Integer, String>();
        for (int i = 0; i < 10; i++) {
            currentTable.put(i, "val " + i);
        }
        assertEquals("val 9val 9",
                currentTable.merge(9, "val 9", String::concat));
        assertEquals("count val 5",
                currentTable.merge(5, "val 5", (value, newValue) -> value = "count " + value));
    }

    @Test
    void computeTest() {
        HashTable<Integer, String> current = new HashTable<Integer, String>();
        for (int i = 0; i < 10; i++) {
            current.put(i, "val " + i);
        }
        assertEquals("BOOM!", current.computeIfAbsent(10, value -> "BOOM!"));
        assertEquals("val 23", current.computeIfAbsent(23, num -> "val " + num));
        assertNull(current.computeIfPresent(9, (num, val) -> null));
        assertNull(current.computeIfPresent(12, (num, val) -> val.concat(String.valueOf(num))));
        assertEquals("3 - val 3",  current.computeIfPresent(3, (num, val) -> num + " - " + val));
        assertEquals("val 4 - four", current.compute(4, (key, val)
                -> {
            assert val != null;
            return val.concat(" - four");
        }));
        assertEquals("val 71", current.compute(7, (k, v) -> (v == null) ? String.valueOf(1) : v+1));
    }
}