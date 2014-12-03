covito-cas-client
====
基于cas-client优化

##主要优化点
1. 将Filter变成一个，避免原来的多个filter配置有顺序问题
2. 增加将配置项配置在资源文件中
3. 增加是否启用配置项
4. 优化地址过滤（可配置哪些地址不在过滤器拦截名单中）
5. 增加配置文件管理器配置项
6. 根据自己的理解重写了整个cas-client源码

###主要配置项
    enable                           是否启用
    configFile                       配置文件地址，默认：/cas-client.properties
    configManagerClass               配置管理器，默认：PropertiesConfigManager
    serverName                       client访问serverName，多个以空格分隔
    service                          登录跳到固定页面地址
    artifactParameterName            
    serviceParameterName
    encodeServiceUrl                 
    renew                            
    serverUrl                        CAS服务端地址（服务根地址）——必填
    gateway                          
    gatewayResolverClass             Gateway解析器
    authRedirectClass                
    urlPattern                       地址正则表达式，多个以;号分隔
    urlMatcherClass                  地址过滤器，默认：RegexUrlMatcher
    isContains                       匹配模式：包含|排除
    exceptionOnValidationFailure     认证失败抛出异常
    redirectAfterValidation          认证过后跳转
    useSession                       使用session存储票据
    ticketValidatorClass             票据验证器——必填

