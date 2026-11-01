package com.roomsync.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.roomsync.model.PaymentMethod;
import com.roomsync.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("pageTitle", "Payments");
		model.addAttribute("activePage", "payments");
		model.addAttribute("pendingShares", paymentService.findPendingShares());
		model.addAttribute("payments", paymentService.findAll());
		model.addAttribute("methods", PaymentMethod.values());
		return "payments";
	}

	@PostMapping
	public String record(@RequestParam Long expenseId, @RequestParam Long roommateId, @RequestParam BigDecimal amount,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
			@RequestParam PaymentMethod paymentMethod, @RequestParam(required = false) String notes,
			RedirectAttributes redirectAttributes) {
		try {
			paymentService.record(expenseId, roommateId, amount, paymentDate, paymentMethod, notes);
			redirectAttributes.addFlashAttribute("success", "Payment recorded.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/payments";
	}
}
