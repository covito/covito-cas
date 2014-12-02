package org.covito.cas.client.proxy;

/**
 * 授权票据存储器
 */
public interface ProxyGrantingTicketStorage {

    /**
     * 保存
     * @param proxyGrantingTicketIou
     * @param proxyGrantingTicket
     */
    public void save(String proxyGrantingTicketIou, String proxyGrantingTicket);

    /**
     * 获取
     * @param proxyGrantingTicketIou
     * @return
     */
    public String retrieve(String proxyGrantingTicketIou);

    /**
     * 清空
     */
    public void cleanUp();
}
