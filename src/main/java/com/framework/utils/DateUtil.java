package com.framework.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    public static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
    public static final String YYYYMMDD_FORMAT = "yyyyMMddHHmmss";

    public static final DateFormat timeStampFormat = new SimpleDateFormat(YYYYMMDD_FORMAT);
    private static final DateFormat _outputFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static Date currentGMTTime() {
        // Date object always stores miliseconds offset based on GMT internally
        return new Date();
    }

    // yyyy-MM-ddTHH:mm:ssZxxxx
    public static Date parseTZDateString(String str) throws ParseException {
        DateFormat dfParse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");
        return dfParse.parse(str);
    }

    public static Date parseDateString(TimeZone tz, String dateString) {
        return parseDateString(tz, dateString, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date parseDateString(TimeZone tz, String dateString, String formatString) {
        DateFormat df = new SimpleDateFormat(formatString);
        df.setTimeZone(tz);

        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("why why ", e);
        }
    }

    public static String displayDateInTimezone(TimeZone tz, Date time) {
        return getDateDisplayString(tz, time, "yyyy-MM-dd HH:mm:ss z");
    }

    public static String getDateDisplayString(TimeZone tz, Date time) {
        return getDateDisplayString(tz, time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateDisplayString(TimeZone tz, Date time, String formatString) {
        DateFormat df = new SimpleDateFormat(formatString);
        df.setTimeZone(tz);

        return df.format(time);
    }

    public static String getOutputString(Date date) {
        if (date == null) {
            return "";
        }
        String formattedString = null;
        synchronized (_outputFormat) {
            formattedString = _outputFormat.format(date);
        }
        return formattedString;
    }

    public static Date now() {
        return new Date(System.currentTimeMillis());
    }

    public enum IntervalType {
        HOURLY, DAILY, WEEKLY, MONTHLY;

        boolean equals(String intervalType) {
            return super.toString().equalsIgnoreCase(intervalType);
        }

        public static IntervalType getIntervalType(String intervalTypeStr) {
            for (IntervalType intervalType : IntervalType.values()) {
                if (intervalType.equals(intervalTypeStr)) {
                    return intervalType;
                }
            }
            return null;
        }
    }

    public static IntervalType getIntervalType(short type) {
        if (type < 0 || type >= IntervalType.values().length) {
            return null;
        }
        return IntervalType.values()[type];
    }

    /**
     * Return next run time
     * 
     * @param intervalType hourly/daily/weekly/monthly
     * @param schedule MM[:HH][:DD] format. DD is day of week for weekly and day of month for
     *        monthly
     * @param timezone The timezone in which the schedule string is specified
     * @param startDate if specified, returns next run time after the specified startDate
     * @return
     */
    public static Date getNextRunTime(IntervalType type, String schedule, String timezone,
            Date startDate) {

        String[] scheduleParts = schedule.split(":"); // MM:HH:DAY

        final Calendar scheduleTime = Calendar.getInstance();
        scheduleTime.setTimeZone(TimeZone.getTimeZone(timezone));

        if (startDate == null) {
            startDate = new Date();
        }
        scheduleTime.setTime(startDate);
        // Throw an ArrayIndexOutOfBoundsException if schedule is badly
        // formatted.
        scheduleTime.setLenient(false);
        int minutes = 0;
        int hour = 0;
        int day = 0;
        Date execDate = null;

        switch (type) {
            case HOURLY:
                if (scheduleParts.length < 1) {
                    throw new RuntimeException("Incorrect schedule format: " + schedule
                            + " for interval type:" + type.toString());
                }
                minutes = Integer.parseInt(scheduleParts[0]);
                scheduleTime.set(Calendar.MINUTE, minutes);
                scheduleTime.set(Calendar.SECOND, 0);
                scheduleTime.set(Calendar.MILLISECOND, 0);
                execDate = scheduleTime.getTime();
                // XXX: !execDate.after(startDate) is strictly for testing.
                // During testing we use a test clock which runs much faster than
                // the real clock
                // So startDate and execDate will always be ahead in the future
                // and we will never increase the time here
                if (execDate.before(new Date()) || !execDate.after(startDate)) {
                    scheduleTime.add(Calendar.HOUR_OF_DAY, 1);
                }
                break;
            case DAILY:
                if (scheduleParts.length < 2) {
                    throw new RuntimeException("Incorrect schedule format: " + schedule
                            + " for interval type:" + type.toString());
                }
                minutes = Integer.parseInt(scheduleParts[0]);
                hour = Integer.parseInt(scheduleParts[1]);

                scheduleTime.set(Calendar.HOUR_OF_DAY, hour);
                scheduleTime.set(Calendar.MINUTE, minutes);
                scheduleTime.set(Calendar.SECOND, 0);
                scheduleTime.set(Calendar.MILLISECOND, 0);
                execDate = scheduleTime.getTime();
                // XXX: !execDate.after(startDate) is strictly for testing.
                // During testing we use a test clock which runs much faster than
                // the real clock
                // So startDate and execDate will always be ahead in the future
                // and we will never increase the time here
                if (execDate.before(new Date()) || !execDate.after(startDate)) {
                    scheduleTime.add(Calendar.DAY_OF_YEAR, 1);
                }
                break;
            case WEEKLY:
                if (scheduleParts.length < 3) {
                    throw new RuntimeException("Incorrect schedule format: " + schedule
                            + " for interval type:" + type.toString());
                }
                minutes = Integer.parseInt(scheduleParts[0]);
                hour = Integer.parseInt(scheduleParts[1]);
                day = Integer.parseInt(scheduleParts[2]);
                scheduleTime.set(Calendar.DAY_OF_WEEK, day);
                scheduleTime.set(Calendar.HOUR_OF_DAY, hour);
                scheduleTime.set(Calendar.MINUTE, minutes);
                scheduleTime.set(Calendar.SECOND, 0);
                scheduleTime.set(Calendar.MILLISECOND, 0);
                execDate = scheduleTime.getTime();
                // XXX: !execDate.after(startDate) is strictly for testing.
                // During testing we use a test clock which runs much faster than
                // the real clock
                // So startDate and execDate will always be ahead in the future
                // and we will never increase the time here
                if (execDate.before(new Date()) || !execDate.after(startDate)) {
                    scheduleTime.add(Calendar.DAY_OF_WEEK, 7);
                }
                ;
                break;
            case MONTHLY:
                if (scheduleParts.length < 3) {
                    throw new RuntimeException("Incorrect schedule format: " + schedule
                            + " for interval type:" + type.toString());
                }
                minutes = Integer.parseInt(scheduleParts[0]);
                hour = Integer.parseInt(scheduleParts[1]);
                day = Integer.parseInt(scheduleParts[2]);
                if (day > 28) {
                    throw new RuntimeException("Day cannot be greater than 28 for monthly schedule");
                }
                scheduleTime.set(Calendar.DAY_OF_MONTH, day);
                scheduleTime.set(Calendar.HOUR_OF_DAY, hour);
                scheduleTime.set(Calendar.MINUTE, minutes);
                scheduleTime.set(Calendar.SECOND, 0);
                scheduleTime.set(Calendar.MILLISECOND, 0);
                execDate = scheduleTime.getTime();
                // XXX: !execDate.after(startDate) is strictly for testing.
                // During testing we use a test clock which runs much faster than
                // the real clock
                // So startDate and execDate will always be ahead in the future
                // and we will never increase the time here
                if (execDate.before(new Date()) || !execDate.after(startDate)) {
                    scheduleTime.add(Calendar.MONTH, 1);
                }
                break;
            default:
                throw new RuntimeException("Incorrect interval: " + type.toString());
        }

        return scheduleTime.getTime();
    }

    // test only
    public static void main(String[] args) {
        System.out.println(getOutputString(convertUtcDate(new Date())));
    }

    public static Date getUtcDate() {
        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return new Date(cal.getTimeInMillis());
    }

    public static Date convertUtcDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return new Date(cal.getTimeInMillis());
    }

    public static String getTimeStamp(Date date) {
        return timeStampFormat.format(date);
    }
}