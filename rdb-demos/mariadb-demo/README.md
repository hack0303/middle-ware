# mariadb

## NOTE
[mariadb](h**ttps://mariadb.org/)

# DOCKER INSTALL

```
docker search mariadb

docker pull mariadb

docker run -d --restart always -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 --name mariadb mariadb:latest

```
username/password root/123456