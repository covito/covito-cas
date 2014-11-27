package org.covito.cas.client;

import org.covito.cas.client.validation.Assertion;


/**
 * 票据验证
 */
public interface TicketValidator {

    Assertion validate(String ticket, String service);
    
}
