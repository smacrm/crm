/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

import gnext.filetransfer.ftp.FtpFileTransfer;
import gnext.filetransfer.http.HttpFileTransfer;
import gnext.filetransfer.nfs.NfsFileTransfer;
import gnext.filetransfer.tftp.TftpFileTransfer;

/**
 *
 * @author daind
 */
public class FileTransferFactory {
    /***
     * Hàm xử lí trả về BaseFileTransfer tùy theo {@link TransferType} trong param.
     * @param param Tham số đầu vào chứa type{@link TransferType}.
     * @return
     * @throws Exception Nếu không có type nào phù hợp.
     */
    public static BaseFileTransfer getTransfer(Parameter param) throws Exception {
        if(param == null || param.getType() == null) throw new IllegalArgumentException("Unknow the type of parameter.");
        switch (param.getType()) {
            case FTP:  return new FtpFileTransfer(param);
            case NFS:  return new NfsFileTransfer(param);
            case TFTP: return new TftpFileTransfer(param);
            case HTTP: return new HttpFileTransfer(param);
            default:    throw new IllegalArgumentException("Unknow the type of parameter.");                
        }
    }
}
