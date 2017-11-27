/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.job;

/**
 *
 * @author daind
 */
public enum CommandType {
    STANDARD(1, "STANDARD"), READ_EMAIL(2, "MAIL(READ)");
    private final int id;
    private final String name;
    CommandType(int id, String name) { this.id = id; this.name = name;}
    public int getId() { return id; }
    public String getName() { return name; }
    public static CommandType getCommandType(int id) {
        for (CommandType tt : CommandType.values()) {
            if(tt.getId() == id) return tt;
        }
        return null;
    }
}
