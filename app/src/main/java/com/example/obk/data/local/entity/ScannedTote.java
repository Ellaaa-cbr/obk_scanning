package com.example.obk.data.local.entity;

/** Each already scanned tote  */
public class ScannedTote {
    public final String code;   // QR code
    public int qty;             // count, 36, can be changed

    public ScannedTote(String code) {
        this.code = code;
        this.qty  = 36;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScannedTote)) return false;
        ScannedTote t = (ScannedTote) o;
        return qty == t.qty && code.equals(t.code);
    }

    @Override public int hashCode() {
        return 31 * code.hashCode() + qty;
    }
}
