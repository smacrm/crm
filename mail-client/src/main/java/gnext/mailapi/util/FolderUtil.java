/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.mailapi.datastructure.TreeFolder;
import gnext.dbutils.model.MailFolder;
import gnext.dbutils.util.Console;
import gnext.dbutils.util.StringUtil;
import gnext.mailapi.mail.Email;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author daind
 */
public class FolderUtil {

    /**
     * Hàm trả về mã code {@link InterfaceUtil.Folder} của Folder.
     * @param folder
     * @return 
     */
    public static String getFolderCode(String folder) {
        if (StringUtil.isEmpty(folder)) return MailFolder.DATA_MAIL_FOLDER_INBOX;

        switch (folder.toUpperCase()) {
            case InterfaceUtil.Folder.INBOX:
                return MailFolder.DATA_MAIL_FOLDER_INBOX;
            case InterfaceUtil.Folder.SENT:
                return MailFolder.DATA_MAIL_FOLDER_SENT;
            case InterfaceUtil.Folder.DRAFT:
                return MailFolder.DATA_MAIL_FOLDER_DRAFT;
            case InterfaceUtil.Folder.JUNK:
                return MailFolder.DATA_MAIL_FOLDER_JUNK;
            case InterfaceUtil.Folder.TRASH:
                return MailFolder.DATA_MAIL_FOLDER_TRASH;
        }
        return MailFolder.DATA_MAIL_FOLDER_INBOX;
    }

    /**
     * Mở Folder trên Mailserver để đọc nếu chưa được mở.
     * @param folder
     * @throws Exception 
     */
    public static void openFolder(Folder folder) throws Exception {
        // clear out any delete-marked messages - no need to process them....
//        folder.expunge();
        if (folder.isOpen() == false) folder.open(Folder.READ_ONLY);
    }

    /**
     * Chương trình sẽ đóng kết nối tới FOLDER những MESSAGE nào mark là xóa thì MAIL-SERVER tự xóa.
     * @param root 
     */
    public static void closeFolder(TreeFolder root) {
        if(root == null) return;
        if(root.getChildren() == null || root.getChildren().isEmpty()) return;
        
        for (TreeFolder tf : root.getChildren()) {
            if(tf.getFolder().isOpen()) {
                try { tf.getFolder().close(true); } catch (Exception e) { }
            }
            closeFolder(tf);
        }
    }
    
    /**
     * Hàm tạo 1 folder mới trên mailserver.
     * @param parent
     * @param folderName
     * @return
     * @throws Exception 
     */
    public static boolean createFolder(Folder parent, String folderName) throws Exception {
        Folder newFolder = parent.getFolder(folderName);
        return newFolder.create(Folder.HOLDS_MESSAGES);
    }

    /**
     * Hàm xử lí trả về danh sách folder trên mailserver theo dạng tree.
     * @param <M>
     * @param m
     * @return
     * @throws Exception 
     */
    public static <M extends Email> Map<String, Object> getTreeFolder(M m) throws Exception {
        Properties pros = m.getConfiguration();
        boolean auth = m.getAuth() != null ? m.getAuth() : false;
        
        Session session = SessionUtil.getSession(pros, auth, m.getUserName(), m.getPassword(), m.getDebug());
        Store store = session.getStore(m.getStoreProtocol());
        store.connect(m.getHost(), m.getUserName(), m.getPassword());
        
        Folder[] folders = store.getDefaultFolder().list();
        TreeFolder root = new TreeFolder(null, null);
        for (Folder f : folders) {
            TreeFolder tf = TreeFolder.addChild(f, root);
            _RecursiveTreeFolder(f, tf);
        }
        
        Map<String, Object> ret = new HashMap<>();
        ret.put("S", store);
        ret.put("TF", root);
        
        return ret;
    }

    /**
     * Hàm xử lí đệ qui để đọc 1 folder trên mailserver.
     * @param folder
     * @param treeFolder
     * @throws Exception 
     */
    private static void _RecursiveTreeFolder(Folder folder, TreeFolder treeFolder) throws Exception {
        Console.log("Folder on server: " + folder.getType() + " - " + folder.getName());
        
        Folder[] folders = null;
        try { folders = folder.list(); } catch (Exception e) { }
        if(folders == null || folders.length <= 0) return;
        
        for (Folder f : folders) {
            TreeFolder tf = TreeFolder.addChild(f, treeFolder);
            _RecursiveTreeFolder(f, tf);
        }
    }
}
