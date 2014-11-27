package org.covito.cas.client;

import org.covito.cas.client.validation.Assertion;


/**
 * 票据验证
 */
public interface TicketValidator {

    /**
     * 较验票据
     * @param ticket
     * @param service
     * @return
     */
    Assertion validate(String ticket, String service);
    
}
