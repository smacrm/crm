/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

import gnext.dbutils.services.Connection;
import gnext.dbutils.util.FileUtil;
import gnext.filetransfer.exceptions.FileTransferDownloadException;
import gnext.filetransfer.exceptions.FileTransferUploadException;
import java.io.InputStream;
import lombok.Getter;

/**
 *
 * @author daind
 */
public abstract class BaseFileTransfer {
    @Getter private final Parameter param;
    public BaseFileTransfer(Parameter param) { this.param = param; }
    public Connection getConn(String path, Integer companyId) { return new Connection(path, companyId); }
    
    /**
     * Hàm kiểm tra tham số đầu vào cho việc test connection.
     * @throws Exception 
     */
    protected abstract void checkTestConnectionParam() throws Exception;
    /**
     * Hàm thực hiện test.
     * @throws Exception 
     */
    protected abstract void testConnection() throws Exception;
    
    /**
     * Hàm API gọi test connection.
     * @throws Exception 
     */
    public void test() throws Exception {
        checkTestConnectionParam();
        testConnection();
    }
    
    
    /***
     * Download dữ liệu theo đường dẫn.
     * @param pathToFile
     * @return
     * @throws FileTransferDownloadException 
     */
    public InputStream download(String pathToFile) throws FileTransferDownloadException {
        checkDownloadparam();
        return executeDowload(pathToFile);
    }
    /**
     * Hàm kiểm tra dữ liệu đầu vào trước khi thực hiện Download.
     * @throws FileTransferDownloadException 
     */
    protected abstract void checkDownloadparam() throws FileTransferDownloadException;
    /**
     * Hàm xử lí dơwnload.
     * @param pathToFile
     * @return
     * @throws FileTransferDownloadException 
     */
    protected abstract InputStream executeDowload(String pathToFile) throws FileTransferDownloadException;

    
    /***
     * Upload theo stream.
     * @param in - được copy 1 bản tránh việc close stream.
     * @throws FileTransferUploadException 
     */
    public void upload(InputStream in) throws FileTransferUploadException {
        InputStream cloneIn = null;
        try {
            if(in != null) cloneIn = FileUtil.cloneStream(in); // chuyển sang kiểu byte stream cho phép reset con trỏ.
            checkUploadparam();
            executeUpload(cloneIn);
        } catch(Exception e) {
            throw new FileTransferUploadException(e);
        } finally {
            try { in.reset(); } catch (Exception e) { } // chuyển con trỏ về đầu stream.
            try { cloneIn.close(); } catch (Exception e) { }
        }
    }
    /**
     * Hàm xử lí việc kiểm tra dữ liệu đầu vào trước khi Upload lên server.
     * @throws FileTransferUploadException 
     */
    protected abstract void checkUploadparam() throws FileTransferUploadException;
    /**
     * Hàm xử lí upload theo inputstream.
     * @param in
     * @throws FileTransferUploadException 
     */
    protected abstract void executeUpload(InputStream in) throws FileTransferUploadException;

    
    /***
     * Delete theo stream.
     * @param in
     * @throws FileTransferUploadException 
     */
    public void delete() throws FileTransferUploadException {
        checkDeleteparam();
        executeDelete();
    }
    /**
     * Hàm xử lí việc kiểm tra dữ liệu đầu vào trước khi Delete.
     * @throws FileTransferUploadException 
     */
    protected abstract void checkDeleteparam() throws FileTransferUploadException;
    /**
     * Hàm xử lí delete theo inputstream.
     * @param in
     * @throws FileTransferUploadException 
     */
    protected abstract void executeDelete() throws FileTransferUploadException;
}
