package com.example.google_map;

public interface CustomPoolElement {
    int getPoolSize();
    boolean returnSame();
    void clean();
    void pool();
}
