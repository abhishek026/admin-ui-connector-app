package admin.ui.connector.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import admin.ui.connector.constant.APIConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuccessResponse<T> {
	private boolean success;
	private T result;
	private String message;
	private String timestamp;
	private int statusCode;

	public SuccessResponse(T data, String message) {
		this.success = true;
		this.result = data;
		this.message = message;
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.statusCode = APIConstants.SUCCESS_CODE;
	}

	public SuccessResponse(int statusCode, String message, T data) {
		this.success = true;
		this.result = data;
		this.message = message;
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.statusCode = statusCode;
	}
	public SuccessResponse(T data) {
		this.success = true;
		this.result = data;
		this.message = APIConstants.SUCCESS_MESSAGE;
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.statusCode = APIConstants.SUCCESS_CODE;
	}

	public SuccessResponse(int value, T data) {
		this.success = true;
		this.result = data;
		this.message = APIConstants.SUCCESS_MESSAGE;
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		this.statusCode = value;
		}
}
