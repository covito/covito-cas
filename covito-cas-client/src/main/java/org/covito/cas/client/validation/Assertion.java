package org.covito.cas.client.validation;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.covito.cas.client.auth.AttributePrincipal;

/**
 * 票据校验结果
 */
public interface Assertion extends Serializable {

    /**
     * 认证通过的时间
     * @return
     */
    Date getValidFromDate();

   
    /**
     * 有效期最大时间
     * @return
     */
    Date getValidUntilDate();

   
    /**
     * 获取身份时间
     * @return
     */
    Date getAuthenticationDate();

    
    /**
     * 存放其它属性，如权限，角色，组织机构
     * @return
     */
    Map<String, Object> getAttributes();

    
    /**
     * 获取 Principal
     * @return
     */
    AttributePrincipal getPrincipal();

    
    /**
     * 是否已较验
     * @return
     */
    boolean isValid();
}
