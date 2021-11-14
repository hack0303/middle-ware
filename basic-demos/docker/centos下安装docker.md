# 安装教程

## Uninstall old versions

Older versions of Docker were called `docker` or `docker-engine`. If these are installed, uninstall them, along with associated dependencies.

```
$ sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```

It’s OK if `yum` reports that none of these packages are installed.

#### Set up the repository

Install the `yum-utils` package (which provides the `yum-config-manager` utility) and set up the **stable** repository.

```
sudo yum install -y yum-utils
```

```
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
```

These repositories are included in the docker.repo file above but are disabled by default. You can enable them alongside the stable repository. The following command enables the nightly repository.

```
sudo yum-config-manager --enable docker-ce-nightly
```

To enable the test channel, run the following command:

```
sudo yum-config-manager --enable docker-ce-test
```

Install the *latest version* of Docker Engine and containerd, or go to the next step to install a specific version:

```
sudo yum install docker-ce docker-ce-cli containerd.io
```

Start Docker.

```
sudo systemctl start docker
```

Verify that Docker Engine is installed correctly by running the `hello-world` image.

```
sudo docker run hello-world
```



ES

#### **2.1、下载 Docker Compose**

```javascript
$ curl -L https://get.daocloud.io/docker/compose/releases/download/1.24.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
```

#### **2.2、修改该文件的权限为可执行**

```javascript
$ chmod +x /usr/local/bin/docker-compose
```

#### **2.3、查看是否已经安装成功**

```javascript
$ docker-compose --version
```


## R&Q
**devicemapper: Error running deviceCreate (ActivateDevice) dm_task_run failed**
[RESOLVE](https://blog.csdn.net/weixin_30463341/article/details/99035583)
## 参考

[centos下安装启动docker](https://docs.docker.com/engine/install/centos/)

[es](https://cloud.tencent.com/developer/article/1704647)

