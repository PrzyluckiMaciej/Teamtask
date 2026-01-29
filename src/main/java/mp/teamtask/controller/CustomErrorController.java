package mp.teamtask.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = 500;
        String errorMessage = "An unexpected error occurred";
        String errorType = "Internal Server Error";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        } else {
            errorMessage = getDefaultErrorMessage(statusCode);
        }

        errorType = getErrorType(statusCode);

        log.error("Error {}: {} - URI: {}", statusCode, errorMessage, uri);
        if (exception != null) {
            log.error("Exception: ", (Throwable) exception);
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("error", errorType);
        model.addAttribute("message", errorMessage);
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("path", uri != null ? uri.toString() : "Unknown");

        return "error/error";
    }

    private String getDefaultErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request - The server cannot process the request due to client error.";
            case 401 -> "Unauthorized - Authentication is required and has failed or not been provided.";
            case 403 -> "Forbidden - You don't have permission to access this resource.";
            case 404 -> "Page Not Found - The requested resource could not be found.";
            case 405 -> "Method Not Allowed - The request method is not supported for the requested resource.";
            case 500 -> "Internal Server Error - Something went wrong on the server.";
            case 502 -> "Bad Gateway - The server received an invalid response from the upstream server.";
            case 503 -> "Service Unavailable - The server is temporarily unable to handle the request.";
            case 504 -> "Gateway Timeout - The server did not receive a timely response from the upstream server.";
            default -> "An unexpected error occurred.";
        };
    }

    private String getErrorType(int statusCode) {
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        return httpStatus != null ? httpStatus.getReasonPhrase() : "Error";
    }
}
