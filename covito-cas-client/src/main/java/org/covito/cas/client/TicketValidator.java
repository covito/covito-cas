package org.covito.cas.client;


/**
 * 票据验证
 */
public interface TicketValidator {

    Assertion validate(String ticket, String service);
    
}
