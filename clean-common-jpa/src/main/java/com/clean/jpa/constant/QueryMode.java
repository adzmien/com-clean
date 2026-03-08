package com.clean.jpa.constant;

/**
 * Determines how filter fields are matched against entity attributes.
 *
 * <ul>
 *   <li>{@link #SEARCH} — case-insensitive LIKE for Strings, exact equality for other types</li>
 *   <li>{@link #FILTER} — exact equality for all types</li>
 * </ul>
 */
public enum QueryMode {
    SEARCH,
    FILTER
}
