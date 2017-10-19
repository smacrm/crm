/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.quartz.listener.trigger;

import java.util.HashMap;
import java.util.Map;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

/**
 *
 * @author daind
 */
public class ManualTriggerListener implements org.quartz.TriggerListener {
    // Biến lưu trữ này sẽ thực hiện đánh dấu các trigger nào bị Misfired.
    // các trigger bị Misfired sẽ không được gọi để thực hiện.
//    public static final Map<Long, Long> TRIGGER_MISSFIRED = new HashMap<>();
    
    private final String name;
    public ManualTriggerListener(String name) { this.name = name; }
    @Override public String getName() { return name; }

    /**
     * Scheduler gọi phương thức triggerFired() khi trigger kết hợp cùng listener được khởi chạy
     * và phương thức execute() được gọi cho công việc.
     * Trong trường hợp là global triggerListener, phương thức này được gọi cho trigger.
     * @param trigger
     * @param context 
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        System.out.println("[ManualTriggerListener] trigger " + trigger.getKey().getName() + " call triggerFired");
    }

    /**
     * Scheduler gọi phương thức VetoJobExecution() khi mà trigger đã được khởi chạy và công việc đã sẵn sàng thực thi.
     * triggerListener đưa ra một cách để ngăn chặn việc thực thi công việc.
     * Nếu phương thức trả về true, công việc sẽ không được thực thi cho lần khởi chạy của trigger.
     * @param trigger
     * @param context
     * @return 
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        System.out.println("[ManualTriggerListener] trigger " + trigger.getKey().getName() + " call vetoJobExecution");
//        if(TRIGGER_MISSFIRED.containsKey(trigger.getStartTime().getTime())) {
//            System.out.println("[ManualTriggerListener] trigger " + trigger.getStartTime().getTime() + " will not executing...");
//            TRIGGER_MISSFIRED.remove(trigger.getStartTime().getTime());
//            return true;
//        }
        return false;
    }

    /**
     * Scheduler sẽ gọi phương thức triggerMisFired() khi trigger đã bị lỡ việc khởi chạy.
     * @param trigger 
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
//        TRIGGER_MISSFIRED.put(trigger.getStartTime().getTime(), System.currentTimeMillis());
        System.out.println("[ManualTriggerListener] trigger " + trigger.getKey().getName() + " will hold on for execute in future.");
    }

    /**
     * Scheduler sẽ gọi phương thức triggerComplete() khi mà trigger đã khởi chạy
     * và công việc đã kết thúc việc thực thi.
     * Điều đó không có nghĩa rằng trigger sẽ không được khởi chạy lại khi trigger hiện tại đã kết thúc.
     * Triggger vẫn có thể được khởi chạy trong tương lai.
     * @param trigger
     * @param context
     * @param triggerInstructionCode 
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        System.out.println("[ManualTriggerListener] trigger " + trigger.getKey().getName() + " call triggerComplete");
    }
}
