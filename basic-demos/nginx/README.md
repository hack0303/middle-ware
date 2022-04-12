# nginx 相关

## Nginx 的 docker 部署
### 1、版本 nginx 的镜像；
```
docker pull nginx:1.15
```


### 2、查看拉取到的镜像信息获取 sha256
```
docker images
```

### 3、在主机上创建用于映射的目录

```
mkdir -p /opt/nginx/config/conf.d
mkdir -p /opt/nginx/logs
```

### 4、在 /opt/nginx/config 目录下生成 nginx.conf 文件，文件内容如下：
```
user  nginx;
worker_processes  1;

error_log  /var/log/nginx/error.log warn;
pid        /var/run/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    keepalive_timeout  65;
  #gzip  on;

    include /etc/nginx/conf.d/*.conf;
}

``` 

 

 

 ### 5、在 /opt/nginx/config/conf.d 生成 default.conf 文件， 内容如下所示：

```
server {
    listen       80;
    server_name  localhost;

    #charset koi8-r;
    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }

    #error_page  404              /404.html;

    # redirect server error pages to the static page /50x.html
    #
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }

    # proxy the PHP scripts to Apache listening on 127.0.0.1:80
    #
    #location ~ \.php$ {
    #    proxy_pass   http://127.0.0.1;
    #}

    # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
    #
    #location ~ \.php$ {
    #    root           html;
    #    fastcgi_pass   127.0.0.1:9000;
    #    fastcgi_index  index.php;
    #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
    #    include        fastcgi_params;
    #}

    # deny access to .htaccess files, if Apache's document root
    # concurs with nginx's one
    #
    #location ~ /\.ht {
    #    deny  all;
    #}
}
```

### 6、运行如下的命令
```
docker run \
                       --name nginx \
                       -p 80:80 \-v  /opt/nginx/config/nginx.conf:/etc/nginx/nginx.conf \
                       -v /opt/nginx/config/conf.d:/etc/nginx/conf.d  \
                       -v /opt/nginx/logs:/var/log/nginx \
                       -d 53f3fd8007f7
```
 ```
docker run \
                       --name nginx \
                       --net host \-v  /opt/nginx/config/nginx.conf:/etc/nginx/nginx.conf \
                       -v /opt/nginx/config/conf.d:/etc/nginx/conf.d  \
                       -v /opt/nginx/logs:/var/log/nginx \
                       -v /home/dev-env/html:/opt/html \
                       -d 53f3fd8007f7
```

7、在浏览器找那个打开 http://192.168.172.20:8100，进入到nginx欢迎页面；
# 相关
[nginx](http://nginx.org/en/download.html)
[docker-maven](https://springframework.guru/fabric8-docker-maven-plugin/)
[nginx反向代理后，获取hostname不正确的问题](https://www.iteye.com/blog/alex-yang-xiansoftware-com-2404387)

# F&Q
host不是请求的
```
          proxy_set_header HOST $host;   
            proxy_set_header X-Real-IP $remote_addr;   
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;  
```