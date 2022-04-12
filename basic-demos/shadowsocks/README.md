Linux配置Shadowsocks-libev+V2Ray与多用户
一、安装Shadowsocks-libev
安装方法见：https://blog.lyz810.com/article/2017/04/install-shadowsocks-libev-on-centos/

之所以用Shadowsocks-libev而不用Shadowsocks，是因为后者在多用户模式下无法配置插件，单用户是可以的

Shadowsocks-libev通过ss-manager启动多个ss-server，每个ss-server都可以用一个生成的配置文件来启动

二、下载v2ray插件
下载地址：https://github.com/shadowsocks/v2ray-plugin/releases

linux64位的系统可以下载linux-amd64的最新版本，本文以1.1.0版本为例

wget https://github.com/shadowsocks/v2ray-plugin/releases/download/v1.1.0/v2ray-plugin-linux-amd64-v1.1.0.tar.gz

tar -xzf v2ray-plugin-linux-amd64-v1.1.0.tar.gz

mv v2ray-plugin_linux_amd64 /usr/local/bin/v2ray-plugin
至此，已经将插件下载并保存到我们希望的目录下了

三、服务器配置文件
我们假设配置文件放在了/etc/shadowsocks-libev/v2ray.conf中（需要创建），打开并编辑此文件

{
    "server": "0.0.0.0",
    "local_port": 1080,
    "port_password": {
	"10000": "密码1",
	"10010": "密码2",
	"10086": "密码3"
    },
    "timeout": 60,
    "method": "chacha20-ietf-poly1305",
    "plugin": "v2ray-plugin",
    "plugin_opts": "server",
    "reuse_port": true
}
该配置比不用插件多了两行，”plugin”: “v2ray-plugin”,表示使用v2ray-plugin插件，请确保v2ray-plugin保存的目录在环境变量中，也就是在任意目录下直接输入v2ray-plugin回车就可以执行而不是报错找不到命令，此处也可以写绝对路径。
“plugin_opts”: “server”,这一行是插件的参数，其中server表示是服务器使用，host表示服务器名称，path为路径，客户端需要与此配置对应，这个配置是最简单的默认配置，及使用websocket+http的方式，后面文章会介绍一种更复杂的quic配置方式

四、客户端配置
根据客户端操作系统选择第一节链接中的对应版本进行下载，在Mac系统下（使用ShadowSocksX-NG客户端），将插件保存到~/Library/Application Support/ShadowsocksX-NG/plugins中，并更改名称为v2ray，Windows下可以保存任意位置

服务器地址、端口、密码、加密方式按实际填写，与服务器配置保持一致，插件填v2ray（Mac下改的插件文件名）或插件保存的绝对路径（windows系统），插件选项留空

五、启动服务器
nohup ss-manager -c /etc/shadowsocks-libev/v2ray.conf &
发布于2017年4月23日
格式日志
分类linux、Server
标签shadowsocks、v2ray、代理
于Linux配置Shadowsocks-libev+V2Ray与多用户留下评论
CentOS 编译安装Shadowsocks-libev并配置多用户
一、安装依赖
yum install epel-release -y
yum install gcc gettext autoconf libtool automake make pcre-devel asciidoc xmlto c-ares-devel libev-devel libsodium-devel mbedtls-devel -y
二、下载源码
在下面链接中选择一个版本下载，后面的步骤以3.3.1版本为准，其他版本大同小异
https://github.com/shadowsocks/shadowsocks-libev/releases

wget https://github.com/shadowsocks/shadowsocks-libev/releases/download/v3.3.1/shadowsocks-libev-3.3.1.tar.gz
三、编译安装
解压缩并进入目录
tar -xzf shadowsocks-libev-3.3.1.tar.gz
cd shadowsocks-libev-3.3.1
编译安装
./configure && make
sudo make install
执行完毕后，shadowsocks会默认安装到/usr/local/bin中，包含ss-local、ss-manager、ss-nat、ss-redir、ss-server、ss-tunnel几个命令

四、服务器配置(多端口多用户配置)
创建配置文件
cd /etc
mkdir shadowsocks-libev
cd shadowsocks-libev
touch simple-config.conf
编写配置文件
{
  "server":"0.0.0.0",
  "local_port":1080,
  "port_password": {
     "10000": "用户1的密码",
     "10010": "用户2的密码",
     "10086": "用户3的密码"
  },
  "timeout":60,
  "method":"aes-256-cfb",
  "reuse_port":true
}
编写完成后进行保存

启动服务器
ss-manager -c /etc/shadowsocks-libev/simple-config.conf
放到后台执行
nohup ss-manager -c /etc/shadowsocks-libev/simple-config.conf &
客户端配置好后就可以愉快的科学上网了，客户端的server对应服务器的IP或域名，端口对应的是port_password中的任一端口（如10000等），加密方法对应配置文件中的method，密码为对应端口的密码
REF

WARN:
```
yum install epel-release -y
yum install gcc gettext autoconf libtool automake make pcre-devel asciidoc xmlto c-ares-devel libev-devel libsodium-devel mbedtls-devel -y
```
[Web技术研究室](https://blog.lyz810.com/article/tag/shadowsocks/)
[error: mbed TLS libraries not found](https://blog.csdn.net/ypbsyy/article/details/84338748)
[git](https://github.com/shadowsocks/shadowsocks-libev)