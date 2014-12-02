package org.covito.cas.client.config;

/**
 * 定制哪些url可不被拦截
 */
public interface UrlMatcher {

	
    /**
     * 是否被匹配
     * @param url
     * @return
     */
    boolean matches(String url);
    
    
    /**
     * 设置拦截规则
     * @param pattern
     */
    void setPattern(String[] pattern);
}
