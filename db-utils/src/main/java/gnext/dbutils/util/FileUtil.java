/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author daind
 */
public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private FileUtil() {}
    
    /**
     * Hàm xử lí xóa danh sách file.
     * @param files 
     */
    public static void deleteFiles(List<File> files) {
        if(files == null || files.isEmpty()) return;
        for(File f : files) try { org.apache.commons.io.FileUtils.forceDelete(f); } catch (Exception e) { }                
    }
    
    /**
     * Hàm xử lí lấy mảng bytes từ {@link InputStream}.
     * @param in
     * @return 
     */
    public static byte[] copyFromInputStream(InputStream in) {
        try {
            return IOUtils.toByteArray(in);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            try { in.reset(); } catch (Exception e) { }
        }
        return null;
    }
    
    /**
     * Clone a inputstream to another inputstream.
     * @param in
     * @return 
     */
    public static InputStream cloneStream(InputStream in) {
        if(in == null) return null;
        try {
            byte[] byteArray = copyFromInputStream(in);
            return new ByteArrayInputStream(byteArray);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            try { in.reset(); } catch (Exception e) { }
        }
        return null;
    }
    
    /***
     * Hàm xử lí tạo Temp file từ InputStream.
     * @param in
     * @param fileName
     * @return
     * @throws IOException 
     */
    public static File stream2file(InputStream in, String fileName) throws IOException {
        try {
            String prefix = FilenameUtils.getBaseName(fileName);
            String suffix = FilenameUtils.getExtension(fileName);
            final File tempFile = File.createTempFile(prefix, "." + suffix);
            org.apache.commons.io.FileUtils.copyInputStreamToFile(in, tempFile);
            return tempFile;
        } finally {
            try { in.reset(); } catch (Exception e) { } // chuyển con trỏ về đầu stream.
        }
    }
    
    /***
     * Hàm tính toán size từ InputStream.
     * @param in
     * @param fileName
     * @return 
     */
    public static long calSize(InputStream in, String fileName) {
        File tempFile = null;
        try {
            tempFile = FileUtil.stream2file(in, fileName);
            return org.apache.commons.io.FileUtils.sizeOf(tempFile);
        } catch (Exception e) { 
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            if(tempFile != null) try { org.apache.commons.io.FileUtils.forceDelete(tempFile); }
            catch (Exception e) { }
        }
        return 0;
    }
    
    /**
     * Hàm tính checksum của {@link InputStream}.
     * @param in
     * @param fileName
     * @return 
     */
    public static String calChecksum(InputStream in, String fileName) {
        File tempFile = null;
        try {
            tempFile = FileUtil.stream2file(in, fileName);
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return getFileChecksum(md, tempFile);
        } catch (Exception e) { 
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            try { if(tempFile != null) org.apache.commons.io.FileUtils.forceDelete(tempFile); } catch (Exception e) { }
        }
        return null;
    }
    
    /**
     * Hàm kiểm tra mimetype của {@link InputStream}.
     * @param in
     * @param fileName
     * @return 
     */
    public static String getMimeType(InputStream in, String fileName) {
        File tempFile = null;
        try {
            tempFile = FileUtil.stream2file(in, fileName);
            Path source = Paths.get(tempFile.getAbsolutePath());
            return Files.probeContentType(source);
        } catch (Exception e) { 
            LOGGER.error(e.getLocalizedMessage(), e);
        } finally {
            try { if(tempFile != null) org.apache.commons.io.FileUtils.forceDelete(tempFile); } catch (Exception e) { }
        }
        return "";
    }
    
    /**
     * Hàm tính checksum {@link InputStream}.
     * 
     * @param digest
     * @param fis
     * @return
     * @throws IOException 
     */
    public static String getFileChecksum(MessageDigest digest, InputStream fis) throws IOException {
        try {
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            };
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } finally {
            try { if(fis != null) fis.close(); } catch (Exception e) { }
        }
    }
    public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        InputStream is = null;
        try {
            // A seemingly small change that could reduce GC pauses if you do a lot of file I/O!
            is  = Files.newInputStream(Paths.get(file.getAbsolutePath()));
//            is = new FileInputStream(file);
            return getFileChecksum(digest, is);
        } finally {
            try { if(is != null) is.close(); } catch (Exception e) { }
        }
    }
    
    /**
     * Lấy thông tin từ File properties với path là đường dẫn tới File.
     * @param path
     * @return 
     */
    public static Properties loadConf(String path) {
        return loadConf(new File(path));
    }
    
    /**
     * Lấy thông tin từ File properties với path là đường dẫn tới File.
     * @param resource
     * @return 
     */
    public static Properties loadConf(File resource) {
        InputStream is = null;
        try {
            // A seemingly small change that could reduce GC pauses if you do a lot of file I/O!
            is = Files.newInputStream(Paths.get(resource.getAbsolutePath()));
            Properties prop = new Properties();
            prop.load(is);
            return prop;
        } catch (IOException io) {
            LOGGER.error(io.getLocalizedMessage(), io);
            Console.error(io);
        } finally {
            if(is != null) try { is.close(); } catch (IOException e) {}
        }
        
        return null;
    }
    
    /**
     * Hàm load file từ resource trong jar hoặc của chính ứng dụng.
     * @param resource
     * @param clazz
     * @return 
     */
    public static File loadResource(String resource, Class<?> clazz) {
        File file = null;
        URL res = clazz.getResource(resource);
        InputStream input = null;
        OutputStream out = null;
        try {
            if (res.toString().startsWith("jar:")) {
                input = clazz.getResourceAsStream(resource);
                file = File.createTempFile("tempfile", ".tmp");
                out = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int read;
                while ((read = input.read(bytes)) != -1) out.write(bytes, 0, read);
            } else {
                file = new File(res.getFile());
            }
            if (file != null && !file.exists()) throw new RuntimeException("Error: File " + file + " not found!");
        } catch(Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            Console.error(e.getLocalizedMessage());
        }finally {
            try { input.close(); } catch (Exception e) { }
            try { out.close(); } catch (Exception e) { }
        }
        return file;
    }
    
    /**
     * Làm mịn đường dẫn của folder.
     * @param origin
     * @return 
     */
    public static String smoothingPath(String origin) {
        if(origin == null || origin.isEmpty()) return "";
        return origin.replaceAll("/+", "/");
    }
}
