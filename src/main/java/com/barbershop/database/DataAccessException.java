package com.barbershop.database;

import java.sql.SQLException;

/**
 * Unchecked wrapper around {@link SQLException}.
 *
 * <p>Database failures are exceptional and are reported to the user through a dialog, so the
 * DAO layer wraps checked {@code SQLException}s in this runtime exception to keep controller
 * code readable.</p>
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, SQLException cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
