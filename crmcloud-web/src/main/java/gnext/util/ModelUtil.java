/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.model.BaseModel;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author daind
 */
public class ModelUtil {

    public static void up(int rowIndex, List<? extends BaseModel> models) {
        for (int i = 0; i < models.size(); i++) {
            BaseModel current = models.get(i);
            if (current.getRowNum() == rowIndex && i == 0) {
                return;
            } else if (current.getRowNum() == rowIndex && i > 0) {
                BaseModel before = models.get(i - 1);
                int brn = before.getRowNum();
                int crn = current.getRowNum();
                current.setRowNum(brn);
                before.setRowNum(crn);
                break;
            }
        }
    }

    public static void first(int rowIndex, List<? extends BaseModel> models) {
        for (int i = 0; i < models.size(); i++) {
            BaseModel current = models.get(i);
            if (current.getRowNum() == rowIndex && i == 0) {
                return;
            } else if (current.getRowNum() == rowIndex && i > 0) {
                BaseModel before = models.get(0);
                int brn = before.getRowNum() + 1;
                current.setRowNum(brn);
                break;
            }
        }
    }

    public static void down(int rowIndex, List<? extends BaseModel> models) {
        for (int i = 0; i < models.size(); i++) {
            BaseModel current = models.get(i);
            if (current.getRowNum() == rowIndex && i == models.size() - 1) {
                return;
            } else if (current.getRowNum() == rowIndex && i < models.size() - 1) {
                BaseModel after = models.get(i + 1);
                int arn = after.getRowNum();
                int crn = current.getRowNum();
                current.setRowNum(arn);
                after.setRowNum(crn);
                break;
            }
        }
    }

    public static void last(int rowIndex, List<? extends BaseModel> models) {
        for (int i = 0; i < models.size(); i++) {
            BaseModel current = models.get(i);
            if (current.getRowNum() == rowIndex && i == models.size() - 1) {
                return;
            } else if (current.getRowNum() == rowIndex && i < models.size() - 1) {
                BaseModel after = models.get(models.size() - 1);
                int arn = after.getRowNum();
                current.setRowNum(arn);
                models.forEach((model)->{
                    if(!model.equals(current)) model.setRowNum(model.getRowNum() + 1);
                });
                break;
            }
        }
    }
}
