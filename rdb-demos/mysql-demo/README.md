# 相关概念
引擎插件 innodb myisam

# 部署
- 测试 docker
```
1.查找版本
docker search mysql

2.拉取镜像
docker pull mysql:latest

3.查看本地镜像
docker images

4.运行容器
docker run -itd --name mysql-test -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql

5.安装成功
docker ps 命令查看是否安装成功


```

