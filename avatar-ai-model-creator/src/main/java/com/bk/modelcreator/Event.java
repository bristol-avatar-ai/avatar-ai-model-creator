package com.bk.modelcreator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface Event {
    default String getTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    String getMessage();

}
