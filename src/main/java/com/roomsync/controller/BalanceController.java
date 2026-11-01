package com.roomsync.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.roomsync.service.BalanceService;

@Controller
public class BalanceController {

	private final BalanceService balanceService;

	public BalanceController(BalanceService balanceService) {
		this.balanceService = balanceService;
	}

	@GetMapping("/balances")
	public String balances(Model model) {
		model.addAttribute("pageTitle", "Balances");
		model.addAttribute("activePage", "balances");
		model.addAttribute("balances", balanceService.calculateAll());
		return "balances";
	}
}
