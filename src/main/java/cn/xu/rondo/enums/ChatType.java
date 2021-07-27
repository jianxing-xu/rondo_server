package cn.xu.rondo.enums;


import lombok.Getter;

/**
 * 枚举，必须实现 isValid 方法
 */
@Getter
public enum ChatType {
    PUBLIC(ChatType.TEXT, "文本消息"),
    NO_PUBLIC(ChatType.IMG, "图片消息");

    private String type;
    private String desc;

    public static final String TEXT = "text";
    public static final String IMG = "img";


    ChatType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public boolean match(final String type) {
        return this.getType().equals(type);
    }

    /**
     * 是否是合法的用户类型
     *
     * @param type 消息类型
     * @return 是否合法
     */
    public static boolean isValid(final String type) {
        for (ChatType item : values()) {
            if (item.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}