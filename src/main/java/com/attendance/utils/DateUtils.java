package com.attendance.utils;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for date-related operations
 */
public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    /**
     * Parse a date string in ISO format (yyyy-MM-dd) to LocalDate
     * 
     * @param dateStr Date string in ISO format
     * @return LocalDate object or null if invalid
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Format a LocalDate to ISO format (yyyy-MM-dd)
     * 
     * @param date LocalDate to format
     * @return Date string in ISO format
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Format a LocalDate to display format (dd MMM yyyy)
     * 
     * @param date LocalDate to format
     * @return Date string in display format
     */
    public static String formatDisplayDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_DATE_FORMATTER);
    }
    
    /**
     * Convert java.sql.Date to LocalDate
     * 
     * @param sqlDate SQL Date to convert
     * @return LocalDate object
     */
    public static LocalDate toLocalDate(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }
    
    /**
     * Convert LocalDate to java.sql.Date
     * 
     * @param localDate LocalDate to convert
     * @return SQL Date object
     */
    public static Date toSqlDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.valueOf(localDate);
    }
    
    /**
     * Get the first day of the current month
     * 
     * @return LocalDate representing the first day of the current month
     */
    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }
    
    /**
     * Get the last day of the current month
     * 
     * @return LocalDate representing the last day of the current month
     */
    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }
    
    /**
     * Get the first day of a specific month
     * 
     * @param year Year
     * @param month Month (1-12)
     * @return LocalDate representing the first day of the specified month
     */
    public static LocalDate getFirstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }
    
    /**
     * Get the last day of a specific month
     * 
     * @param year Year
     * @param month Month (1-12)
     * @return LocalDate representing the last day of the specified month
     */
    public static LocalDate getLastDayOfMonth(int year, int month) {
        return YearMonth.of(year, month).atEndOfMonth();
    }
    
    /**
     * Get the first day of the current semester
     * This is a simplified implementation that assumes:
     * - Semester 1, 3, 5: July to December
     * - Semester 2, 4, 6: January to June
     * 
     * @param semester Semester number (1-6)
     * @return LocalDate representing the first day of the current semester
     */
    public static LocalDate getFirstDayOfSemester(int semester) {
        int currentYear = LocalDate.now().getYear();
        
        // Check if semester is odd or even
        boolean isOddSemester = semester % 2 == 1;
        
        if (isOddSemester) {
            // Odd semesters start in July
            return LocalDate.of(currentYear, 7, 1);
        } else {
            // Even semesters start in January
            return LocalDate.of(currentYear, 1, 1);
        }
    }
    
    /**
     * Get the last day of the current semester
     * This is a simplified implementation that assumes:
     * - Semester 1, 3, 5: July to December
     * - Semester 2, 4, 6: January to June
     * 
     * @param semester Semester number (1-6)
     * @return LocalDate representing the last day of the current semester
     */
    public static LocalDate getLastDayOfSemester(int semester) {
        int currentYear = LocalDate.now().getYear();
        
        // Check if semester is odd or even
        boolean isOddSemester = semester % 2 == 1;
        
        if (isOddSemester) {
            // Odd semesters end in December
            return LocalDate.of(currentYear, 12, 31);
        } else {
            // Even semesters end in June
            return LocalDate.of(currentYear, 6, 30);
        }
    }
    
    /**
     * Get a list of weekdays (Monday to Friday) between two dates
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of weekdays between the dates (inclusive)
     */
    public static List<LocalDate> getWeekdaysBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> weekdays = new ArrayList<>();
        
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return weekdays;
        }
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                weekdays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return weekdays;
    }
    
    /**
     * Check if a date is a weekday (Monday to Friday)
     * 
     * @param date Date to check
     * @return true if the date is a weekday, false otherwise
     */
    public static boolean isWeekday(LocalDate date) {
        if (date == null) {
            return false;
        }
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    
    /**
     * Get the start and end dates of the current week (Monday to Sunday)
     * 
     * @return Array with [startDate, endDate]
     */
    public static LocalDate[] getCurrentWeekDates() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        
        return new LocalDate[] { monday, sunday };
    }
    
    /**
     * Get the start and end dates of the previous week
     * 
     * @return Array with [startDate, endDate]
     */
    public static LocalDate[] getPreviousWeekDates() {
        LocalDate today = LocalDate.now();
        LocalDate previousMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        LocalDate previousSunday = previousMonday.plusDays(6);
        
        return new LocalDate[] { previousMonday, previousSunday };
    }
    
    /**
     * Get the academic year based on the current date
     * This method assumes the academic year starts in July and ends in June
     * 
     * @return Academic year in the format "YYYY-YYYY" (e.g., "2024-2025")
     */
    public static String getCurrentAcademicYear() {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int month = today.getMonthValue();
        
        // If current month is July or later, academic year is currentYear-nextYear
        // Otherwise, it's previousYear-currentYear
        if (month >= 7) {
            return currentYear + "-" + (currentYear + 1);
        } else {
            return (currentYear - 1) + "-" + currentYear;
        }
    }
    
    /**
     * Get the academic year for a specific date
     * This method assumes the academic year starts in July and ends in June
     * 
     * @param date The date to check
     * @return Academic year in the format "YYYY-YYYY" (e.g., "2024-2025")
     */
    public static String getAcademicYear(LocalDate date) {
        if (date == null) {
            return getCurrentAcademicYear();
        }
        
        int year = date.getYear();
        int month = date.getMonthValue();
        
        if (month >= 7) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
    
    /**
     * Get the current semester based on the date
     * This method assumes:
     * - First year: Semesters 1 (Jul-Dec) and 2 (Jan-Jun)
     * - Second year: Semesters 3 (Jul-Dec) and 4 (Jan-Jun)
     * - Third year: Semesters 5 (Jul-Dec) and 6 (Jan-Jun)
     * 
     * @param className Class name (e.g., "FY-CS", "SY-CS", "TY-CS")
     * @return Semester number (1-6)
     */
    public static int getCurrentSemester(String className) {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        boolean isSecondHalf = month >= 7; // July-December is the second half of the year
        
        if (className.startsWith("FY")) {
            // First year
            return isSecondHalf ? 1 : 2;
        } else if (className.startsWith("SY")) {
            // Second year
            return isSecondHalf ? 3 : 4;
        } else if (className.startsWith("TY")) {
            // Third year
            return isSecondHalf ? 5 : 6;
        }
        
        // Default to 1 if class name is not recognized
        return 1;
    }
}