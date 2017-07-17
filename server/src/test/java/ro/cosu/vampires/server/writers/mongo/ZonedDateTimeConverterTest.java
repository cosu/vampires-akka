/*
 * The MIT License (MIT)
 * Copyright © 2016 Cosmin Dumitru, http://cosu.ro <cosu@cosu.ro>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package ro.cosu.vampires.server.writers.mongo;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ZonedDateTimeConverterTest {

    @Test
    public void testDecode() throws Exception {
        ZonedDateTimeConverter localDateTimeConverter = new ZonedDateTimeConverter();
        Date parse = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
                .parse("Sun Feb 14 01:27:58 CET 2016");

        ZonedDateTime decode = (ZonedDateTime) localDateTimeConverter.decode(ZonedDateTime.class, parse, null);
        ZonedDateTime parsedDate = ZonedDateTime.parse("2016-02-14T00:27:58Z");
        assertThat(decode, equalTo(parsedDate));
    }

    @Test
    public void testEncode() throws Exception {
        ZonedDateTime localDateTime = ZonedDateTime.parse("2016-02-14T00:27:58.000Z");
        ZonedDateTimeConverter localDateTimeConverter = new ZonedDateTimeConverter();
        Date date = (Date) localDateTimeConverter.encode(localDateTime, null);

        Date parsedDate = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH)
                .parse("Sun Feb 14 01:27:58 CET 2016");
        assertThat(date, equalTo(parsedDate));
    }
}