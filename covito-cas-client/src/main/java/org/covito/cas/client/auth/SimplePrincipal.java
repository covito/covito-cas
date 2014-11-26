package org.covito.cas.client.auth;

import java.io.Serializable;
import java.security.Principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Assert;

public class SimplePrincipal implements Principal, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7228537251043331045L;

	private final String name;

	public SimplePrincipal(final String name) {
		this.name = name;
		Assert.assertNotNull("name cannot be null.", name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (!(o instanceof SimplePrincipal)) {
			return false;
		} else {
			return new EqualsBuilder().append(name, ((SimplePrincipal) o).getName()).build();
		}
	}

	public int hashCode() {
		return new HashCodeBuilder().append(name).hashCode();
	}
}
