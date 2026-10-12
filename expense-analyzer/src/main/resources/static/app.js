// ===== Settings =====
const API = "/api";
const CURRENCY = "₹";

// ===== Helpers =====
const $ = (id) => document.getElementById(id);
const money = (n) => CURRENCY + Number(n || 0).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
// Use the browser's LOCAL date (toISOString would give the UTC date, which can be "yesterday")
const pad = (n) => String(n).padStart(2, "0");
const currentMonth = () => { const d = new Date(); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`; };
const today = () => { const d = new Date(); return `${currentMonth()}-${pad(d.getDate())}`; };

// Calls the REST API and returns JSON (or throws an Error with the server's message)
async function api(path, options = {}) {
  const response = await fetch(API + path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (response.status === 204) return null;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const details = data.errors ? " " + Object.values(data.errors).join(", ") : "";
    throw new Error((data.message || "Request failed") + details);
  }
  return data;
}

// ===== Dashboard =====
let categoryChart, incomeExpenseChart, trendChart;

async function loadDashboard() {
  const month = $("monthPicker").value;
  const d = await api(`/dashboard?month=${month}`);

  $("totalIncome").textContent = money(d.totalIncome);
  $("totalExpenses").textContent = money(d.totalExpenses);
  $("balance").textContent = money(d.remainingBalance);
  $("balance").className = Number(d.remainingBalance) < 0 ? "red" : "green";
  $("budgetAmount").textContent = d.budgetSet ? money(d.monthlyBudget) : "Not set";
  $("budgetPercent").textContent = d.budgetSet ? d.budgetUsedPercent + "%" : "-";
  $("topCategory").textContent = d.highestSpendingCategory
    ? `${d.highestSpendingCategory} (${money(d.highestSpendingAmount)})` : "No expenses";
  $("avgDaily").textContent = money(d.averageDailyExpense);
  $("budgetInput").value = d.budgetSet ? d.monthlyBudget : "";

  // progress bar + alert banner
  const bar = $("budgetBar");
  bar.style.width = Math.min(Number(d.budgetUsedPercent), 100) + "%";
  bar.className = "bar-fill " + (d.budgetStatus === "EXCEEDED" ? "exceeded" : d.budgetStatus === "WARNING" ? "warning" : "");
  const alertBox = $("alertBox");
  const level = { EXCEEDED: "exceeded", WARNING: "warning", OK: "ok", NO_BUDGET: "info" }[d.budgetStatus];
  const icon = { EXCEEDED: "🚨 ", WARNING: "⚠️ ", OK: "✅ ", NO_BUDGET: "ℹ️ " }[d.budgetStatus];
  alertBox.className = "alert " + level;
  alertBox.textContent = icon + d.budgetMessage;

  drawCharts(d);
}

function drawCharts(d) {
  const palette = ["#4f46e5", "#16a34a", "#f59e0b", "#ef4444", "#06b6d4", "#a855f7", "#ec4899", "#84cc16", "#64748b"];
  [categoryChart, incomeExpenseChart, trendChart].forEach((c) => c && c.destroy());

  categoryChart = new Chart($("categoryChart"), {
    type: "doughnut",
    data: { labels: d.categoryLabels, datasets: [{ data: d.categoryValues, backgroundColor: palette }] },
    options: { maintainAspectRatio: false, plugins: { legend: { position: "right" } } },
  });

  incomeExpenseChart = new Chart($("incomeExpenseChart"), {
    type: "bar",
    data: { labels: ["Income", "Expense"], datasets: [{ data: [d.totalIncome, d.totalExpenses], backgroundColor: ["#16a34a", "#dc2626"] }] },
    options: { maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } },
  });

  trendChart = new Chart($("trendChart"), {
    type: "line",
    data: { labels: d.trendLabels, datasets: [{ label: "Expenses", data: d.trendValues, borderColor: "#4f46e5", backgroundColor: "rgba(79,70,229,.15)", fill: true, tension: 0.3 }] },
    options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true } } },
  });
}

// ===== Budget =====
$("budgetForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await api(`/budgets/${$("monthPicker").value}`, { method: "PUT", body: JSON.stringify({ amount: $("budgetInput").value }) });
    await loadDashboard();
  } catch (err) { alert(err.message); }
});

$("deleteBudgetBtn").addEventListener("click", async () => {
  try {
    await api(`/budgets/${$("monthPicker").value}`, { method: "DELETE" });
    await loadDashboard();
  } catch (err) { alert(err.message); }
});

// ===== Transactions list =====
async function loadCategories() {
  const categories = await api("/categories");
  $("categoryList").innerHTML = categories.map((c) => `<option value="${escapeHtml(c)}">`).join("");
  const selected = $("filterCategory").value;
  $("filterCategory").innerHTML = '<option value="">All categories</option>' +
    categories.map((c) => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join("");
  $("filterCategory").value = selected;
}

async function loadTransactions() {
  const params = new URLSearchParams();
  const filters = { keyword: "searchBox", category: "filterCategory", type: "filterType", month: "filterMonth", sortBy: "sortBy", direction: "direction" };
  for (const [name, id] of Object.entries(filters)) if ($(id).value) params.set(name, $(id).value);

  const list = await api("/transactions?" + params.toString());
  const rows = $("transactionRows");
  rows.innerHTML = "";
  $("emptyMessage").classList.toggle("hidden", list.length > 0);

  list.forEach((t) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td class="c-date">${t.date}</td><td class="c-desc desc">${escapeHtml(t.description || "")}</td>
      <td class="c-cat">${escapeHtml(t.categoryName)}</td><td class="c-type"><span class="badge ${t.type}">${t.type}</span></td>
      <td class="c-amt num ${t.type === "INCOME" ? "green" : "red"}">${t.type === "INCOME" ? "+" : "-"}${money(t.amount)}</td>
      <td class="c-act"><button class="small" data-edit="${t.id}">Edit</button>
          <button class="small danger" data-delete="${t.id}">Delete</button></td>`;
    tr.querySelector("[data-edit]").addEventListener("click", () => startEdit(t));
    tr.querySelector("[data-delete]").addEventListener("click", () => removeTransaction(t.id));
    rows.appendChild(tr);
  });
}

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

