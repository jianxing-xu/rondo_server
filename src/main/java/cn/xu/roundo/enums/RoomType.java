package cn.xu.roundo.enums;

import lombok.Getter;

/**
 * 枚举，必须实现 isValidName 方法
 */
@Getter
public enum RoomType {
    A(0, "房间类型0"),
    B(1, "房间类型1"),
    C(4, "房间类型4");
    private Integer code;

    private String desc;

    RoomType() {
    }

    RoomType(Integer code, String desc) {
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
        for (RoomType item : values()) {
            if (item.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(final Integer code) {
        for (RoomType item : values()) {
            if (item.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}