/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.tftp;

import gnext.filetransfer.BaseFileTransfer;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.exceptions.FileTransferDownloadException;
import gnext.filetransfer.exceptions.FileTransferUploadException;
import java.io.InputStream;

/**
 *
 * @author daind
 */
public class TftpFileTransfer extends BaseFileTransfer {

    public TftpFileTransfer(Parameter param) {
        super(param);
    }

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
