/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.ftp;

import gnext.dbutils.util.StringUtil;
import java.io.File;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author daind
 */
public class FtpUtil {
    private FtpUtil() {}
    
    /**
     * Hàm xử lí xóa thư mục trên server.
     * @param ftpClient
     * @param parentDir
     * @param currentDir
     * @throws IOException 
     */
    public static void removeDirectory(FTPClient ftpClient, String parentDir, String currentDir)
            throws IOException {
        // kiểm tra nếu folder hoặc file cần xóa không chứa home folder thì cần append thêm vào đường dẫn tương đối.
        String homeFolder = ftpClient.printWorkingDirectory();
        if(!parentDir.startsWith(homeFolder)) parentDir = homeFolder + parentDir;
        
        String dir = parentDir;
        if (!StringUtil.isEmpty(currentDir)) dir += File.separator + currentDir;
        
        FTPFile[] subFiles = ftpClient.listFiles(dir);
        
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) continue;

                String filePath = parentDir + File.separator + currentDir + File.separator + currentFileName;
                if (currentDir.equals("")) filePath = parentDir + File.separator + currentFileName;

                if (aFile.isDirectory()) {
                    removeDirectory(ftpClient, dir, currentFileName);
                } else {
                    ftpClient.deleteFile(filePath);
                }
            }
        }
        
        ftpClient.removeDirectory(dir);
    }
}
