package ro.cosu.vampires.server.writers.mongo;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class LocalDateTimeConverterTest {

    @Test
    public void testDecode() throws Exception {
        LocalDateTimeConverter localDateTimeConverter = new LocalDateTimeConverter();
        Date parse = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
                .parse("Sun Feb 14 01:27:58 CET 2016");

        LocalDateTime decode = (LocalDateTime) localDateTimeConverter.decode(LocalDateTime.class, parse, null);
        LocalDateTime parsedDate = LocalDateTime.parse("2016-02-14T00:27:58");
        assertThat(decode, equalTo(parsedDate));
    }

    @Test
    public void testEncode() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.parse("2016-02-14T00:27:58.000");
        LocalDateTimeConverter localDateTimeConverter = new LocalDateTimeConverter();
        Date date = (Date) localDateTimeConverter.encode(localDateTime, null);

        Date parsedDate = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
                .parse("Sun Feb 14 01:27:58 CET 2016");
        assertThat(date, equalTo(parsedDate));
    }
}