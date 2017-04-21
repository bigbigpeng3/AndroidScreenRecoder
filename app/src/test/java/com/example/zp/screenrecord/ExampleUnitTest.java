package com.example.dw.screenrecord;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);


        int i = 2 * 1024 * 1024 ;
        int j =  2 << 10 << 10;
        System.out.println(" i = " + i );
        System.out.println(" j =  " + j );

    }
}