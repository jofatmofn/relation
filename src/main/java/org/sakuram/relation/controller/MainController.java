package org.sakuram.relation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/reltree")
	public String main() {
		return "reltree";
	}
}
