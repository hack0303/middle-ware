npm镜像及配置方法
npm全称Node Package Manager，是node.js的模块依赖管理工具。由于npm的源在国外，所以国内用户使用起来各种不方便。下面整理出了一部分国内优秀的npm镜像资源，国内用户可以选择使用。

国内优秀npm镜像
淘宝npm镜像
搜索地址：http://npm.taobao.org/
registry地址：http://registry.npm.taobao.org/
cnpmjs镜像
搜索地址：http://cnpmjs.org/
registry地址：http://r.cnpmjs.org/
如何使用
有很多方法来配置npm的registry地址，下面根据不同情境列出几种比较常用的方法。以淘宝npm镜像举例：

1.临时使用
npm --registry https://registry.npm.taobao.org install express
 

2.持久使用（推荐使用）
 

打开cmd使用命令：

npm config set registry https://registry.npm.taobao.org

// 配置后可通过下面命令来验证是否成功
　npm config ls

// 此时：metrics-registry = "http://registry.npm.taobao.org/"表示设置成功


npm config get registry

// 或
npm info express
3.通过cnpm使用 （也可以使用cnpm）
npm install -g cnpm --registry=https://registry.npm.taobao.org
// 使用
cnpm install expresstall express

## FIX 01
```For local projects
For all your repositories, you can set:

git config --global url."https://github.com/".insteadOf git://github.com/
You can also use SSH, but GitHub Security reminds us that, as of March 15th, 2022, GitHub stopped accepting DSA keys. RSA keys uploaded after Nov 2, 2021 will work only with SHA-2 signatures.
The deprecated MACs, ciphers, and unencrypted Git protocol are permanently disabled.

So this (with the right key) would work:

git config --global url."git@github.com:".insteadOf git://github.com/
That will change any git://github.com/ (unencrypted Git protocol) into git@github.com: (SSH URL).
```
REF
[git clone出现 fatal: unable to access 'https://github.com/...'的解决办法(亲测有效)](https://blog.csdn.net/Dashi_Lu/article/details/89641778?spm=1001.2101.3001.6650.3&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-3.pc_relevant_paycolumn_v3&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7ERate-3.pc_relevant_paycolumn_v3&utm_relevant_index=5)
[镜像设置](https://blog.csdn.net/qq_31307269/article/details/106736215)
[The unauthenticated git protocol on port 9418 is no longer supported](https://stackoverflow.com/questions/70663523/the-unauthenticated-git-protocol-on-port-9418-is-no-longer-supported)
[fatal: unable to access 'https://github.com/xxx': OpenSSL SSL_connect: SSL_ERROR_SYSCALL in connection to github.com:443 [duplicate]](https://stackoverflow.com/questions/49345357/fatal-unable-to-access-https-github-com-xxx-openssl-ssl-connect-ssl-error)