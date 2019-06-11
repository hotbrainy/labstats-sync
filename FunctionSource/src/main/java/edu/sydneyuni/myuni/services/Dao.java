package edu.sydneyuni.myuni.services;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface Dao<T> {

    void insert(T item) throws IOException;
}
