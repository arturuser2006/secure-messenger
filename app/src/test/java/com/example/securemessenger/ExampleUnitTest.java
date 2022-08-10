package com.example.securemessenger;

import org.junit.Test;

import static org.junit.Assert.*;

import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        //DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        Date now = Calendar.getInstance().getTime();
        Calendar cal = new GregorianCalendar();
        cal.set(2000, 12, 30);
        Date time = cal.getTime();
        System.out.println(now.after(time));
    }
}