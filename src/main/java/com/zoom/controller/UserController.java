package com.zoom.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import com.zoom.model.User;
import com.zoom.service.UserService;
import com.zoom.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import io.openvidu.java.client.OpenViduRole;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

//	@RequestMapping(value = "/")
//	public String logout(HttpSession httpSession) {
//		if (checkUserLogged(httpSession)) {
//			return "redirect:/dashboard";
//		} else {
//			httpSession.invalidate();
//			return "index";
//		}
//	}

	@GetMapping("/login")
	public String displayLoginForm() {
		System.out.println("inside login");
		return "login";
	}

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetailsImpl user,
							HttpSession httpSession,
							Model model){
		httpSession.setAttribute("loggedUser",user.getName());
		model.addAttribute("username",user.getName());
		return "dashboard";
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user",new User());
		System.out.println("user Controller");
		return "registrationForm";
	}

	@PostMapping("/register")
	public String register(@ModelAttribute("user")User user,
						 RedirectAttributes redirectAttributes){
		boolean checkRegistered = userService.register(user);
		if (checkRegistered) {
			redirectAttributes.addFlashAttribute("success", "!!! Registered Successfully !!!");
		} else {
			redirectAttributes.addFlashAttribute("error", "!!! Already Registered !!!");
		}
		return "redirect:/";
	}

	private boolean checkUserLogged(HttpSession httpSession) {
		return !(httpSession == null || httpSession.getAttribute("loggedUser") == null);
	}
}