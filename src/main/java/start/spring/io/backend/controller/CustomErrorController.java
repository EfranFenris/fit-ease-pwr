package start.spring.io.backend.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    @GetMapping
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
            
            switch (statusCode) {
                case 403:
                    model.addAttribute("message", "You do not have permission to access this resource.");
                    break;
                case 404:
                    model.addAttribute("path", path);
                    break;
                case 500:
                    model.addAttribute("error", "Internal Server Error");
                    if (exception != null) {
                        model.addAttribute("message", exception.toString());
                    }
                    break;
                default:
                    if (message != null) {
                        model.addAttribute("message", message);
                    }
            }
        }

        return "error";
    }
}
