# build
docker pull star7th/showdoc

mkdir /var/showdoc_data
mkdir /data/showdoc_data/html
chmod 777 -R /data/showdoc_data

docker run -d --name showdoc -p 4999:80 -v /data/showdoc_data/html:/var/www/html/ star7th/showdoc

[showdoc-docker](https://github.com/star7th/showdoc/blob/master/documentation/en/ByDocker.md)