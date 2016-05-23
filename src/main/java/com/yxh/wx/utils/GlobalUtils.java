package com.yxh.wx.utils;

import java.util.Random;

import com.google.common.io.BaseEncoding;

public class GlobalUtils {

	public static String randomString(int i) {
		Random random = new Random(); // or SecureRandom
	    final byte[] buffer = new byte[i];
	    random.nextBytes(buffer);
	    return BaseEncoding.base64Url().omitPadding().encode(buffer); // or base32()
	}
}
