使用 Docker Compose 部署 Docker Registry 并启用认证，可以通过以下步骤实现。我们将使用 htpasswd 文件来配置基本的用户名和密码认证。
1. 创建 docker-compose.yml 文件

在项目目录中创建一个 docker-compose.yml 文件，内容如下：
yaml
复制

version: '3.8'

services:
  registry:
    image: registry:2
    container_name: registry
    ports:
      - "5000:5000"
    environment:
      REGISTRY_AUTH: htpasswd
      REGISTRY_AUTH_HTPASSWD_REALM: Registry Realm
      REGISTRY_AUTH_HTPASSWD_PATH: /auth/htpasswd
    volumes:
      - registry_data:/var/lib/registry
      - ./auth:/auth

volumes:
  registry_data:

配置说明：

    REGISTRY_AUTH: htpasswd：启用基于 htpasswd 的认证。

    REGISTRY_AUTH_HTPASSWD_REALM：认证域的名称，可以自定义。

    REGISTRY_AUTH_HTPASSWD_PATH：指定 htpasswd 文件的路径（挂载到容器内的 /auth/htpasswd）。

    volumes：

        registry_data：用于持久化存储镜像数据。

        ./auth:/auth：将本地的 auth 目录挂载到容器内，用于存放 htpasswd 文件。

2. 创建 htpasswd 文件

    在项目目录下创建 auth 目录：
    bash
    复制

    mkdir -p auth

    使用 htpasswd 工具生成认证文件。如果没有 htpasswd 工具，可以通过 Docker 容器生成：
    bash
    复制

    docker run --rm --entrypoint htpasswd httpd:2 -Bbn myuser mypassword > auth/htpasswd

        myuser：用户名。

        mypassword：密码。

        -B：使用 bcrypt 加密（推荐）。

        -n：直接输出到标准输出。

        > auth/htpasswd：将输出重定向到 auth/htpasswd 文件。

    生成的文件内容类似如下：
    复制

    myuser:$2y$05$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

3. 启动 Docker Registry

在 docker-compose.yml 文件所在目录下运行以下命令启动服务：
bash
复制

docker-compose up -d

    -d 参数表示以后台模式运行容器。

4. 验证 Docker Registry 是否运行

    查看容器状态：
    bash
    复制

    docker-compose ps

    访问 Registry API：

    尝试访问 Registry API，应该会返回未授权的错误：
    bash
    复制

    curl http://localhost:5000/v2/_catalog

    输出示例：
    json
    复制

    {"errors":[{"code":"UNAUTHORIZED","message":"authentication required","detail":null}]}

5. 配置 Docker 客户端

    登录 Docker Registry：

    使用之前创建的用户名和密码登录：
    bash
    复制

    docker login localhost:5000

    输入用户名和密码后，登录成功。

    配置不安全的 Registry（可选）：

    如果 Docker Registry 使用 HTTP 而不是 HTTPS，需要配置 Docker 客户端以允许不安全的 Registry。

    编辑或创建 /etc/docker/daemon.json 文件，添加以下内容：
    json
    复制

    {
      "insecure-registries" : ["localhost:5000"]
    }

    重启 Docker 服务：
    bash
    复制

    sudo systemctl restart docker

6. 推送和拉取镜像
6.1 标记镜像

将本地镜像标记为指向本地仓库：
bash
复制

docker tag my-image localhost:5000/my-image

6.2 推送镜像

将镜像推送到本地仓库：
bash
复制

docker push localhost:5000/my-image

6.3 拉取镜像

从本地仓库拉取镜像：
bash
复制

docker pull localhost:5000/my-image

7. 停止和清理

如果需要停止 Docker Registry，可以运行以下命令：
bash
复制

docker-compose down

如果需要删除持久化数据（如镜像存储），可以添加 -v 参数：
bash
复制

docker-compose down -v

8. 扩展配置（可选）
8.1 启用 HTTPS

如果需要启用 HTTPS，可以使用以下步骤：

    生成自签名证书：
    bash
    复制

    mkdir -p certs
    openssl req -newkey rsa:4096 -nodes -sha256 -keyout certs/domain.key -x509 -days 365 -out certs/domain.crt

    修改 docker-compose.yml，挂载证书并配置 TLS：
    yaml
    复制

    version: '3.8'

    services:
      registry:
        image: registry:2
        container_name: registry
        ports:
          - "5000:5000"
        environment:
          REGISTRY_AUTH: htpasswd
          REGISTRY_AUTH_HTPASSWD_REALM: Registry Realm
          REGISTRY_AUTH_HTPASSWD_PATH: /auth/htpasswd
          REGISTRY_HTTP_TLS_CERTIFICATE: /certs/domain.crt
          REGISTRY_HTTP_TLS_KEY: /certs/domain.key
        volumes:
          - registry_data:/var/lib/registry
          - ./auth:/auth
          - ./certs:/certs

    volumes:
      registry_data:

    重启服务：
    bash
    复制

    docker-compose up -d

    配置 Docker 客户端信任自签名证书：

        将 domain.crt 复制到 Docker 客户端的证书目录（例如 /etc/docker/certs.d/localhost:5000/ca.crt）。

        重启 Docker 服务。

通过以上步骤，你可以使用 Docker Compose 部署一个带有认证功能的 Docker Registry，并根据需要启用 HTTPS。