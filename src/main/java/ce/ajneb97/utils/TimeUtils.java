package ce.ajneb97.utils;

import ce.ajneb97.manager.MessagesManager;

public class TimeUtils {

    public static String getTime(long seconds, MessagesManager msgManager) {
        if (seconds == 0) {
            return seconds + msgManager.getTimeSeconds();
        }
        long totalMin = seconds / 60;
        long totalHour = totalMin / 60;
        long totalDay = totalHour / 24;
        String time = "";
        if (seconds > 59) {
            seconds = seconds - 60 * totalMin;
        }
        if (seconds > 0) {
            time = seconds + msgManager.getTimeSeconds();
        }
        if (totalMin > 59) {
            totalMin = totalMin - 60 * totalHour;
        }
        if (totalMin > 0) {
            time = totalMin + msgManager.getTimeMinutes() + " " + time;
        }
        if (totalHour > 24) {
            totalHour = totalHour - 24 * totalDay;
        }
        if (totalHour > 0) {
            time = totalHour + msgManager.getTimeHours() + " " + time;
        }
        if (totalDay > 0) {
            time = totalDay + msgManager.getTimeDays() + " " + time;
        }

        return time;
    }
}
