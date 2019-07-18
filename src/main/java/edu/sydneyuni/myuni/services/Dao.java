package edu.sydneyuni.myuni.services;

import java.io.IOException;

public interface Dao<T> {

    void insert(T item) throws IOException;
}
