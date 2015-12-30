# qrcode
generating QRcode - 生成二维码

## api
GET /qrcode  
参数说明: 	
* url: 用于生成二维码的url (特殊字符需 encodeURI);
* size: 二维码尺寸（默认180）；
* img: 内嵌图片的地址（可选）；

sample:  
curl http://localhost:8080/qrcode?url=your-url-string&size=180
