# 简介

# docker 简单部署
```
```
1.docker run -d --name rabbitmq -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password -p 5671:5671 -p 5672:5672 -p 4369:4369 -p 25672:25672 -p 15671:15671 -p 15672:15672 rabbitmq:management
 
 用root/123456

2.docker update rabbitmq --restart=always

3.通过15672端口访问rabbitmq
默认账号：guest
默认密码:  guest

访问用 root/123456
```
# 相关
