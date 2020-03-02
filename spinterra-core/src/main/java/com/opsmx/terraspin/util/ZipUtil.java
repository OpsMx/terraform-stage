package com.opsmx.terraspin.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class ZipUtil {
	
	/*
	 * public static void zip(String targetPath, String destinationFilePath, String
	 * password) { try { ZipParameters parameters = new ZipParameters(); //
	 * parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
	 * //parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
	 * 
	 * 
	 * if(password.length()>0){ parameters.setEncryptFiles(true);
	 * parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
	 * parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
	 * parameters.setPassword(password); }
	 * 
	 * 
	 * ZipFile zipFile = new ZipFile(destinationFilePath);
	 * 
	 * File targetFile = new File(targetPath); if(targetFile.isFile()){
	 * zipFile.addFile(targetFile, parameters); }else if(targetFile.isDirectory()){
	 * zipFile.addFolder(targetFile, parameters); }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } }
	 * 
	 * 
	 * public static void unzip(String targetZipFilePath, String
	 * destinationFolderPath, String password) { try { ZipFile zipFile = new
	 * ZipFile(targetZipFilePath); if (zipFile.isEncrypted()) { //
	 * zipFile.setPassword(password); } zipFile.extractAll(destinationFolderPath);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } }
	 */

	////////////////

	
	static List<String> totalfiles = new ArrayList<String>();
	public void zipDirectory(String sourceDirectoryPath, String zipPath) throws IOException {

		try{
			File dir = new File(sourceDirectoryPath);
			String dirPath= dir.getAbsolutePath();
			ZipUtil Obj = new ZipUtil();
			
			//List all files of input directory by calling listFiles method
			Obj.listFiles(dir);
			
			//create a new zip file in which all input files have to be zipped.
			File zipFile = new File(zipPath);
			
			//create output stream for the zipfile.
			FileOutputStream fos = new FileOutputStream(zipFile);
			
			//create zipoutputstream for the outputstream.
			ZipOutputStream zos = new ZipOutputStream(fos);
			
			byte[] buffer = new byte[1024];
			int len;
			
			//for each file in list do zipping process
			for (String path : totalfiles) {
				File ipfile = new File(path);
				
				//for zipping purpose we need only relative path. we shouldn't consider absolute path.
				//this will give relative path of the file which we are zipping now.
				String zippath = path.substring(dirPath.length() + 1, path.length());
				
				//we should create zipentry for each file.
				ZipEntry zen = new ZipEntry(zippath);
				
				//adding to zipoutputstream
				zos.putNextEntry(zen);
				
				FileInputStream fis = new FileInputStream(ipfile);
				
				while ((len = fis.read(buffer)) > 0) {
                  zos.write(buffer, 0, len);
                }
				
				//close all IO streams. Or else we may get corrupted zip files
				zos.closeEntry();
				fis.close();
				
				System.out.println(ipfile.getAbsolutePath()+"is zipped");
			}
			
			zos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	void listFiles(File dir) throws IOException {
		File[] files = dir.listFiles();
		
		for (File file : files) {
			//check a file is directory or not
			if (file.isDirectory()) {
				//if a directory then make recursive call to list out subfiles
				listFiles(file);
			} else {
				//if it is a file then add it's absolute path to the list.
				totalfiles.add(file.getAbsolutePath());
			}
		}
	}
	
	

    public  void unzip(final String zipFilePath, final String unzipLocation) throws IOException {		
    	
    	File opdir = new File(unzipLocation);
	
	//we should check whether output exists or not
	if (!opdir.exists()) {
		//if not we should create. And if any parent folders not present then mkdirs() will create that parent folders
		opdir.mkdirs();
	}
	
	byte[] buffer = new byte[1024];
	int len;
	
	try {
		File ipfile = new File(zipFilePath);
		FileInputStream fis = new FileInputStream(ipfile);
		
		//zipinputstream will be useful for reading zipped contents.
		ZipInputStream zis = new ZipInputStream(fis);
		
		//each file in zip file we can get by getNextEntry() method.
		ZipEntry zen = zis.getNextEntry();
		
		//we should check whether it is corrupted zip file or not. if corrupted then zip entry will be null
		//we can't extract that corrupted files.
		while (zen != null) {
			String fileName = zen.getName();
			
			//files should be unzipped according their paths only.
			File newFile = new File(opdir + File.separator + fileName);
			
			//if any parent files are not present then we should create them also.
			new File(newFile.getParent()).mkdirs();
			
			FileOutputStream fos = new FileOutputStream(newFile);
			
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			
			System.out.println(fileName+" is unzippied now");
			
			//we should close io streams.
			fos.close();
			zis.closeEntry();
			zen = zis.getNextEntry();
		}
		
		zis.closeEntry();
		zis.close();
		fis.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}

    public static void unzipFiles(final ZipInputStream zipInputStream, final Path unzipFilePath) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipFilePath.toAbsolutePath().toString()))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }

    }

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String currentUserDir = System.getProperty("user.home");
		System.out.println("---" + currentUserDir);
		String currentUser = System.getProperty("user");
		System.out.println("---" + currentUser);

		/*
		 * String source = "/home/opsmx/lalit/work/opsmx/extrathing/gittest/test2"; File
		 * srcDir = new File(source); try { FileUtils.cleanDirectory(srcDir); } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		/*
		 * String source = "/home/opsmx/lalit/work/opsmx/extra/.testfol"; File srcDir =
		 * new File(source);
		 * 
		 * String destination = "/home/opsmx/lalit/work/opsmx/extrathing"; File destDir
		 * = new File(destination);
		 * 
		 * try { FileUtils.copyDirectoryToDirectory(srcDir, destDir);
		 * System.out.println("done!!"); } catch (IOException e) { e.printStackTrace();
		 * }
		 */

		/*
		 * String targetPath = "target\\file\\or\\folder\\path"; String zipFilePath =
		 * "zip\\file\\Path"; String unzippedFolderPath = "destination\\folder\\path";
		 * String password = "your_password"; // keep it EMPTY<""> for applying no
		 * password protection
		 */
		
		ZipUtil ZipUtil = new ZipUtil();

		String targetPath = "/home/terraspin/trytest/pipelineId-spinPipeId";
		String zipFilePath = "/home/opsmx/lalit/work/opsmx/extrathing/timepa/gittest2.zip";
		String unzippedFolderPath = "/home/opsmx/lalit/work/opsmx/extrathing/timepa/gittest2";
		String password = "your_password"; // keep it EMPTY<""> for applying no password protection

		// ZipUtil.zip(targetPath, zipFilePath, password);
		//ZipUtil.zipDirectory(targetPath, zipFilePath);
		ZipUtil.unzip(zipFilePath, unzippedFolderPath);
		

	}

}
