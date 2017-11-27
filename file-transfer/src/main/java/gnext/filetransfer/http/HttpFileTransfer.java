/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer.http;

import com.mysql.jdbc.StringUtils;
import gnext.filetransfer.BaseFileTransfer;
import gnext.filetransfer.HttpParameter;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.authenticate.BasicAuthenticate;
import gnext.filetransfer.exceptions.FileTransferDownloadException;
import gnext.filetransfer.exceptions.FileTransferUploadException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class HttpFileTransfer extends BaseFileTransfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFileTransfer.class);
    public HttpFileTransfer(Parameter param) { super(param); }

    private String getAuthEncoded() {
        HttpParameter param = (HttpParameter) getParam();
        if(param.isBasicauthenticate()) {
            BasicAuthenticate ba = param.getBasicauthenticate();
            String originAuth = ba.getUsername() + ":" + ba.getPassword();
            byte[] bytesEncoded = Base64.encodeBase64(originAuth.getBytes());
            return new String(bytesEncoded);
        }
        return null;
    }
    
    //*****************************************************************************************************************
    //********************************************** UPLOAD API *******************************************************
    //*****************************************************************************************************************
    @Override
    protected void checkUploadparam() throws FileTransferUploadException {
        HttpParameter param = (HttpParameter) getParam();
        if(StringUtils.isNullOrEmpty(param.getUrl())) throw new FileTransferUploadException("URL is must to not empty.");
    }

    @Override
    protected void executeUpload(InputStream in) throws FileTransferUploadException {
        HttpParameter param = (HttpParameter) getParam();
        String url = param.getUrl();
        List<BaseFileTransfer> callback = param.getCallback();
        executeUpload(url, callback);
    }

    private void executeUpload(String url, List<BaseFileTransfer> bfts) throws FileTransferUploadException {
        InputStream in = null;
        try {
            URL _url = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) _url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            
            String authEncoded = getAuthEncoded();
            if(authEncoded != null) connection.setRequestProperty("Authorization", "Basic " + authEncoded);
            
            in = connection.getInputStream();
            if(bfts != null && !bfts.isEmpty()) for(BaseFileTransfer bft : bfts) bft.upload(in);
        } catch(Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FileTransferUploadException(e);
        } finally {
            try { in.close(); } catch (Exception e) { }
        }
    }

    //*****************************************************************************************************************
    //******************************************** DOWNLOAD API *******************************************************
    //*****************************************************************************************************************
    @Override
    protected void checkDownloadparam() throws FileTransferDownloadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected InputStream executeDowload(String pathToFile) throws FileTransferDownloadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //*****************************************************************************************************************
    //********************************************** DELETE API *******************************************************
    //*****************************************************************************************************************  
    @Override
    protected void checkDeleteparam() throws FileTransferUploadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void executeDelete() throws FileTransferUploadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    //*****************************************************************************************************************
    //******************************************** TEST CONNECTION API ************************************************
    //*****************************************************************************************************************   
    @Override
    protected void testConnection() throws FileTransferDownloadException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void checkTestConnectionParam() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
