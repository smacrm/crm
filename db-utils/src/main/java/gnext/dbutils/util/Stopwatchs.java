/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

/**
 *
 * @author daind
 */
public class Stopwatchs {
    private final long start;
    private final String name;
    
    public Stopwatchs(String name) {
        this.name = name;
        this.start = System.currentTimeMillis();
    }
    
    public double elapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    }

    public String getName() {
        return name;
    }
}
