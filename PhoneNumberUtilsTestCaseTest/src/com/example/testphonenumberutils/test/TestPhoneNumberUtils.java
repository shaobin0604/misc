package com.example.testphonenumberutils.test;

import android.telephony.PhoneNumberUtils;
import android.test.AndroidTestCase;

public class TestPhoneNumberUtils extends AndroidTestCase {
    public void testNumber1() throws Exception {
        assertFalse(PhoneNumberUtils.isGlobalPhoneNumber("af"));
    }
    
    public void testNumber2() throws Exception {
        assertTrue(PhoneNumberUtils.isGlobalPhoneNumber(PhoneNumberUtils.stripSeparators("(+86)123456aaa00789")));
    }
}
