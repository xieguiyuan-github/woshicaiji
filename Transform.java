package com.fh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.fh.common.WebContext;

public class Transform {
	
	//将文件拷贝到服务器，返回访问路径
	public static String trans(CommonsMultipartFile file){
		HttpServletRequest req = WebContext.getRequest();
		//图片访问路径
		String imgUrl = null; 
		//图片存放文件夹
		String str = "picture";
		//图片存放在服务器的路径
		String path = req.getServletContext().getRealPath(str);
		File f = new File(path);
		//如果该文件夹不存在则创建一个
		if (!f.exists()) {
			f.mkdirs();
		}
		//获取上传文件的文件名
		String originalFilename = file.getOriginalFilename();
		//截取上传文件后缀
		int index = originalFilename.lastIndexOf(".");
		String unloadPath = originalFilename.substring(index);
		//设置保存时的文件名
		String newFileName = String.valueOf(new Date().getTime()) + unloadPath;
		try {
			//文件拷贝到服务器
			file.transferTo(new File(path + "/" + newFileName));
			//返回文件保存路径
			imgUrl = str + "/" + newFileName;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imgUrl;
	}
	
	
	
	//将文件拷贝到服务器，返回访问路径
	public static String transFTP(CommonsMultipartFile file){
		HttpServletRequest req = WebContext.getRequest();
		//图片访问路径
		String imgUrl = "http://192.168.235.130:8080/"; 
		if (!file.isEmpty()) {
			//图片存放文件夹
			String str = "picture";
			//图片存放在服务器的路径
			String path = req.getServletContext().getRealPath(str);
			File f = new File(path);
			//如果该文件夹不存在则创建一个
			if (!f.exists()) {
				f.mkdirs();
			}
			//获取上传文件的文件名
			String originalFilename = file.getOriginalFilename();
			//截取上传文件后缀
			int index = originalFilename.lastIndexOf(".");
			String unloadPath = originalFilename.substring(index);
			//设置保存时的文件名
			String newFileName = String.valueOf(new Date().getTime()) + unloadPath;
			
			//创建一个文件流
			FileInputStream fis = null;
			//创建一个客户端
			FTPClient ftpClient = new FTPClient();
			//上传文件
			File tfile = new File(path,newFileName);
			try {
				//文件拷贝到服务器
				file.transferTo(tfile);
				//通过ip地址进行连接  魔法数字
				ftpClient.connect(FtpClientGlobal.FTP_IP,FtpClientGlobal.FTP_PORT);
				//用用户名密码登陆
				boolean login = ftpClient.login(FtpClientGlobal.FTP_USERNAME, FtpClientGlobal.FTP_PASSWORD);
				if(login){
					//切换工作目录
					boolean checkWorkingDirectory = ftpClient.changeWorkingDirectory(FtpClientGlobal.FTP_IMG_DIRECTORY);
					//判断如果没有，创建文件
					if (!checkWorkingDirectory) {
						ftpClient.makeDirectory(FtpClientGlobal.FTP_IMG_DIRECTORY);
						ftpClient.changeWorkingDirectory(FtpClientGlobal.FTP_IMG_DIRECTORY);
					}
					//转换字符集编码
					ftpClient.setControlEncoding(FtpClientGlobal.FTP_CODING);
					//创建缓冲区
					ftpClient.setBufferSize(1024);
					//更改二进制流的编码
					ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
					//开启被动动模式ftp client告诉ftp server开通一个端口来传输数据（否则可能上传的图片没有内容）
					ftpClient.enterLocalPassiveMode();
					//创建文件上传的流
					fis = new FileInputStream(tfile);
					//进行上传
					ftpClient.storeFile(tfile.getName(), fis);
					//关流
					fis.close();
					ftpClient.logout();
					//删除本地的文件
					tfile.delete();
					
				}else{
					System.out.println("没有连接上FTP服务器");
				}
				//返回文件保存路径
				imgUrl = imgUrl + newFileName;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return imgUrl;
	}
	
	
     /**
      * Description: 从FTP服务器下载文件
      *@param fileName
      *            要下载的文件名
      *@return
      */
     public static void downFile(String fileName,HttpServletResponse resp) {
           FTPClient ftp= new FTPClient();
           fileName = fileName.substring(fileName.lastIndexOf("/")+1);
           int reply;
           try{
                ftp.connect(FtpClientGlobal.FTP_IP,FtpClientGlobal.FTP_PORT);
                ftp.login(FtpClientGlobal.FTP_USERNAME,FtpClientGlobal.FTP_PASSWORD);
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);//文件类型为二进制文件
                reply= ftp.getReplyCode();
                if(!FTPReply.isPositiveCompletion(reply)) {
                     ftp.disconnect();
                     System.out.println("连接失败！！！");
                }
                ftp.enterLocalPassiveMode();//本地模式
                ftp.changeWorkingDirectory(FtpClientGlobal.FTP_IMG_DIRECTORY);
                resp.setCharacterEncoding("utf-8");
    			resp.setContentType("application/octet-stream");//下载文件     upload/wenjian.jsp  名字
    			resp.setHeader("Content-Disposition", "attachment;filename="+fileName);
    			OutputStream  os = resp.getOutputStream();
                ftp.retrieveFile(fileName,os);
                os.close();
                //从目标文件夹比较下载
                /*FTPFile[] fs = ftp.listFiles();
                for(FTPFile ff: fs) {
                     if(ff.getName().equals(fileName)) {
                           OutputStream  os = resp.getOutputStream();
                           ftp.retrieveFile(ff.getName(),os);
                           os.close();
                     }
                }*/
                ftp.logout();
                ftp.disconnect();
           }catch(SocketException e) {
                //TODOAuto-generated catch block
                e.printStackTrace();
           }catch(IOException e) {
                //TODOAuto-generated catch block
                e.printStackTrace();
           }finally{
                if(ftp.isConnected()) {
                     try{
                           ftp.disconnect();
                     }catch(IOException e) {
                           //TODOAuto-generated catch block
                           e.printStackTrace();
                     }
                }
           }
     }
	
	public static void main(String[] args) {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect(FtpClientGlobal.FTP_IP,FtpClientGlobal.FTP_PORT);
			boolean login = ftp.login(FtpClientGlobal.FTP_USERNAME, FtpClientGlobal.FTP_PASSWORD);
			if (login) {
				System.out.println("连接上FTP");
				boolean checkWorkingDirectory = ftp.changeWorkingDirectory(FtpClientGlobal.FTP_IMG_DIRECTORY);
				//判断如果没有，创建文件
				if (!checkWorkingDirectory) {
					ftp.makeDirectory(FtpClientGlobal.FTP_IMG_DIRECTORY);
				}
			}else{
				System.out.println("连接错误");
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//用用户名密码登陆
		
	}

}
