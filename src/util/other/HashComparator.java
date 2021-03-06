package util.other;

import java.util.Comparator;

public class HashComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
}
