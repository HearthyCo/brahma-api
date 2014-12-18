package gl.glue.brahma.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TreeMap<T> {
    private Map<T, TreeMap<T>> children;

    public TreeMap() {
        children = new HashMap<>();
    }

    public void add(T... path) {
        if (path.length == 0) return;
        T key = path[0];
        TreeMap<T> tm = children.get(key);
        if (tm == null) {
            tm = new TreeMap<>();
            children.put(key, tm);
        }
        tm.add((T[])Arrays.asList(path).subList(1, path.length).toArray());
    }

    public TreeMap<T> get(T key) {
        return children.get(key);
    }

}
