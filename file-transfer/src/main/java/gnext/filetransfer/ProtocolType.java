/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

/**
 *
 * @author daind
 */
public enum ProtocolType {
    SSL("SSL", true), TLS("TLS", false);
    private final String type;
    private final boolean inUse;
    ProtocolType(String type, boolean inUse) { this.type = type; this.inUse = inUse;}
    public String getType() { return type; }
    public boolean isInUse() { return inUse; }
    public static ProtocolType getProtocolType(String type) {
        for (ProtocolType tt : ProtocolType.values()) {
            if(tt.getType().equalsIgnoreCase(type))
                return tt;
        }
        return null;
    }
}
