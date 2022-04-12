#FAQ
Q:SpringBoot升级2.4.0所出现的问题：When allowCredentials is true, allowedOrigins cannot contain the specia
When allowCredentials is true, allowedOrigins cannot contain the special value "*“since that cannot be set on the “Access-Control-Allow-Origin” response header. To allow credentials to a set of origins, list them explicitly or consider using"allowedOriginPatterns” instead
A:跨域配置报错，将.allowedOrigins替换成.allowedOriginPatterns即可。

# REF
[SPRING版本](https://spring.io/projects/spring-cloud#overview)
[spring initialization](https://start.spring.io/)
[SpringBoot升级2.4.0所出现的问题：When allowCredentials is true, allowedOrigins cannot contain the specia](https://blog.csdn.net/jxysgzs/article/details/110818712)