/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

import lombok.Getter;

/**
 *
 * @author daind
 */
public class DeleteParameter extends Parameter {
    
    // tham số đường dẫn file hoặc thư mục trên server.
    @Getter private String deletePath;
    // tham số xác định là folder hay file
    @Getter private boolean folder;
    
    public static DeleteParameter getInstance(TransferType type) {
        DeleteParameter parameter = new DeleteParameter();
        parameter.type(type);
        return parameter;
    }
    
    public DeleteParameter deletePath(String deletePath) {
        this.deletePath = deletePath;
        return this;
    }
    
    public DeleteParameter folder(boolean folder) {
        this.folder = folder;
        return this;
    }
}
