package org.gf.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.zxing.WriterException;

public class QrcodeServlet extends HttpServlet {
	private static final long serialVersionUID = -5084244362162482675L;
	private static int maxSize = 600;
	private static int minSize = 30;
	private static int defaultWidth = 180;
	private static int defaultHeight = 180;
	private static String qrcodeImageType = "jpeg"; 
	private static String defaultImageKey = "defaultImage"; 
	private static HashMap<String, String> cache = new HashMap<String, String>();
	
	public void init() throws ServletException {
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String img = request.getParameter("img");
		String size = request.getParameter("size");
		String url = request.getParameter("url");		
		
		//参数
		if(url == null || url.isEmpty()){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("{\"msg\":\"bad params: url\"}");
			return;
		}
		int width = defaultWidth;
		int height = defaultHeight;
		try{			
			int imgSize = Integer.parseInt(size);
			//invalid if bigger than 600*600
			if(imgSize >= minSize && imgSize <= maxSize){
				width = height = imgSize;
			}
		}catch(NumberFormatException e){
			//use default size
		}
		if(img == null || img.isEmpty()){
			img = this.getDefaultImagePath();			
		}					        
        QrcodeUtil qrcodeUtil = new QrcodeUtil();
        BufferedImage bufferedImage = null;
        try {
        	//获取图像流
			bufferedImage = qrcodeUtil.genQrcode(url, width, height, img);
		} catch (WriterException e) {
			e.printStackTrace();
		}
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        ImageIO.write(bufferedImage, qrcodeImageType, out);
        byte[] b = out.toByteArray();  
        
        response.setContentType("image/".concat(qrcodeImageType)); //设置返回的文件类型   
        OutputStream os = response.getOutputStream();  
        os.write(b);  
        os.flush();  
        os.close();
   	}
	
	public String getDefaultImagePath(){
		String filename = this.getPropertyValue(defaultImageKey);			
		if(filename != null && !filename.isEmpty()){
			String baseUrl = this.getBasePath();
			return baseUrl.replace("/WEB-INF/classes", "/images")+filename;			
		}
		return null;
	}
	
	public String getBasePath(){
		String path = this.getClass().getResource("/").getPath();				
		if(!path.endsWith("/")){
			path += "/";
		}
		return path;
	}
	
	//获取配置文件qrcode.properties的值
	public String getPropertyValue(String key) {
		//from cache		
		String value = cache.get(key);
		if(value != null){			
			return value;
		}
		String retStr = null;        
        String config = getBasePath() + "qrcode.properties";        
//        System.out.println(config);
        
		Properties properties = new Properties();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(config);
			properties.load(fileInputStream);
			retStr = properties.getProperty(key);
			//set cache
			cache.put(key, retStr);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(fileInputStream != null){
					fileInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return retStr;
	}
	
	
	//判断字符串是否数字
	public boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	//判断字符串是否数字
	public boolean isOk(String str){
		
		Pattern pattern = Pattern.compile("_(\\d*)x(\\d*)");
		return pattern.matcher(str).find();
	}
	
	public void destroy() {
		super.destroy();
	}

}