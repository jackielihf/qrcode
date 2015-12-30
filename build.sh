
#!/bin/bash

name=qrcode
ver=`date "+%Y%m%d"`

echo app:version: $name:$ver

cd $(cd `dirname $0`;pwd)
sudo docker build -t $name:$ver .

