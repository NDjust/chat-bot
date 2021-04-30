package me.nathan.oauthclient.model.dto.response.api;

import lombok.Builder;
import lombok.Data;

@Data
public class DefaultResponse<T> {

    private int code;

    private String message;

    private T data;

    @Builder
    public DefaultResponse(T data, int code, String message) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static<T> DefaultResponse<T> response(int code, String message) {
        return response(null, code, message);
    }

    public static<T> DefaultResponse<T> response(final T data, int code, String message) {
        return DefaultResponse.<T>builder()
                .code(code)
                .data(data)
                .message(message)
                .build();
    }
}
