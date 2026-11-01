function openRoommateModal(id, name, phone) {
	document.getElementById("roommateModalTitle").textContent = id ? "Edit roommate" : "Add roommate";
	document.getElementById("roommateId").value = id || "";
	document.getElementById("roommateName").value = name || "";
	document.getElementById("roommatePhone").value = phone || "";
	const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById("roommateModal"));
	modal.show();
}

function openPaymentModal(expenseId, roommateId, expenseName, roommateName, remaining) {
	document.getElementById("paymentExpenseId").value = expenseId;
	document.getElementById("paymentRoommateId").value = roommateId;
	document.getElementById("paymentExpenseName").value = expenseName;
	document.getElementById("paymentRoommateName").value = roommateName;
	document.getElementById("paymentAmount").value = remaining;
	document.getElementById("paymentAmount").max = remaining;
	document.getElementById("paymentHint").textContent = "Remaining amount: ₹" + Number(remaining).toFixed(2);
	document.getElementById("paymentDate").value = new Date().toISOString().slice(0, 10);
	bootstrap.Modal.getOrCreateInstance(document.getElementById("paymentModal")).show();
}

function toggleCustomSplit() {
	const method = document.getElementById("splittingMethod");
	const box = document.getElementById("customSplitBox");
	if (!method || !box) {
		return;
	}
	box.style.display = method.value === "CUSTOM" ? "block" : "none";
}

function showSplitPreview() {
	const preview = document.getElementById("splitPreview");
	const totalInput = document.getElementById("totalAmount");
	const method = document.getElementById("splittingMethod");
	if (!preview || !totalInput || !method || typeof roommates === "undefined") {
		return;
	}
	const total = Number(totalInput.value);
	if (!total || total <= 0) {
		preview.innerHTML = "<strong>Enter a valid total amount first.</strong>";
		return;
	}
	if (method.value === "EQUAL") {
		const count = roommates.length;
		const share = Math.floor((total * 100) / count) / 100;
		let assigned = 0;
		let html = "<h3>Equal split preview</h3>";
		roommates.forEach((roommate, index) => {
			const amount = index === 0 ? Number((total - share * (count - 1)).toFixed(2)) : share;
			assigned += amount;
			html += `<div class="d-flex justify-content-between py-1"><span>${roommate.name}</span><strong>₹${amount.toFixed(2)}</strong></div>`;
		});
		preview.innerHTML = html;
		return;
	}
	let sum = 0;
	let html = "<h3>Custom split preview</h3>";
	roommates.forEach((roommate) => {
		const input = document.querySelector(`[name="customAmount_${roommate.id}"]`);
		const amount = Number(input && input.value ? input.value : 0);
		sum += amount;
		html += `<div class="d-flex justify-content-between py-1"><span>${roommate.name}</span><strong>₹${amount.toFixed(2)}</strong></div>`;
	});
	const ok = Math.abs(sum - total) < 0.001;
	html += `<div class="mt-2 ${ok ? "text-success" : "text-danger"}">Entered total: ₹${sum.toFixed(2)} / ₹${total.toFixed(2)}</div>`;
	preview.innerHTML = html;
}

document.addEventListener("DOMContentLoaded", () => {
	toggleCustomSplit();
	const method = document.getElementById("splittingMethod");
	if (method) {
		method.addEventListener("change", toggleCustomSplit);
	}
	const previewBtn = document.getElementById("previewBtn");
	if (previewBtn) {
		previewBtn.addEventListener("click", showSplitPreview);
	}
});
