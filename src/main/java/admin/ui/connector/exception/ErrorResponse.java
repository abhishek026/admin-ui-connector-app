package admin.ui.connector.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ErrorResponse {
    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;
    private String timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.success = false;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
}
