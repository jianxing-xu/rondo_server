package cn.xu.roundo.enums;


import lombok.Getter;

/**
 * 枚举，必须实现 isValid 方法
 */
@Getter
public enum SwitchEnum {
    PUBLIC(0, "不是"),
    NO_PUBLIC(1, "是的");
    private Integer code;

    private String desc;

    private SwitchEnum() {
    }

    SwitchEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public boolean match(final String code) {
        return this.getCode().equals(code);
    }

    /**
     * 是否是合法的用户类型
     *
     * @param code 用户类型
     * @return 是否合法
     */
    public static boolean isValid(final String code) {
        for (SwitchEnum item : values()) {
            if (item.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isValid(final Integer code) {
        for (SwitchEnum item : values()) {
            if (item.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}