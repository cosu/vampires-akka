package ro.cosu.vampires.server.util;

import com.google.common.collect.Lists;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class TestAppender extends AppenderBase<ILoggingEvent> {
    public static Map<Level, List<ILoggingEvent>> events = new Hashtable<>();

    public static void clear() {
        events.clear();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        Level level = eventObject.getLevel();
        if (!events.containsKey(level)) {
            events.put(level, Lists.newArrayList());
        }
        events.get(level).add(eventObject);
    }

}
