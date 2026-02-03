package start.spring.io.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.UserService;

/**
 * This controller manages the User List.
 * It allows the Administrator to see all registered users, add new ones manually,
 * delete users, or edit their details.
 *
 * The @PreAuthorize("hasRole('admin')") annotation allows
 * only users with the 'admin' role can enter any method in this class.
 */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('admin')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the list of all users in the system.
     * We also send an empty "newUser" object to the view so the "Add User" modal/form
     * has somewhere to store data if the admin wants to create one.
     */
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("newUser", new User());
        model.addAttribute("currentPage", "users");
        return "user-list";
    }

    /**
     * Process the "Add User" form.
     * Takes the data entered by the admin and saves a new user to the database.
     */
    @PostMapping("/add")
    public String addUser(@ModelAttribute("newUser") User user) {
        userService.createUser(user);
        return "redirect:/users";
    }

    /**
     * Deletes a user based on their ID.
     * The ID comes from the URL (e.g., /users/delete/5).
     */
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    /**
     * Shows the Edit User form.
     * This reuses the "user-edit.html" view but configures it for an Admin.
     * Admins are allowed to change Roles, so we set "isAdminMode" to true.
     */
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        model.addAttribute("user", user);

        // Configuration specific to Admin mode
        model.addAttribute("postUrl", "/users/edit/" + id);
        model.addAttribute("isAdminMode", true); // Esto mostrará el selector de roles

        return "user-edit";
    }

    /**
     * Saves the changes made to a user profile.
     */
    @PostMapping("/edit/{id}")
    public String editUser(@PathVariable Integer id, @ModelAttribute("user") User user) {
        userService.updateUser(id, user);
        return "redirect:/users";
    }
}