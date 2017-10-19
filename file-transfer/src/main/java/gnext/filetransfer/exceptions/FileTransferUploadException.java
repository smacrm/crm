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
public class FileTransferUploadException extends Exception {
    private static final long serialVersionUID = 4808645677936773910L;
    public FileTransferUploadException() {
        super("Can not upload file to server.");
    }
    
    public FileTransferUploadException(String msg) {
        super(msg);
    }
    
    public FileTransferUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileTransferUploadException(Throwable cause) {
        super(cause);
    }
}
