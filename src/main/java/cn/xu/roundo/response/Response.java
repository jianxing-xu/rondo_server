package cn.xu.roundo.response;


import cn.xu.roundo.enums.ErrorEnum;
import lombok.Getter;

@Getter
public class Response<T> {
    /**
     * 状态码，比如1000代表响应成功
     */
    private int code;

    /**
     * 响应信息，用来说明响应情况
     */
    private String msg;

    /**
     * 响应的具体数据
     */
    private T data;

    public static Response success() {
        return new Response(1000, "success");
    }

    public static Response success(String msg) {
        return new Response(1000, msg);
    }

    public static Response errorMsg(String msg) {
        return new Response(5005, msg);
    }


    public Response(T data) {
        this.code = ErrorEnum.SUCCESS.getCode();
        this.msg = ErrorEnum.SUCCESS.getMsg();
        this.data = data;
    }

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}