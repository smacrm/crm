/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.datastructure;

import gnext.dbutils.util.Console;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Folder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class TreeFolder {

    @Getter @Setter private Folder folder;
    @Getter @Setter private List<TreeFolder> children = new ArrayList<>();
    @Getter @Setter private TreeFolder parent;

    public TreeFolder(Folder folder, TreeFolder parent) {
        this.folder = folder;
        this.parent = parent;
    }

    /**
     * Thêm 1 node vào tree.
     * @param folder
     * @param parent
     * @return 
     */
    public static TreeFolder addChild(Folder folder, TreeFolder parent) {
        TreeFolder node = new TreeFolder(folder, parent);
        parent.getChildren().add(node);

        return node;
    }

    /**
     * Hàm xử lí in theo dạng tree.
     * @param root
     * @param cs 
     */
    public static void export(TreeFolder root, int cs) {
        for (TreeFolder tf : root.getChildren()) {
            
            for (int i = 0; i < cs; i++)
                System.out.print("-");
            
            Console.log(tf.getFolder().getFullName());
            export(tf, cs + 1);
        }
    }
}
