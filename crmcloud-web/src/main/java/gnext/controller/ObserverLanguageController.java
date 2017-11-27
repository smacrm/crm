/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller;

import gnext.controller.issue.IssueLampController;
import gnext.controller.system.MaintenanceController;
import java.io.Serializable;

/**
 *
 * @author daind
 */
public interface ObserverLanguageController extends Serializable {
    /**
     * Sự kiện thay đổi ngôn ngữ cho việc nhập dữ liệu theo ngôn ngữ đó.
     * Một ví dụ về việc sử dụng tham khảo lớp {@link MaintenanceController} là lớp đóng vai trò Server.
     * kết hợp với lớp {@link IssueLampController} đóng vai trò là một client.
     */
    public void onChangeLanguage();
}
