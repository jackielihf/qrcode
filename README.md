# qrcode
generating QRcode - 生成二维码

## api
GET /qrcode
query params: 	
* url: 用于生成二维码的url；
* size: 二维码尺寸（默认180）；
* img: 内嵌图片的地址（可选）；

sample:  
curl http://localhost:8080/qrcode?url=http://www.baidu.com&size=180
