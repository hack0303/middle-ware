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
# REF
[菜鸟教程](https://www.runoob.com/docker/docker-install-mongodb.html)
[springboot整合](https://spring.io/projects/spring-data-mongodb)



