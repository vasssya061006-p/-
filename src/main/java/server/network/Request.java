package server.network;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String command;
    private Object data;
    private Integer userId;

    public Request(String command, Object data) {
        this.command = command;
        this.data = data;
        this.userId = null;
    }

    public String getCommand() { return command; }
    public Object getData() { return data; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}