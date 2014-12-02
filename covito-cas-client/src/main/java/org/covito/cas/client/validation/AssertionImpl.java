package org.covito.cas.client.validation;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.covito.cas.client.auth.AttributePrincipal;
import org.junit.Assert;

public class AssertionImpl implements Assertion {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6395408517241893148L;

	private final Date validFromDate;

	private final Date validUntilDate;

	private final Date authenticationDate;

	private final Map<String, Object> attributes;

	private final AttributePrincipal principal;
	
	public AssertionImpl(final AttributePrincipal principal) {
		this(principal, Collections.<String, Object> emptyMap());
	}
	
	public AssertionImpl(final AttributePrincipal principal, final Map<String, Object> attributes) {
		this(principal, new Date(), null, new Date(), attributes);
	}
	
	public AssertionImpl(AttributePrincipal principal, Date validFromDate, Date validUntilDate,
			Date authenticationDate, Map<String, Object> attributes) {
		this.principal = principal;
		this.validFromDate = validFromDate;
		this.validUntilDate = validUntilDate;
		this.attributes = attributes;
		this.authenticationDate = authenticationDate;
		Assert.assertNotNull("principal cannot be null.", this.principal);
		Assert.assertNotNull("validFromDate cannot be null.", this.validFromDate);
		Assert.assertNotNull("attributes cannot be null.", this.attributes);
	}

	@Override
	public Date getValidFromDate() {
		return validFromDate;
	}

	@Override
	public Date getValidUntilDate() {
		return validUntilDate;
	}

	@Override
	public Date getAuthenticationDate() {
		return authenticationDate;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public AttributePrincipal getPrincipal() {
		return principal;
	}

	@Override
	public boolean isValid() {
		if (this.validFromDate == null) {
			return true;
		}

		final Date now = new Date();
		return this.validFromDate.before(now) && (this.validUntilDate == null || this.validUntilDate.after(now));
	}

}
