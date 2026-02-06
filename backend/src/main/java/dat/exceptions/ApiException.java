package dat.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiException extends Exception{

  private final int statusCode;
  private final String timestamp;

  public ApiException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
    this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getTimestamp() {
    return timestamp;
  }

  // Skjul unødvendige detaljer i JSON-responsen
  @JsonIgnore
  @Override
  public StackTraceElement[] getStackTrace() {
    return super.getStackTrace();
  }

  @JsonIgnore
  @Override
  public Throwable getCause() {
    return super.getCause();
  }

  @JsonIgnore
  @Override
  public String getLocalizedMessage() {
    return super.getLocalizedMessage();
  }

}
