package com.roomsync.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.roomsync.service.DashboardService;

@Controller
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/")
	public String dashboard(Model model) {
		model.addAttribute("pageTitle", "Dashboard");
		model.addAttribute("activePage", "dashboard");
		model.addAttribute("view", dashboardService.build());
		return "dashboard";
	}
}
