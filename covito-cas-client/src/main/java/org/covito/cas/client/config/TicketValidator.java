package org.covito.cas.client.config;

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
    
    /**
     * 验证器初始化
     * @param config
     * @return
     */
    void initValidator(ConfigManager config);
}
