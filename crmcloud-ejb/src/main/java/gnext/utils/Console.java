/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import java.io.OutputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 *
 * @author daind
 */
public final class Console {
    public static boolean showConsole = true;
    private Console() { }
    
    public static void log(StringBuilder message) {
        if(!showConsole) return;
        System.out.println(message);
    }
    
    public static void log(String message) {
        if(!showConsole) return;
        System.out.println(message);
    }
    
    public static void log(Exception e) {
        if(!showConsole) return;
        e.printStackTrace();
    }
    
    /**
     * Hàm thực hiện lệnh commandline lỗi sẽ throw nếu xảy ra.
     * @param cmd
     * @throws Exception 
     */
    public static void exec(String cmd) throws Exception {
        CommandLine cmdLine = CommandLine.parse(cmd);
        DefaultExecutor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
    }
    
    /**
     * Hàm thực hiện lệnh commandline lỗi sẽ throw nếu xảy ra.
     * @param cmd
     * @param outputStream
     * @throws Exception 
     */
    public static void exec(String cmd, OutputStream outputStream) throws Exception {
        CommandLine cmdLine = CommandLine.parse(cmd);
        DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler psh = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(psh);
        int exitValue = executor.execute(cmdLine);
    }
    
    /**
     * Hàm thực hiện lệnh commandline lỗi sẽ throw nếu xảy ra. Qúa thời gian timeOut sẽ tự động shutdown
     * @param cmd
     * @param timeOut - Thời gian tối đa để chạy câu lệnh
     * @throws Exception 
     */
    public static void exec(String cmd, Integer timeOut) throws Exception {
        CommandLine cmdLine = CommandLine.parse(cmd);
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchDog = new ExecuteWatchdog(timeOut);
        executor.setWatchdog(watchDog);
        int exitValue = executor.execute(cmdLine);
    }
    
    /**
     * Hàm kiểm tra kết nối với internet.
     * @return 
     */
    public static boolean checkInternet() {
        return ping("google.com");
    }
    
    /**
     * Hàm xử lí ping địa chỉ Ip.
     * @param ipOrHostname
     * @return 
     */
    public static boolean ping(final String ipOrHostname) {
        try {
            // send 1 packets and stop.
            String cmd = "ping -c 1 " + ipOrHostname;
            exec(cmd);
            return true;
        } catch (Exception e) {
            Console.log(e);
        }
        return false;
    }
    
    public static void main(String[] args) throws Exception {
//        apacheRun("ping google.com");
//        apacheRun("AcroRd32.exe /p /h ada");
//        apacheRun("ping vnext123.com");

//        List<String> a = run("AcroRd32.exe /p /h ada");
//        for(String s : a) System.out.println("gnext.util.Console.main()" + s);
        exec("sh /home/daind/workspace/vnext/sourcecode/web/crmcloud-web/src/main/resources/shellscript/split_db.sh /mnt/cfg/cfg.properties 1");
    }
}
