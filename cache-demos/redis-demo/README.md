# 说明
集群下不需要选库，只有一个0号库

# 相关概念
```
RDB 与 AOF
```
# docker install
```
 docker pull redis:latest
 
 docker images
 
 docker run -itd --name redis-test -p 6379:6379 redis
 
  # docker run -itd --name redis -p 6379:6379 redis
 
 docker exec -it redis-test /bin/bash
```
# docker-compose 
```
docker-compose up -d
```
# redis
[正确姿势](https://blog.csdn.net/qq_35042060/article/details/99680719)
[redlock](https://redis.io/topics/distlock)
[docker-compose](https://www.cnblogs.com/xpengp/p/12713374.html)
[archtechture](xx)
[cluster-spec](https://redis.io/docs/reference/cluster-spec/)
