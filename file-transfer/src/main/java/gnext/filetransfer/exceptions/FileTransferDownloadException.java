/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.exceptions;

/**
 *
 * @author daind
 */
public class FileTransferDownloadException extends Exception {
    private static final long serialVersionUID = 2003136081757890610L;
    public FileTransferDownloadException() {
        super("Can not upload file to server.");
    }
    
    public FileTransferDownloadException(String msg) {
        super(msg);
    }
    
    public FileTransferDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileTransferDownloadException(Throwable cause) {
        super(cause);
    }
}
