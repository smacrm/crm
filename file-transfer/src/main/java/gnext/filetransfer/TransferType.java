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
public enum TransferType {
    FTP("FTP", true), TFTP("TFTP", false), NFS("NFS", false), HTTP("HTTP", false), NONE("NONE", false);
    private final String type;
    private final boolean inUse;
    TransferType(String type, boolean inUse) { this.type = type; this.inUse = inUse;}
    public String getType() { return type; }
    public boolean isInUse() { return inUse; }
    public static TransferType getTransferType(String type) {
        for (TransferType tt : TransferType.values()) {
            if(tt.getType().equalsIgnoreCase(type))
                return tt;
        }
        return null;
    }
}
