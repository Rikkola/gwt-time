/*
 * Copyright (c) 2008, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar;

import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.time.Instant;
import javax.time.calendar.TimeZone.Discontinuity;
import javax.time.calendar.TimeZone.OffsetInfo;

import org.testng.annotations.Test;

/**
 * Test TimeZone.
 *
 * @author Michael Nascimento Santos
 * @author Stephen Colebourne
 */
@Test
public class TestTimeZone {

    //-----------------------------------------------------------------------
    // Basics
    //-----------------------------------------------------------------------
    public void test_interfaces() {
        assertTrue(TimeZone.UTC instanceof Serializable);
    }

    public void test_immutable() {
        Class<ZoneOffset> cls = ZoneOffset.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()));
                assertTrue(Modifier.isFinal(field.getModifiers()));
            }
        }
    }

    public void test_serialization_fixed() throws Exception {
        TimeZone test = TimeZone.timeZone("UTC+01:30");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        TimeZone result = (TimeZone) in.readObject();
        
        assertSame(result, test);
    }

    public void test_serialization_Europe() throws Exception {
        TimeZone test = TimeZone.timeZone("Europe/London");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        TimeZone result = (TimeZone) in.readObject();
        
        assertSame(result, test);
    }

    public void test_serialization_America() throws Exception {
        TimeZone test = TimeZone.timeZone("America/Chicago");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(test);
        baos.close();
        byte[] bytes = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        TimeZone result = (TimeZone) in.readObject();
        
        assertSame(result, test);
    }

    //-----------------------------------------------------------------------
    // UTC
    //-----------------------------------------------------------------------
    public void test_constant_UTC() {
        TimeZone test = TimeZone.UTC;
        assertEquals(test.getID(), "UTC");
        assertEquals(test.getName(), "UTC");
        assertEquals(test.getShortName(), "UTC");
        assertEquals(test.isFixed(), true);
        assertEquals(test.getOffset(Instant.instant(0L)), ZoneOffset.UTC);
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 30));
        assertEquals(info.isDiscontinuity(), false);
        assertEquals(info.getDiscontinuity(), null);
        assertEquals(info.getOffset(), ZoneOffset.UTC);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.UTC);
        assertSame(test, TimeZone.timeZone("UTC"));
        assertSame(test, TimeZone.timeZone(ZoneOffset.UTC));
    }

    //-----------------------------------------------------------------------
    public void test_factory_string_UTC() {
        String[] values = new String[] {
            "Z",
            "+00","+0000","+00:00","+000000","+00:00:00",
            "-00","-0000","-00:00","-000000","-00:00:00",
        };
        for (int i = 0; i < values.length; i++) {
            TimeZone test = TimeZone.timeZone("UTC" + values[i]);
            assertSame(test, TimeZone.UTC);
        }
    }

    public void test_factory_string_invalid() {
        String[] values = new String[] {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","ZZ",
            "+0","+0:00","+00:0","+0:0",
            "+000","+00000",
            "+0:00:00","+00:0:00","+00:00:0","+0:0:0","+0:0:00","+00:0:0","+0:00:0",
            "+01_00","+01;00","+01@00","+01:AA",
            "+19","+19:00","+18:01","+18:00:01","+1801","+180001",
            "-0","-0:00","-00:0","-0:0",
            "-000","-00000",
            "-0:00:00","-00:0:00","-00:00:0","-0:0:0","-0:0:00","-00:0:0","-0:00:0",
            "-19","-19:00","-18:01","-18:00:01","-1801","-180001",
            "-01_00","-01;00","-01@00","-01:AA",
            "@01:00",
        };
        for (int i = 0; i < values.length; i++) {
            try {
                TimeZone.timeZone("UTC" + values[i]);
                fail("Should have failed:" + values[i]);
            } catch (IllegalArgumentException ex) {
                // expected
            }
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_factory_string_null() {
        TimeZone.timeZone((String) null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_factory_string_unknown() {
        TimeZone.timeZone("Unknown");
    }

    //-----------------------------------------------------------------------
    public void test_factory_string_London() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.getName(), "Europe/London");
        assertEquals(test.getShortName(), "Europe/London");
        assertSame(TimeZone.timeZone("Europe/London"), test);
    }

    //-----------------------------------------------------------------------
    // Europe/London
    //-----------------------------------------------------------------------
    public void test_London() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getID(), "Europe/London");
        assertEquals(test.getName(), "Europe/London");
        assertEquals(test.getShortName(), "Europe/London");
        assertEquals(test.isFixed(), false);
    }

    public void test_London_getOffset() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 1, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 2, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 4, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 5, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 6, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 7, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 8, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 9, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 12, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
    }

    public void test_London_getOffset_toDST() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        // cutover at 01:00Z
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 30, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 30, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
    }

    public void test_London_getOffset_fromDST() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
        // cutover at 01:00Z
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 10, 26, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 10, 26, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(0));
    }

    public void test_London_getOffsetInfo() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 1, 1)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 2, 1)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 1)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 4, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 5, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 7, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 8, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 9, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 1)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 12, 1)), ZoneOffset.zoneOffset(0));
    }

    public void test_London_getOffsetInfo_toDST() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 24)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 25)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 26)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 27)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 28)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 29)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 30)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 31)), ZoneOffset.zoneOffset(1));
        // cutover at 01:00Z
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 0, 59, 59, 999999999)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 2, 0, 0, 0)), ZoneOffset.zoneOffset(1));
    }

    public void test_London_getOffsetInfo_fromDST() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 24)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 25)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 26)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 27)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 28)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 29)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 30)), ZoneOffset.zoneOffset(0));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 31)), ZoneOffset.zoneOffset(0));
        // cutover at 01:00Z
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 0, 59, 59, 999999999)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 2, 0, 0, 0)), ZoneOffset.zoneOffset(0));
    }

    public void test_London_getOffsetInfo_gap() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 1, 0, 0, 0));
        assertEquals(info.isDiscontinuity(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.zoneOffset(1));
        Discontinuity dis = info.getDiscontinuity();
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(0));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(1));
        assertEquals(dis.getTransitionInstant(), OffsetDateTime.dateTime(2008, 3, 30, 1, 0, ZoneOffset.UTC).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(0)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(1)), false);
        assertEquals(dis.toString(), "Discontinuity[Gap from Z to +01:00]");
    }

    public void test_London_getOffsetInfo_overlap() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 1, 0, 0, 0));
        assertEquals(info.isDiscontinuity(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.zoneOffset(0));
        Discontinuity dis = info.getDiscontinuity();
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(1));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(0));
        assertEquals(dis.getTransitionInstant(), OffsetDateTime.dateTime(2008, 10, 26, 1, 0, ZoneOffset.UTC).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(-1)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(0)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(1)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.toString(), "Discontinuity[Overlap from +01:00 to Z]");
    }

    //-----------------------------------------------------------------------
    // Europe/Paris
    //-----------------------------------------------------------------------
    public void test_Paris() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        assertEquals(test.getID(), "Europe/Paris");
        assertEquals(test.getName(), "Europe/Paris");
        assertEquals(test.getShortName(), "Europe/Paris");
        assertEquals(test.isFixed(), false);
    }

    public void test_Paris_getOffset() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 1, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 2, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 4, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 5, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 6, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 7, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 8, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 9, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 12, 1, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
    }

    public void test_Paris_getOffset_toDST() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        // cutover at 01:00Z
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 30, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 30, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
    }

    public void test_Paris_getOffset_fromDST() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 24, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 25, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 26, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 27, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 28, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 29, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 30, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 31, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
        // cutover at 01:00Z
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 10, 26, 0, 59, 59, 999999999, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(2));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 10, 26, 1, 0, 0, 0, ZoneOffset.UTC).toInstant()), ZoneOffset.zoneOffset(1));
    }

    public void test_Paris_getOffsetInfo() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 1, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 2, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 4, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 5, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 7, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 8, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 9, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 1)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 1)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 12, 1)), ZoneOffset.zoneOffset(1));
    }

    public void test_Paris_getOffsetInfo_toDST() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 24)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 25)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 26)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 27)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 28)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 29)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 30)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 31)), ZoneOffset.zoneOffset(2));
        // cutover at 01:00Z which is 02:00+01:00(local Paris time)
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 1, 59, 59, 999999999)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 3, 0, 0, 0)), ZoneOffset.zoneOffset(2));
    }

    public void test_Paris_getOffsetInfo_fromDST() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 24)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 25)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 26)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 27)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 28)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 29)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 30)), ZoneOffset.zoneOffset(1));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 31)), ZoneOffset.zoneOffset(1));
        // cutover at 01:00Z which is 02:00+01:00(local Paris time)
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 1, 59, 59, 999999999)), ZoneOffset.zoneOffset(2));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 3, 0, 0, 0)), ZoneOffset.zoneOffset(1));
    }

    public void test_Paris_getOffsetInfo_gap() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 30, 2, 0, 0, 0));
        assertEquals(info.isDiscontinuity(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.zoneOffset(2));
        Discontinuity dis = info.getDiscontinuity();
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(1));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(2));
        assertEquals(dis.getTransitionInstant(), OffsetDateTime.dateTime(2008, 3, 30, 1, 0, ZoneOffset.UTC).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(3)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(1)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.toString(), "Discontinuity[Gap from +01:00 to +02:00]");
    }

    public void test_Paris_getOffsetInfo_overlap() {
        TimeZone test = TimeZone.timeZone("Europe/Paris");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 10, 26, 2, 0, 0, 0));
        assertEquals(info.isDiscontinuity(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.zoneOffset(1));
        Discontinuity dis = info.getDiscontinuity();
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(2));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(1));
        assertEquals(dis.getTransitionInstant(), OffsetDateTime.dateTime(2008, 10, 26, 1, 0, ZoneOffset.UTC).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(0)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(1)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(3)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(0)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(1)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(2)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(3)), false);
        assertEquals(dis.toString(), "Discontinuity[Overlap from +02:00 to +01:00]");
    }

    //-----------------------------------------------------------------------
    // America/New_York
    //-----------------------------------------------------------------------
    public void test_NewYork() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        assertEquals(test.getID(), "America/New_York");
        assertEquals(test.getName(), "America/New_York");
        assertEquals(test.getShortName(), "America/New_York");
        assertEquals(test.isFixed(), false);
    }

    public void test_NewYork_getOffset() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        ZoneOffset offset = ZoneOffset.zoneOffset(-5);
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 1, 1, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 2, 1, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 1, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 4, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 5, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 6, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 7, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 8, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 9, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 12, 1, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 1, 28, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 2, 28, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 4, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 5, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 6, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 7, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 8, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 9, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 10, 28, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 28, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 12, 28, offset).toInstant()), ZoneOffset.zoneOffset(-5));
    }

    public void test_NewYork_getOffset_toDST() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        ZoneOffset offset = ZoneOffset.zoneOffset(-5);
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 8, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 9, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 10, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 11, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 12, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 13, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 3, 14, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        // cutover at 02:00 local
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 9, 1, 59, 59, 999999999, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 3, 9, 2, 0, 0, 0, offset).toInstant()), ZoneOffset.zoneOffset(-4));
    }

    public void test_NewYork_getOffset_fromDST() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        ZoneOffset offset = ZoneOffset.zoneOffset(-4);
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 1, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 2, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 3, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 4, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 5, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 6, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        assertEquals(test.getOffset(OffsetDateTime.dateMidnight(2008, 11, 7, offset).toInstant()), ZoneOffset.zoneOffset(-5));
        // cutover at 02:00 local
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 11, 2, 1, 59, 59, 999999999, offset).toInstant()), ZoneOffset.zoneOffset(-4));
        assertEquals(test.getOffset(OffsetDateTime.dateTime(2008, 11, 2, 2, 0, 0, 0, offset).toInstant()), ZoneOffset.zoneOffset(-5));
    }

    public void test_NewYork_getOffsetInfo() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 1, 1)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 2, 1)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 1)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 4, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 5, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 7, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 8, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 9, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 12, 1)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 1, 28)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 2, 28)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 4, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 5, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 6, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 7, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 8, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 9, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 10, 28)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 28)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 12, 28)), ZoneOffset.zoneOffset(-5));
    }

    public void test_NewYork_getOffsetInfo_toDST() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 8)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 9)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 10)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 11)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 12)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 13)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 3, 14)), ZoneOffset.zoneOffset(-4));
        // cutover at 02:00 local
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 9, 1, 59, 59, 999999999)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 9, 3, 0, 0, 0)), ZoneOffset.zoneOffset(-4));
    }

    public void test_NewYork_getOffsetInfo_fromDST() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 1)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 2)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 3)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 4)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 5)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 6)), ZoneOffset.zoneOffset(-5));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateMidnight(2008, 11, 7)), ZoneOffset.zoneOffset(-5));
        // cutover at 02:00 local
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 11, 2, 0, 59, 59, 999999999)), ZoneOffset.zoneOffset(-4));
        checkOffset(test.getOffsetInfo(LocalDateTime.dateTime(2008, 11, 2, 2, 0, 0, 0)), ZoneOffset.zoneOffset(-5));
    }

    public void test_NewYork_getOffsetInfo_gap() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 3, 9, 2, 0, 0, 0));
        assertEquals(info.isDiscontinuity(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.zoneOffset(-4));
        Discontinuity dis = info.getDiscontinuity();
        assertEquals(dis.isGap(), true);
        assertEquals(dis.isOverlap(), false);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(-5));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(-4));
        assertEquals(dis.getTransitionInstant(), OffsetDateTime.dateTime(2008, 3, 9, 2, 0, ZoneOffset.zoneOffset(-5)).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-5)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-4)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(-5)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(-4)), false);
        assertEquals(dis.toString(), "Discontinuity[Gap from -05:00 to -04:00]");
    }

    public void test_NewYork_getOffsetInfo_overlap() {
        TimeZone test = TimeZone.timeZone("America/New_York");
        OffsetInfo info = test.getOffsetInfo(LocalDateTime.dateTime(2008, 11, 2, 1, 0, 0, 0));
        assertEquals(info.isDiscontinuity(), true);
        assertEquals(info.getOffset(), null);
        assertEquals(info.getEstimatedOffset(), ZoneOffset.zoneOffset(-5));
        Discontinuity dis = info.getDiscontinuity();
        assertEquals(dis.isGap(), false);
        assertEquals(dis.isOverlap(), true);
        assertEquals(dis.getOffsetBefore(), ZoneOffset.zoneOffset(-4));
        assertEquals(dis.getOffsetAfter(), ZoneOffset.zoneOffset(-5));
        assertEquals(dis.getTransitionInstant(), OffsetDateTime.dateTime(2008, 11, 2, 2, 0, ZoneOffset.zoneOffset(-4)).toInstant());
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-1)), false);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-5)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(-4)), true);
//        assertEquals(dis.containsOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(-1)), false);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(-5)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(-4)), true);
        assertEquals(dis.isValidOffset(ZoneOffset.zoneOffset(2)), false);
        assertEquals(dis.toString(), "Discontinuity[Overlap from -04:00 to -05:00]");
    }

