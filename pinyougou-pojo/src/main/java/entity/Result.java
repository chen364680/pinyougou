package entity;

import java.io.Serializable;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package entity
 * @company www.itheima.com
 */
public class Result implements Serializable{
    private String message;
    private boolean success;

    public Result() {
    }

    public Result(boolean success,String message) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
