package com.example.expenseanalyzer.service;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

final class MonthUtil {
    private MonthUtil() { }

    /** Parses "YYYY-MM"; throws IllegalArgumentException (HTTP 400) for bad input. */
    static YearMonth parse(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Month must be in the format YYYY-MM, e.g. 2026-10");
        }
    }
}