// ===== Add / edit / delete =====
$("transactionForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  $("formError").textContent = "";
  const id = $("transactionId").value;
  const body = {
    amount: $("amount").value, type: $("type").value, categoryName: $("categoryName").value,
    date: $("date").value, description: $("description").value,
  };
  try {
    await api(id ? `/transactions/${id}` : "/transactions", { method: id ? "PUT" : "POST", body: JSON.stringify(body) });
    resetForm();
    await refreshAll();
  } catch (err) { $("formError").textContent = err.message; }
});

function startEdit(t) {
  $("transactionId").value = t.id;
  $("type").value = t.type;
  $("amount").value = t.amount;
  $("categoryName").value = t.categoryName;
  $("date").value = t.date;
  $("description").value = t.description || "";
  $("formTitle").textContent = "Edit Transaction #" + t.id;
  $("saveBtn").textContent = "Update";
  $("cancelEditBtn").classList.remove("hidden");
  $("transactionForm").scrollIntoView({ behavior: "smooth" });
}

function resetForm() {
  $("transactionForm").reset();
  $("transactionId").value = "";
  $("date").value = today();
  $("formTitle").textContent = "Add Transaction";
  $("saveBtn").textContent = "Add";
  $("cancelEditBtn").classList.add("hidden");
}
$("cancelEditBtn").addEventListener("click", resetForm);

async function removeTransaction(id) {
  if (!confirm("Delete this transaction?")) return;
  try { await api(`/transactions/${id}`, { method: "DELETE" }); await refreshAll(); }
  catch (err) { alert(err.message); }
}

// ===== Wiring =====
async function refreshAll() {
  await Promise.all([loadDashboard(), loadTransactions(), loadCategories()]);
}

["filterCategory", "filterType", "filterMonth", "sortBy", "direction"].forEach((id) => $(id).addEventListener("change", loadTransactions));
let searchTimer;
$("searchBox").addEventListener("input", () => { clearTimeout(searchTimer); searchTimer = setTimeout(loadTransactions, 300); });
$("clearFiltersBtn").addEventListener("click", () => {
  ["searchBox", "filterCategory", "filterType", "filterMonth"].forEach((id) => ($(id).value = ""));
  $("sortBy").value = "date"; $("direction").value = "desc";
  loadTransactions();
});
$("monthPicker").addEventListener("change", loadDashboard);
$("fab").addEventListener("click", () => { $("formPanel").scrollIntoView({ behavior: "smooth" }); $("amount").focus({ preventScroll: true }); });

$("monthPicker").value = currentMonth();
resetForm();
refreshAll().catch((err) => { $("alertBox").className = "alert exceeded"; $("alertBox").textContent = "Cannot reach the server: " + err.message; });
