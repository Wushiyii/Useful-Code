package com.wushiyii.beans;

public class CodeAndDesc<T> {


    private T code;
    private String desc;


    public CodeAndDesc() {}

    public CodeAndDesc(T code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static <T> CodeAndDesc<T> of(T code, String desc) {
        return new CodeAndDesc<>(code, desc);
    }


    public T getCode() {
        return code;
    }

    public void setCode(T code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    @Override
    public String toString() {
        return "CodeAndDesc{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }
}
