package service;

import java.util.List;

public abstract class Service<T> {
    private static final String DB_PATH = "";


    public List<T> getAll() { return null; } // <T>
    public boolean add(String... vargs) { return false; }
    public boolean update(String... vargs) { return false; }
    public boolean delete(String id) { return false; }
}
