package com.example.hubaoyu.threebody.model;

/**
 * SquatModelResponse
 *
 * @author huangchen
 */
public class SquatModelResponse {
    /**
     * 操作是否成功
     */
    private boolean ok;

    /**
     * 错误码
     */
    private int errorCode;

    /**
     * 中文的错误信息
     */
    private String text;

    /**
     * 错误信息，优先使用 {@link #text}
     */
    private String errorMessage;

    /**
     * 版本
     */
    private String version;

    private SquatModel data;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SquatModel getData() {
        return data;
    }

    public void setData(SquatModel data) {
        this.data = data;
    }

}