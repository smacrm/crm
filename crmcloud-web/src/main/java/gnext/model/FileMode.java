/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author HUONG
 */
public class FileMode implements Serializable { 

    @Getter @Setter
    private String mode;

    @Getter @Setter
    private String multiple;

    @Getter @Setter
    private String sizeLimit;

    @Getter @Setter
    private String fileLimit;

    @Getter @Setter
    private String allowTypes;
}
