
FROM docker.gf.com.cn/gf-tomcat:20150812
# author
MAINTAINER lihuafeng <lihuafeng@gf.com.cn>
 
RUN mkdir -p /opt/qrcode
ADD ./dist/qrcode /opt/qrcode/
RUN cp /opt/qrcode -r /opt/tomcat/webapps
 
# ssh
RUN mkdir /var/run/sshd -p
EXPOSE 22

#
WORKDIR /opt/tomcat
CMD sh ./bin/startup.sh && /usr/sbin/sshd && tail -f /etc/hosts


