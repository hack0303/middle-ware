# 常用场景

## git global setup （全局设置）

```
git config --global user.name "xxx"

git config --global user.email "xxx@email.com"
```

## create a new repository 

```
git clone xxx.git

cd xxxfolder

touch README.md

git add README.md

git commit -m "add README"

git push -u origin master
```

## existing folder

```
cd existing_folder

git init

git remote add origin xxx.git

git add .

git commit -m "init commit"

git push -u origin master
```



## existing git respository 

```
cd existing_repo

git remote rename origin old-origin

git remote add origin xxx.git

git push -u origin --all

git push -u origin --tags
```



## git change local/remote branch name

```
git branch -m old_branch new branch

git push origin :old_branch

git push --set-upstream origin new_branch
```

REF
[Git:推送到新的远程地址;更改本地和远程分支的名称](https://blog.csdn.net/fly910905/article/details/91821452)