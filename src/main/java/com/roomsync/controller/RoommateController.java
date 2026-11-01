package com.roomsync.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.roomsync.model.Roommate;
import com.roomsync.service.RoommateService;

@Controller
@RequestMapping("/roommates")
public class RoommateController {

	private final RoommateService roommateService;

	public RoommateController(RoommateService roommateService) {
		this.roommateService = roommateService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("pageTitle", "Roommates");
		model.addAttribute("activePage", "roommates");
		model.addAttribute("roommates", roommateService.findAll());
		return "roommates";
	}

	@PostMapping
	public String save(@RequestParam(required = false) Long id, @RequestParam String name,
			@RequestParam(required = false) String phone, RedirectAttributes redirectAttributes) {
		try {
			Roommate roommate = new Roommate();
			roommate.setId(id);
			roommate.setName(name);
			roommate.setPhone(phone);
			roommateService.save(roommate);
			redirectAttributes.addFlashAttribute("success",
					id == null ? "Roommate added." : "Roommate updated.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/roommates";
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			roommateService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Roommate removed.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/roommates";
	}
}
