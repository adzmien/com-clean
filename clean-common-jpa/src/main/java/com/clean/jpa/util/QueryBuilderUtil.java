package com.clean.jpa.util;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.clean.jpa.exception.ColumnValidationException;

public final class QueryBuilderUtil {

    private QueryBuilderUtil() {}

    /**
     * Validates that {@code cols} is non-empty and every entry is in {@code allowedColumns}.
     * Throws {@link ColumnValidationException} with 400 if empty, 403 if disallowed columns found.
     */
    public static void validateColumns(List<String> cols, Set<String> allowedColumns) {
        if (cols == null || cols.isEmpty())
            throw new ColumnValidationException("columns must not be empty", HttpStatus.BAD_REQUEST);
        List<String> disallowed = cols.stream().filter(c -> !allowedColumns.contains(c)).toList();
        if (!disallowed.isEmpty())
            throw new ColumnValidationException(
                    "Requested columns are not permitted: " + disallowed, HttpStatus.FORBIDDEN);
    }
}
