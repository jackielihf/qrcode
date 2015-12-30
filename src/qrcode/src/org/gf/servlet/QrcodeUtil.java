package org.gf.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrcodeUtil {
	// 图片默认尺寸
	private int nestedImageWidth = 0;
	private int nestedImageHeight = 0;
	private static HashMap<String, byte[]> cache = new HashMap<String, byte[]>();

	
	// 二维码写码器
	private static MultiFormatWriter mutiWriter = new MultiFormatWriter();

	/**
	 * 得到BufferedImage
	 * 
	 * @param content
	 *            二维码显示的文本
	 * @param width
	 *            二维码的宽度
	 * @param height
	 *            二维码的高度
	 * @param srcImagePath
	 *            中间嵌套的图片
	 * @return
	 * @throws WriterException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public BufferedImage genQrcode(String content, int width,
			int height, String srcImagePath) throws WriterException,
			IOException {
		//设置图像大小
		this.setNestedImageSize(width, height);
		
		// 读取源图像
		BufferedImage scaleImage = scale(srcImagePath, nestedImageWidth,
				nestedImageHeight, true);

		@SuppressWarnings("rawtypes")
		java.util.Hashtable hint = new java.util.Hashtable();
		hint.put(EncodeHintType.CHARACTER_SET, "utf-8");
		hint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hint.put(EncodeHintType.MARGIN, 0);
		// 生成二维码
		BitMatrix matrix = mutiWriter.encode(content, BarcodeFormat.QR_CODE,
				width, height, hint);
		
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
			}
		}
		
		//如果获取不到图像
		if(scaleImage == null){
			return image;
		}
		
		image = addLogo(image, scaleImage);
		return image;
	}
	
	/** 
     * 给二维码图片添加Logo 
     * 
     * @param qrPic 
     * @param logoPic 
     */  
    private BufferedImage addLogo(BufferedImage qrBufferedImage, BufferedImage logoBufferedImage)
    {  
        try  
        {     
            /** 
             * 读取二维码图片，并构建绘图对象 
             */   
            Graphics2D g = qrBufferedImage.createGraphics();  
            
            if(nestedImageWidth == 0 && nestedImageHeight == 0) {
            	nestedImageWidth = logoBufferedImage.getWidth();
            	nestedImageHeight = logoBufferedImage.getHeight();
            }
   
            // 计算图片放置位置  
            /** 
             * logo放在中心 
             */  
            int x = (qrBufferedImage.getWidth() - nestedImageWidth) / 2; 
            int y = (qrBufferedImage.getHeight() - nestedImageHeight) / 2;  

            //开始绘制图片  
            g.drawImage(logoBufferedImage, x, y, nestedImageWidth, nestedImageHeight, null);  
            //g.drawRoundRect(x, y, image_width, image_height, 5, 5);
            //g.drawRect(x, y, image_width, image_height);
            BasicStroke stroke = new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    		g.setStroke(stroke);// 设置笔画对象
    		g.setColor(Color.white);
               
            g.dispose();  
            logoBufferedImage.flush();  
            qrBufferedImage.flush();  
        }  
        catch (Exception e)  
        {  
            e.printStackTrace();  
        }
        return qrBufferedImage;
    }

	/**
	 * 把传入的原始图像按高度和宽度进行缩放，生成符合要求的图标
	 * 
	 * @param srcImageFile
	 *            源文件地址
	 * @param height
	 *            目标高度
	 * @param width
	 *            目标宽度
	 * @param hasFiller
	 *            比例不对时是否需要补白：true为补白; false为不补白;
	 * @throws IOException
	 */
	private BufferedImage scale(String srcImageFile, int height,
			int width, boolean hasFiller) {
		if(srcImageFile == null || srcImageFile.isEmpty()){
			return null;
		}				
		
        BufferedImage image = null;
		try {
			InputStream ins = this.getImageStream(srcImageFile);
			if(ins != null){
				image = ImageIO.read(ins);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	private InputStream getImageStream(String uri){
		if(uri == null || uri.isEmpty()){
			return null;
		}
		if(uri.contains("http")){
			return this.getHttpImageStream(uri);
		}else{
			return this.getLocalImageStreamWithCache(uri);
		}		
	}
	
	private byte[] readInputStream(InputStream inStream) throws Exception{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        //创建一个Buffer字符串  
        byte[] buffer = new byte[1024];  
        //每次读取的字符串长度，如果为-1，代表全部读取完毕  
        int len = 0;  
        //使用一个输入流从buffer里把数据读取出来  
        while( (len=inStream.read(buffer)) != -1 ){  
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度  
            outStream.write(buffer, 0, len);  
        }  
        //关闭输入流  
        inStream.close();  
        //把outStream里的数据写入内存  
        return outStream.toByteArray();  
	}
	public InputStream getHttpImageStream(String uri){
		URL url;
        ByteArrayInputStream bin = null;
		try {
			url = new URL(uri);
			//打开链接  
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
	        //设置请求方式为"GET"  
	        conn.setRequestMethod("GET");  
	        //超时响应时间为5秒  
	        conn.setConnectTimeout(5 * 1000);  
	        //通过输入流获取图片数据  
	        InputStream inStream = conn.getInputStream();  
	        //得到图片的二进制数据，以二进制封装得到数据，具有通用性  
	        try {
				byte[] data = readInputStream(inStream);
				bin = new ByteArrayInputStream(data);
			} catch (Exception e) {
				return null;
			} 
		} catch (MalformedURLException e) {			
			return null;
		} catch (IOException e) {			
			return null;
		}
		return bin;
	}
	public InputStream getLocalImageStreamWithCache(String uri){		
		//from cache
		byte[] data = cache.get(uri);
		
		if(data != null){
			return new ByteArrayInputStream(data);
		}		
		
		InputStream ins = this.getLocalImageStream(uri);
		//set cache
		if(ins != null){
			try {
				data = this.readInputStream(ins);				
				cache.put(uri, data);
				return new ByteArrayInputStream(data);
			} catch (Exception e) {
			}
		}
		return null;
				
	}
	public InputStream getLocalImageStream(String uri){
		File file = new File(uri);
		InputStream ins = null;
		if(file.exists()){
			try {
				ins = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return ins;
	}
	
	

	
	private void setNestedImageSize(int parentWidth, int parentHeight) {
		if(parentWidth > 0 && parentHeight > 0){
			this.nestedImageWidth = parentWidth/6; 
			this.nestedImageHeight = parentHeight/6;
		}
	}
}