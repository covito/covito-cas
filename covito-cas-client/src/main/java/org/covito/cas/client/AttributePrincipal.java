package org.covito.cas.client;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

/**
 * 扩展带属性的Principal
 */
public interface AttributePrincipal extends Principal, Serializable {

    /**
     * 获取票据
     * @param service
     * @return
     */
    String getProxyTicket(String service);

    /**
     * 获取属性Map
     * @return
     */
    Map<String, Object> getAttributes();
    
}
