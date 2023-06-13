package com.example.activitytracker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CustomMap<K, V> implements Serializable {

    private Map<K, V> map;

    public CustomMap() {
        map = new HashMap<>();
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void remove(K key) {
        map.remove(key);
    }

    public synchronized boolean containsKey(K key){
        return map.containsKey(key);
    }



}
