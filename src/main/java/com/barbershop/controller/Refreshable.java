package com.barbershop.controller;

/**
 * Implemented by screen controllers that need to reload their data when the screen becomes
 * visible, so the tables always show current database contents.
 */
public interface Refreshable {

    /** Reloads the screen's data from the database and the in-memory data structures. */
    void refresh();
}
