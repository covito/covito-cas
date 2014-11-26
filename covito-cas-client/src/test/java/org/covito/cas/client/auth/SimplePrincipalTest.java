package org.covito.cas.client.auth;

import org.junit.Test;

public class SimplePrincipalTest {

	@Test(expected=AssertionError.class)
	public void create(){
		new SimplePrincipal(null);
	}
}
