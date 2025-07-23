package com.example.obk.data.local.entity;

/** 扫描到的单个 TOTE（条码 + 数量，可编辑） */
public class ScannedTote {
    public final String code;   // 条码
    public int qty;             // 数量，默认 36，可修改

    public ScannedTote(String code) {
        this.code = code;
        this.qty  = 36;
    }

    /* 用于 DiffUtil 判断 */
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
