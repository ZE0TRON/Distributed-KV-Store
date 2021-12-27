package de.tum.i13.shared;

public class Pair<K, V> {

    public K fst;
    public V snd;

    public Pair<K, V> Pair(K fst, V snd) {
        return new Pair<>(fst, snd);
    }

    public Pair(K fst, V snd) {
        this.fst = fst;
        this.snd = snd;
    }



}