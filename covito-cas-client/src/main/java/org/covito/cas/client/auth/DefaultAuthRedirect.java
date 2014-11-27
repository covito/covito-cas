package org.covito.cas.client.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.covito.cas.client.AuthRedirect;

public class DefaultAuthRedirect implements AuthRedirect{

	@Override
	public void redirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) throws IOException {
		response.sendRedirect(redirectUrl);
	}

}
