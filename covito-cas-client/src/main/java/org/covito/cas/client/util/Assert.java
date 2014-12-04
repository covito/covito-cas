package org.covito.cas.client.util;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

public class Assert {

	/**
	 * 断言不为空
	 * @param msg
	 * @param o
	 */
	public static void assertNotNull(String msg,Object o){
		MatcherAssert.assertThat(msg,o, CoreMatchers.notNullValue());
	}
	
	/**
	 * 断言表达式为真
	 * @param msg
	 * @param o
	 */
	public static void assertTrue(String msg,boolean condition){
		MatcherAssert.assertThat(msg,condition);
	}
}
