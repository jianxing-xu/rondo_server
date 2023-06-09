package cn.xu.rondo.response;


import cn.xu.rondo.enums.EE;
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

    public static Response<String> success() {
        return new Response<>(1000, "success");
    }

    public static Response<String> success(String msg) {
        return new Response<>(1000, msg);
    }

    // 成操作的提示消息
    public static Response<String> successTip(String tipMsg) {
        return new Response<>(1001, tipMsg);
    }

    public static Response<String> errorMsg(String msg) {
        return new Response<>(5005, msg);
    }


    public Response(T data) {
        this.code = EE.SUCCESS.getCode();
        this.msg = EE.SUCCESS.getMsg();
        this.data = data;
    }

    public Response(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}