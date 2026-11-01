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

import com.roomsync.model.Expense;
import com.roomsync.model.ExpenseCategory;
import com.roomsync.model.ExpenseShare;
import com.roomsync.model.SplitMethod;
import com.roomsync.service.ExpenseService;
import com.roomsync.service.RoommateService;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

	private final ExpenseService expenseService;
	private final RoommateService roommateService;

	public ExpenseController(ExpenseService expenseService, RoommateService roommateService) {
		this.expenseService = expenseService;
		this.roommateService = roommateService;
	}

	@GetMapping
	public String list(@RequestParam(required = false) String q,
			@RequestParam(required = false) ExpenseCategory category,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			Model model) {
		model.addAttribute("pageTitle", "Expenses");
		model.addAttribute("activePage", "expenses");
		model.addAttribute("expenses", expenseService.search(q, category, from, to));
		model.addAttribute("categories", ExpenseCategory.values());
		model.addAttribute("q", q);
		model.addAttribute("category", category);
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		return "expenses";
	}

	@GetMapping("/new")
	public String createForm(Model model, RedirectAttributes redirectAttributes) {
		if (roommateService.findAll().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Add at least one roommate before creating an expense.");
			return "redirect:/roommates";
		}
		model.addAttribute("pageTitle", "Add Expense");
		model.addAttribute("activePage", "expenses");
		Expense expense = new Expense();
		expense.setExpenseDate(java.time.LocalDate.now());
		expense.setSplittingMethod(SplitMethod.EQUAL);
		model.addAttribute("expense", expense);
		addFormOptions(model);
		return "expense-form";
	}

	@GetMapping("/{id}")
	public String details(@PathVariable Long id, Model model) {
		Expense expense = expenseService.getById(id);
		model.addAttribute("pageTitle", "Expense Details");
		model.addAttribute("activePage", "expenses");
		model.addAttribute("expense", expense);
		return "expense-details";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Expense expense = expenseService.getById(id);
		model.addAttribute("pageTitle", "Edit Expense");
		model.addAttribute("activePage", "expenses");
		model.addAttribute("expense", expense);
		Map<Long, BigDecimal> customAmounts = new HashMap<>();
		for (ExpenseShare share : expense.getShares()) {
			customAmounts.put(share.getRoommate().getId(), share.getAllocatedAmount());
		}
		model.addAttribute("customAmounts", customAmounts);
		addFormOptions(model);
		return "expense-form";
	}

	@PostMapping("/save")
	public String save(@RequestParam(required = false) Long id, @RequestParam String name,
			@RequestParam ExpenseCategory category, @RequestParam BigDecimal totalAmount,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expenseDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
			@RequestParam Long paidById, @RequestParam SplitMethod splittingMethod,
			@RequestParam(required = false) String description,
			@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
		try {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("Expense name is required.");
			}
			if (expenseDate == null) {
				throw new IllegalArgumentException("Expense date is required.");
			}
			Map<Long, BigDecimal> customAmounts = extractCustomAmounts(params);
			if (id == null) {
				Expense created = expenseService.create(name, category, totalAmount, expenseDate, dueDate, paidById,
						splittingMethod, description, customAmounts, null);
				redirectAttributes.addFlashAttribute("success", "Expense saved and split.");
				return "redirect:/expenses/" + created.getId();
			}
			expenseService.update(id, name, category, totalAmount, expenseDate, dueDate, paidById, splittingMethod,
					description, customAmounts);
			redirectAttributes.addFlashAttribute("success", "Expense updated.");
			return "redirect:/expenses/" + id;
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return id == null ? "redirect:/expenses/new" : "redirect:/expenses/" + id + "/edit";
		}
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		expenseService.delete(id);
		redirectAttributes.addFlashAttribute("success", "Expense deleted.");
		return "redirect:/expenses";
	}

	private void addFormOptions(Model model) {
		model.addAttribute("roommates", roommateService.findAll());
		model.addAttribute("categories", ExpenseCategory.values());
		model.addAttribute("splitMethods", SplitMethod.values());
	}

	private Map<Long, BigDecimal> extractCustomAmounts(Map<String, String> params) {
		Map<Long, BigDecimal> amounts = new HashMap<>();
		params.forEach((key, value) -> {
			if (key.startsWith("customAmount_") && value != null && !value.isBlank()) {
				Long roommateId = Long.valueOf(key.substring("customAmount_".length()));
				amounts.put(roommateId, new BigDecimal(value));
			}
		});
		return amounts;
	}
}