//    //-----------------------------------------------------------------------
//    // toTimeZone()
//    //-----------------------------------------------------------------------
//    public void test_toTimeZone() {
//        TimeZone offset = TimeZone.timeZone(1, 2, 3);
//        assertEquals(offset.toTimeZone(), TimeZone.timeZone(offset));
//    }

//    //-----------------------------------------------------------------------
//    // compareTo()
//    //-----------------------------------------------------------------------
//    public void test_compareTo() {
//        TimeZone offset1 = TimeZone.timeZone(1, 2, 3);
//        TimeZone offset2 = TimeZone.timeZone(2, 3, 4);
//        assertTrue(offset1.compareTo(offset2) > 0);
//        assertTrue(offset2.compareTo(offset1) < 0);
//        assertTrue(offset1.compareTo(offset1) == 0);
//        assertTrue(offset2.compareTo(offset2) == 0);
//    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals() {
        TimeZone test1 = TimeZone.timeZone("Europe/London");
        TimeZone test2 = TimeZone.timeZone("Europe/Paris");
        TimeZone test2b = TimeZone.timeZone("Europe/Paris");
        assertEquals(test1.equals(test2), false);
        assertEquals(test2.equals(test1), false);
        
        assertEquals(test1.equals(test1), true);
        assertEquals(test2.equals(test2), true);
        assertEquals(test2.equals(test2b), true);
        
        assertEquals(test1.hashCode() == test1.hashCode(), true);
        assertEquals(test2.hashCode() == test2.hashCode(), true);
        assertEquals(test2.hashCode() == test2b.hashCode(), true);
    }

    public void test_equals_null() {
        assertEquals(TimeZone.timeZone("Europe/London").equals(null), false);
    }

    public void test_equals_notTimeZone() {
        assertEquals(TimeZone.timeZone("Europe/London").equals("Europe/London"), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    public void test_toString() {
        TimeZone test = TimeZone.timeZone("Europe/London");
        assertEquals(test.toString(), "Europe/London");
        test = TimeZone.timeZone("UTC+01:02:03");
        assertEquals(test.toString(), "UTC+01:02:03");
        test = TimeZone.UTC;
        assertEquals(test.toString(), "UTC");
    }

    //-----------------------------------------------------------------------
    private void checkOffset(OffsetInfo info, ZoneOffset zoneOffset) {
        assertEquals(info.isDiscontinuity(), false);
        assertEquals(info.getDiscontinuity(), null);
        assertEquals(info.getOffset(), zoneOffset);
        assertEquals(info.getEstimatedOffset(), zoneOffset);
//        assertEquals(info.containsOffset(zoneOffset), true);
        assertEquals(info.isValidOffset(zoneOffset), true);
    }

}
