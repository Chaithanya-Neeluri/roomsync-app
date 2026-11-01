package com.roomsync.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.roomsync.model.BillFrequency;
import com.roomsync.model.ExpenseCategory;
import com.roomsync.model.RecurringBill;
import com.roomsync.model.SplitMethod;
import com.roomsync.service.RecurringBillService;
import com.roomsync.service.RoommateService;

@Controller
@RequestMapping("/recurring")
public class RecurringBillController {

	private final RecurringBillService recurringBillService;
	private final RoommateService roommateService;

	public RecurringBillController(RecurringBillService recurringBillService, RoommateService roommateService) {
		this.recurringBillService = recurringBillService;
		this.roommateService = roommateService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("pageTitle", "Recurring Bills");
		model.addAttribute("activePage", "recurring");
		model.addAttribute("bills", recurringBillService.findAll());
		return "recurring";
	}

	@GetMapping("/new")
	public String createForm(Model model, RedirectAttributes redirectAttributes) {
		if (roommateService.findAll().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Add at least one roommate first.");
			return "redirect:/roommates";
		}
		model.addAttribute("pageTitle", "Add Recurring Bill");
		model.addAttribute("activePage", "recurring");
		RecurringBill bill = new RecurringBill();
		bill.setStartDate(java.time.LocalDate.now());
		bill.setNextDueDate(java.time.LocalDate.now());
		bill.setFrequency(BillFrequency.MONTHLY);
		bill.setSplittingMethod(SplitMethod.EQUAL);
		bill.setActive(true);
		model.addAttribute("bill", bill);
		addFormOptions(model);
		return "recurring-form";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("pageTitle", "Edit Recurring Bill");
		model.addAttribute("activePage", "recurring");
		model.addAttribute("bill", recurringBillService.getById(id));
		addFormOptions(model);
		return "recurring-form";
	}

	@PostMapping("/save")
	public String save(@RequestParam(required = false) Long id, @RequestParam String billName,
			@RequestParam ExpenseCategory category, @RequestParam BigDecimal amount,
			@RequestParam BillFrequency frequency,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextDueDate,
			@RequestParam SplitMethod splittingMethod, @RequestParam Long paidById,
			@RequestParam(defaultValue = "true") boolean active, @RequestParam Map<String, String> params,
			RedirectAttributes redirectAttributes) {
		try {
			Map<Long, BigDecimal> customAmounts = new HashMap<>();
			params.forEach((key, value) -> {
				if (key.startsWith("customAmount_") && value != null && !value.isBlank()) {
					customAmounts.put(Long.valueOf(key.substring("customAmount_".length())), new BigDecimal(value));
				}
			});
			recurringBillService.save(id, billName, category, amount, frequency, startDate, nextDueDate, splittingMethod,
					paidById, active, customAmounts);
			redirectAttributes.addFlashAttribute("success", id == null ? "Recurring bill created." : "Recurring bill updated.");
			return "redirect:/recurring";
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return id == null ? "redirect:/recurring/new" : "redirect:/recurring/" + id + "/edit";
		}
	}

	@PostMapping("/{id}/pause")
	public String pause(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		recurringBillService.toggleActive(id);
		redirectAttributes.addFlashAttribute("success", "Recurring bill status updated.");
		return "redirect:/recurring";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		recurringBillService.delete(id);
		redirectAttributes.addFlashAttribute("success", "Recurring bill deleted.");
		return "redirect:/recurring";
	}

	private void addFormOptions(Model model) {
		model.addAttribute("roommates", roommateService.findAll());
		model.addAttribute("categories", ExpenseCategory.values());
		model.addAttribute("frequencies", BillFrequency.values());
		model.addAttribute("splitMethods", SplitMethod.values());
	}
}
