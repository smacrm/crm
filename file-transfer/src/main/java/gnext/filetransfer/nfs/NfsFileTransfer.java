/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.nfs;

import gnext.filetransfer.BaseFileTransfer;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.exceptions.FileTransferDownloadException;
import gnext.filetransfer.exceptions.FileTransferUploadException;
import java.io.InputStream;

/**
 *
 * @author daind
 * 
 * 1) NFS can run over the Internet, but not well.
 *      It's simply not optimized for the sub-par WAN links and possible traffic loss associated with it.
 * 2) NFS is far too unreliable (any WAN-based system is) to be used for an active operating system image.
 *      You'd probably see a lot of corrupt copies if you tried it.
 */
public class NfsFileTransfer extends BaseFileTransfer {

    public NfsFileTransfer(Parameter param) { super(param); }

    @Override
    protected void checkDownloadparam() throws FileTransferDownloadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected InputStream executeDowload(String pathToFile) throws FileTransferDownloadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void checkUploadparam() throws FileTransferUploadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void executeUpload(InputStream in) throws FileTransferUploadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void checkDeleteparam() throws FileTransferUploadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void executeDelete() throws FileTransferUploadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    protected void testConnection() throws FileTransferDownloadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void checkTestConnectionParam() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
