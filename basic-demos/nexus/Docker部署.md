1. 创建数据目录

在主机上创建用于持久化 Nexus 数据的目录：
bash
复制

sudo mkdir -p /home/nexus/data
sudo chown -R 200:200 /home/nexus/data  # 确保 Nexus 用户有权限访问

注意：Nexus 容器默认以用户 ID 200 运行，因此需要确保挂载的目录对该用户有读写权限。

2. 使用 Docker 运行 Nexus 容器

使用以下命令运行 Nexus 容器，并配置自动启动和数据挂载：
bash
复制
```
docker run -d \
--name nexus \
--restart unless-stopped \
-p 8081:8081 \
-v /home/nexus/data:/nexus-data \
sonatype/nexus3
```
参数说明：

-d：后台运行容器。

--name nexus：为容器指定名称。

--restart unless-stopped：配置容器自动启动策略：

unless-stopped：除非手动停止，否则容器会自动重启（例如主机重启后）。

也可以使用 --restart always，表示总是自动重启。

-p 8081:8081：将容器的 8081 端口映射到主机的 8081 端口。

-v /home/nexus/data:/nexus-data：将主机的 /home/nexus/data 目录挂载到容器的 /nexus-data 目录，用于持久化 Nexus 数据。

3. 验证容器状态

运行以下命令，检查容器是否正常运行：
bash
复制

docker ps

如果容器状态为 Up，则表示运行成功。
4. 访问 Nexus Web 界面

在浏览器中访问 http://<主机IP>:8081，进入 Nexus Web 界面。

默认用户名：admin

初始密码：通过以下命令查看容器日志，找到 admin.password：
bash
复制

docker logs nexus

5. 配置 Nexus

修改密码：登录后按提示修改默认密码。

创建 Maven 仓库：

进入 Repository 菜单。

点击 Create repository。

选择 maven2 (hosted)，设置仓库名称（如 maven-releases），选择 Version policy 为 Release 或 Snapshot。

保存设置。

6. 验证数据挂载

检查主机上的 /home/nexus/data 目录，确认 Nexus 数据已正确挂载：
bash
复制

ls -l /home/nexus/data

如果看到类似 blobs/, cache/, db/ 等目录，说明数据挂载成功。
7. 配置 Maven 使用私有仓库

在 ~/.m2/settings.xml 中添加以下配置：
xml
复制

<settings>
<servers>
<server>
<id>nexus</id>
<username>admin</username>
<password>your_password</password>
</server>
</servers>
<mirrors>
<mirror>
<id>nexus</id>
<mirrorOf>*</mirrorOf>
<url>http://<主机IP>:8081/repository/maven-public/</url>
</mirror>
</mirrors>
</settings>

运行 HTML
8. 测试部署

使用以下命令将应用部署到 Nexus：
bash
复制

mvn deploy:deploy-file -DgroupId=com.example \
-DartifactId=your-artifact \
-Dversion=1.0.0 \
-Dpackaging=jar \
-Dfile=path/to/your-file.jar \
-DrepositoryId=nexus \
-Durl=http://<主机IP>:8081/repository/maven-releases/

9. 其他操作

停止容器：
bash
复制

docker stop nexus

启动容器：
bash
复制

docker start nexus

删除容器：
bash
复制

docker rm -f nexus

通过以上步骤，Nexus Repository Manager 将以自动启动的方式运行，并将数据持久化到主机的 /home/nexus/data 目录。