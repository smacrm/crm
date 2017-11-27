package gnext.util;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author gnextadmin
 */
public class UploadedFileExt implements Serializable{

    private static final long serialVersionUID = 5547671104580704254L;
    
    @Getter @Setter
    public UploadedFile file;

    @Getter @Setter
    private Integer oldId;

    @Getter @Setter
    private String sizeView;
}
