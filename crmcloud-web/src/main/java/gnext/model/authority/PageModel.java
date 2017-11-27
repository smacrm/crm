/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.authority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.TreeNode;

public class PageModel extends AuthModel implements Serializable, Comparable<PageModel> {

    @Setter @Getter
    private List<PageModel.ActionModel> action;
    
    @Setter @Getter
    private TreeNode treeNode;

    public PageModel(String name) {
        this(0, name, new ArrayList<PageModel.ActionModel>());
    }

    public PageModel(int id, String name) {
        this(id, name, new ArrayList<PageModel.ActionModel>());
    }

    public PageModel(int id, String name, List<PageModel.ActionModel> action) {
        this.id = id;
        this.name = name;
        this.action = action;
    }

    public boolean hasAction(int index) {
        return index < action.size();
    }
    
    public PageModel.ActionModel getActionByLabel(String label){
        for(PageModel.ActionModel a : action){
            if( a.getName().equalsIgnoreCase(label) ) return a;
        }
        return null;
    }
    
    public boolean hasActionByLabel(String label){
        for(PageModel.ActionModel a : action){
            if( a.getName().equalsIgnoreCase(label) ) return true;
        }
        return false;
    }

    //Eclipse Generated hashCode and equals
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PageModel other = (PageModel) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (action == null) {
            if (other.action != null) {
                return false;
            }
        } else if (!action.equals(other.action)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(PageModel document) {
        return this.getName().compareTo(document.getName());
    }

    public class ActionModel extends AuthModel implements Serializable {

        
        @Setter @Getter
        private TreeNode treeNode;

        public ActionModel(int id) {
            this.id = id;
        }

        public ActionModel(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public ActionModel(int id, String name, boolean selected) {
            this.id = id;
            this.name = name;
            this.selected = selected;
        }

        public ActionModel(int id, String name, boolean selected, TreeNode treeNode) {
            this.id = id;
            this.name = name;
            this.selected = selected;
            this.treeNode = treeNode;
        }
        
    }
}
