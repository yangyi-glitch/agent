package lmy.agent.vo.resp;

public class Result<T> {
    private T data;
    private int code;
    private Long total;
    private String msg;

    public Result(T data, int code) {
        this.data = data;
        this.code = code;
    }

    public Result(T data, Long total, int code) {
        this.data = data;
        this.total = total;
        this.code = code;
    }

    public static <T> Result success(T data) {
        return new Result<>(data, 200);
    }

    public static <T> Result success(T data, Long total) {
        return new Result<>(data, total, 200);
    }

    public static <T> Result error(T data) {
        return new Result<>(data, 500);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
