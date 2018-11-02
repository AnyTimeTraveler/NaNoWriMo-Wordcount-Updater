package org.simonscode.nanowrimotracker;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Calendar.*;

class ChartTimescaler {
    // Just some constants to make the code below more readable
    private static final long ONE_SECOND = 1000L;
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;

    private static SimpleDateFormat fullDateFormatter = new SimpleDateFormat("dd.MM.yy HH:mm");
    private static SimpleDateFormat dayFormatter = new SimpleDateFormat("d.M");
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM HH:mm");

    private static Calendar calendar = getInstance();

    static Map<Double, Object> generateDateOverrideMap(List<Date> dates) {
        Map<Double, Object> output = new HashMap<>();
        if (dates.size() < 3) {
            for (Date element : dates) {
                output.put((double) element.getTime(), fullDateFormatter.format(element));
            }
            return output;
        }
        Date first = dates.get(0);
        Date last = dates.get(dates.size() - 1);

        long diff = last.getTime() - first.getTime();

        calendar.setTime(new Date());
        calendar.set(HOUR, 0);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();

        calendar.setTime(first);
        calendar.set(HOUR, 0);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);
        long start = calendar.getTimeInMillis();

        long days = diff / ONE_DAY;

        if (diff > 30 * ONE_DAY) {
            for (long i = start; i < last.getTime(); i += (days / 10) * ONE_DAY) {
                output.put((double) i, fullDateFormatter.format(new Date(i)));
            }
            output.put((double) last.getTime(), timeFormatter.format(last));

            return output;
        }
        if (diff > 10 * ONE_DAY) {
            for (long i = start; i < last.getTime(); i += (days / 10) * ONE_DAY) {
                output.put((double) i, dayFormatter.format(new Date(i)));
            }
            output.put((double) last.getTime(), timeFormatter.format(last));

            return output;
        }
        if (diff > 5 * ONE_DAY) {
            for (long i = start; i < last.getTime(); i += (days / 5) * ONE_DAY) {
                output.put((double) i, dayFormatter.format(new Date(i)));
            }
            return output;
        }
        if (diff > 2 * ONE_DAY) {
            for (long i = start; i < last.getTime(); i += ONE_DAY / 2) {
                if (i >= startOfToday) {
                    output.put((double) i, timeFormatter.format(new Date(i)));
                } else {
                    output.put((double) i, dateTimeFormatter.format(new Date(i)));
                }
            }
            return output;
        }
        if (diff > 12 * ONE_HOUR) {
            for (long i = start; i < last.getTime(); i += 3 * ONE_HOUR) {
                output.put((double) i, timeFormatter.format(new Date(i)));
            }
            return output;
        }

        calendar.setTime(first);
        calendar.add(DAY_OF_YEAR, 1);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);
        start = calendar.getTimeInMillis();

        if (diff > 3 * ONE_HOUR) {
            for (long i = start; i < last.getTime(); i += ONE_HOUR) {
                output.put((double) i, timeFormatter.format(new Date(i)));
            }
            return output;
        }
        if (diff > ONE_HOUR) {
            output.put((double) first.getTime(), timeFormatter.format(first));
            for (long i = start; i < last.getTime(); i += 15 * ONE_MINUTE) {
                output.put((double) i, timeFormatter.format(new Date(i)));
            }
            output.put((double) last.getTime(), timeFormatter.format(last));
            return output;
        }

        output.put((double) first.getTime(), timeFormatter.format(first));
        for (long i = start; i < last.getTime(); i += ONE_HOUR) {
            output.put((double) i, timeFormatter.format(new Date(i)));
        }
        output.put((double) last.getTime(), timeFormatter.format(last));

        return output;
    }
}
