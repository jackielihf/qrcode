#!/bin/bash

cd $(cd `dirname $0`;pwd)

#host=docker.gf.com.cn
name=qrcode
ver=20150812
#image=$host/$name:$ver
image=$name:$ver
echo app:version $image


port=8080
home=/opt/qrcode
# home in docker
tomcat_home=/opt/tomcat
dhome=/opt/tomcat/webapps/qrcode
  
 
# 启动image，将日志和配置目录mount到container中
sudo docker run --name $name -d -p $port:$port -v $home/logs:$tomcat_home/logs -v $home/conf/qrcode.properties:$dhome/WEB-INF/classes/qrcode.properties -v /etc/localtime:/etc/localtime:ro $image

