package start.spring.io.backend.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This controller handles errors that happen in the application.
 * Instead of showing a technical "Whitelabel Error Page", it catches errors
 * (like 404 Not Found) and shows our custom "error.html" page.
 */
@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    /**
     * Logic to determine what went wrong and prepare the error message.
     * It checks the status code (like 404, 500) and passes the relevant info
     * to the HTML view.
     */
    @GetMapping
    public String handleError(HttpServletRequest request, Model model) {
        // Retrieve error details from the request
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
            
            switch (statusCode) {
                case 403:
                    // Permission denied
                    model.addAttribute("message", "You do not have permission to access this resource.");
                    break;
                case 404:
                    // Page not found
                    model.addAttribute("path", path);
                    break;
                case 500:
                    // Server crashed
                    model.addAttribute("error", "Internal Server Error");
                    if (exception != null) {
                        model.addAttribute("message", exception.toString());
                    }
                    break;
                default:
                    // Any other error
                    if (message != null) {
                        model.addAttribute("message", message);
                    }
            }
        }

        return "error";
    }
}
