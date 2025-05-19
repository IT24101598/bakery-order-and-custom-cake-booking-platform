package com.example.bakery.controller;


import com.example.bakery.model.User;
import com.example.bakery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Show register form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "register"; // renders register.jsp
    }

    // Handle registration
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String email,
            @RequestParam int age, // Adding age here
            @RequestParam String role,
            @RequestParam String gender,
            Model model
    ) throws IOException {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match!");
            return "register";
        }

        if (userService.isUsernameTaken(username)) {
            model.addAttribute("errorMessage", "Username already exists. Please choose another one.");
            return "register";
        }

        if (userService.isEmailTaken(email)) {
            model.addAttribute("errorMessage", "Email already exists. Please choose another one.");
            return "register";
        }

        User user = new User(username, password, email, age, role, gender); // Add age to the User constructor
        userService.addUser(user);
        return "redirect:/users/login?success=true";
    }

    @PutMapping("/{userId}")
    public String updateUser(@PathVariable int userId,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam int age, // Adding age here
                             @RequestParam String role,
                             @RequestParam String password,
                             @RequestParam(required = false) String gender) throws IOException {
        User updatedUser = new User(username, password, email, age, role, gender); // Add age to the User constructor
        updatedUser.setUserId(userId);
        userService.updateUser(userId, updatedUser);
        return "redirect:/users";
    }

    // Show login page
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // renders login.jsp
    }

    // Handle login
// Inside your login logic
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        User user = userService.validateLogin(email, password);

        if (user != null) {
            // Set the session attributes
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userRole", user.getRole());

            // Set cookies after successful login
            Cookie loginCookie = new Cookie("login", "true");
            loginCookie.setMaxAge(60 * 60); // 1 hour
            loginCookie.setPath("/");
            response.addCookie(loginCookie);

            Cookie roleCookie = new Cookie("role", user.getRole()); // ✅ correctly set here
            roleCookie.setPath("/");
            roleCookie.setMaxAge(60 * 60); // 1 hour
            response.addCookie(roleCookie);

            // Role-based redirect logic
            switch (user.getRole()) {
                case "admin":
                    return "redirect:/admin";

                case "customer":
                    return "redirect:/";

                default:
                    return "redirect:/";
            }
        } else {
            model.addAttribute("errorMessage", "Invalid username or password!");
            return "login";
        }
    }







    // Show logged-in user's role
    @GetMapping("/role")
    public String showUserRole(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            model.addAttribute("role", loggedInUser.getRole());
            return "userRole"; // Replace with the page where you want to display the role
        } else {
            model.addAttribute("errorMessage", "You are not logged in!");
            return "login"; // Redirect to login if not logged in
        }
    }

    // List users
    @GetMapping
    public String viewUsers(Model model) throws IOException {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "userManagement";
    }

    // Delete user
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable int userId) throws IOException {
        userService.deleteUser(userId);
        return "redirect:/users";
    }

    // Edit user form
    @GetMapping("/edit/{userId}")
    public String editUserForm(@PathVariable int userId, Model model) throws IOException {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.getUserId() == userId) {
                model.addAttribute("user", user);
                return "editUser";
            }
        }
        return "redirect:/users";
    }

    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate(); // Destroys the entire session
        return "redirect:/users/login?logout=true"; // Redirect to login page
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        // Get the logged-in user from the session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser != null) {
            // Add the logged-in user to the model to display on the profile page
            model.addAttribute("user", loggedInUser);
            return "profile"; // Return the profile view (profile.jsp or profile.html)
        } else {
            // If no user is logged in, redirect to login page
            model.addAttribute("errorMessage", "You must be logged in to view your profile.");
            return "login"; // Redirect to login page if not logged in
        }
    }


    // Show update form for logged-in user
    @GetMapping("/update")
    public String showUpdateForm(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            model.addAttribute("user", loggedInUser); // Populate the form with logged-in user's data
            return "updateProfile"; // Render updateProfile.jsp or a similar view
        } else {
            model.addAttribute("errorMessage", "You are not logged in!");
            return "login";
        }
    }

    // Handle user profile update
    @PostMapping("/update")
    public String updateUserProfile(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam int age,
            @RequestParam String role,
            @RequestParam(required = false) String gender,
            HttpSession session,
            Model model) throws IOException {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            // Update the user's profile data
            loggedInUser.setUsername(username);
            loggedInUser.setEmail(email);
            loggedInUser.setAge(age);
            loggedInUser.setRole(role);
            loggedInUser.setGender(gender);

            // Update user in the service
            userService.updateUser(loggedInUser.getUserId(), loggedInUser);

            // Update session data
            session.setAttribute("loggedInUser", loggedInUser);

            model.addAttribute("successMessage", "Profile updated successfully.");
            return "redirect:/users/profile"; // Redirect to profile page or dashboard
        } else {
            model.addAttribute("errorMessage", "You are not logged in!");
            return "login";
        }
    }


    // Handle user profile deletion
    @GetMapping("/delete")
    public String deleteUserProfile(HttpSession session) throws IOException {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            userService.deleteUser(loggedInUser.getUserId()); // Delete the user profile
            session.invalidate(); // Invalidate the session to log the user out
            return "redirect:/users/login?deleted=true"; // Redirect to login page with a message
        } else {
            return "redirect:/users/login"; // If not logged in, redirect to login page
        }
    }

}
