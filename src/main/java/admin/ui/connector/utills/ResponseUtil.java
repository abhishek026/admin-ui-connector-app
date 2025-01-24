package admin.ui.connector.utills;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import admin.ui.connector.exception.SuccessResponse;

public interface ResponseUtil {
	public static <T> ResponseEntity<SuccessResponse<T>> buildResponse(HttpStatus status, String message, T data) {
		SuccessResponse<T> customResponse = new SuccessResponse<T>(status.value(), message, data);
		return new ResponseEntity<>(customResponse, status);
	}

	public static <T> ResponseEntity<SuccessResponse<T>> buildResponse(String message, T data) {
		SuccessResponse<T> customResponse = new SuccessResponse<T>(data, message);
		return new ResponseEntity<>(customResponse, HttpStatus.OK);
	}
	public static <T> ResponseEntity<SuccessResponse<T>> buildResponse(T data) {
		SuccessResponse<T> customResponse = new SuccessResponse<T>(data);
		return new ResponseEntity<>(customResponse, HttpStatus.OK);
	}
	public static <T> ResponseEntity<SuccessResponse<T>> buildResponse(HttpStatus status,T data) {
		SuccessResponse<T> customResponse = new SuccessResponse<T>(status.value(), data);
		return new ResponseEntity<>(customResponse, status);
	}
}
