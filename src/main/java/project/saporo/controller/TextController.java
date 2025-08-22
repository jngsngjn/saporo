package project.saporo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Setter;

@RestController
public class TextController {

	private String message = "Hello World!";

	@GetMapping("/")
	public String getMessage() {
		return message;
	}

	@PostMapping("/api/change-text")
	public String changeText(String text) {
		message = text;
		return message;
	}
}