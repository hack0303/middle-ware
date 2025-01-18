# note


# docker install

```
docker sreach mongo
```
```
docker pull mongo:latest
```
```
docker run -itd --name mongo -p 27017:27017 mongo --auth
```
```
docker exec -it mongo mongo admin
```
```
 mongo cmd 
 >  db.createUser({ user:'admin',pwd:'123456',roles:[ { role:'userAdminAnyDatabase', db: 'admin'},"readWriteAnyDatabase"]});
 # 尝试使用上面创建的用户信息进行连接。
 > db.auth('admin', '123456')
```
# 内存限制参数
计算公式 (系统内存-1G)x 0.5
--wiredTigerCacheSizeGB 8
# REF
[菜鸟教程](https://www.runoob.com/docker/docker-install-mongodb.html)
[springboot整合](https://spring.io/projects/spring-data-mongodb)
[mongodb占用内存太大解决办法](https://blog.csdn.net/qq_38316655/article/details/113205223)
[mongo内存设置](https://www.cnblogs.com/dream-it-possible/p/13595851.html)


