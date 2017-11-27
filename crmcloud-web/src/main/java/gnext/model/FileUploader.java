package gnext.model;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class FileUploader implements Serializable {
    private static final long serialVersionUID = -690898391593349050L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploader.class);

    @Getter @Setter private InputStream inputStream;
    @Getter @Setter private byte[] streamBytes;

    public FileUploader() { init(); }
    private void init() { }

    public void load(UploadedFile logo) {
        if (logo != null) streamBytes = logo.getContents(); else reset();
    }

    public void load(String filePath) {
        try {
            streamBytes = IOUtils.toByteArray(new FileInputStream(filePath));
        } catch (Exception e) { reset(); }
    }

    public void reset() {
        streamBytes = null;
        inputStream = null;
    }

    public byte[] getBytes() {
        return streamBytes;
    }
    
    public boolean isHasImage() {
        return (streamBytes != null && streamBytes.length > 0) || (inputStream != null);
    }

    public StreamedContent getStreamedContent() {
        if ((streamBytes == null || streamBytes.length == 0) && (inputStream == null)) return new DefaultStreamedContent();
        if(streamBytes != null && streamBytes.length > 0) return new DefaultStreamedContent(new ByteArrayInputStream(streamBytes));
        if(inputStream != null) return new DefaultStreamedContent(inputStream);
        return new DefaultStreamedContent();
    }
}
