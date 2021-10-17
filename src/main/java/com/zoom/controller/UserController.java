package com.zoom.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import com.zoom.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import io.openvidu.java.client.OpenViduRole;

@Controller
public class UserController {

	public class MyUser {

		String name;
		String pass;
		OpenViduRole role;

		public MyUser(String name, String pass, OpenViduRole role) {
			this.name = name;
			this.pass = pass;
			this.role = role;
		}
	}

	public static Map<String, MyUser> users = new ConcurrentHashMap<>();

	public UserController() {
		users.put("publisher1", new MyUser("publisher1", "pass", OpenViduRole.PUBLISHER));
		users.put("publisher2", new MyUser("publisher2", "pass", OpenViduRole.PUBLISHER));
		users.put("subscriber", new MyUser("subscriber", "pass", OpenViduRole.SUBSCRIBER));
	}

	@RequestMapping(value = "/")
	public String logout(HttpSession httpSession) {
		if (checkUserLogged(httpSession)) {
			return "redirect:/dashboard";
		} else {
			httpSession.invalidate();
			return "index";
		}
	}

	@RequestMapping(value = "/dashboard", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(@RequestParam(name = "user", required = false) String user,
			@RequestParam(name = "pass", required = false) String pass, Model model, HttpSession httpSession) {

		// Check if the user is already logged in
		String userName = (String) httpSession.getAttribute("loggedUser");
		if (userName != null) {
			// User is already logged. Immediately return dashboard
			model.addAttribute("username", userName);
			return "dashboard";
		}

		// User wasn't logged and wants to
		if (login(user, pass)) { // Correct user-pass

			// Validate session and return OK
			// Value stored in HttpSession allows us to identify the user in future requests
			httpSession.setAttribute("loggedUser", user);
			model.addAttribute("username", user);

			// Return dashboard.html template
			return "dashboard";

		} else { // Wrong user-pass
			// Invalidate session and redirect to index.html
			httpSession.invalidate();
			return "redirect:/";
		}
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String logout(Model model, HttpSession httpSession) {
		httpSession.invalidate();
		return "redirect:/";
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user",new User());
		return "registrationForm";
	}

	@PostMapping("/register")
	public void register(@ModelAttribute("user")User user){

	}


	private boolean login(String user, String pass) {
		return (user != null && pass != null && users.containsKey(user) && users.get(user).pass.equals(pass));
	}

	private boolean checkUserLogged(HttpSession httpSession) {
		return !(httpSession == null || httpSession.getAttribute("loggedUser") == null);
	}
}