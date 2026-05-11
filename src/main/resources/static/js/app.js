// Global application state
let currentUser = null;
let editId = null;
let contacts = [];
let activeView = "none";
let resetToken = null;
let pendingRegistrationEmail = "";

// Validation rules
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegex = /^[0-9]{10}$/;

// Event listeners
document.getElementById("registerForm").addEventListener("submit", register);
document.getElementById("verifyRegistrationForm").addEventListener("submit", verifyRegistrationOtp);
document.getElementById("loginForm").addEventListener("submit", login);
document.getElementById("forgotPasswordForm").addEventListener("submit", forgotPassword);
document.getElementById("resetPasswordOtpForm").addEventListener("submit", resetPasswordWithOtp);
document.getElementById("resetPasswordForm").addEventListener("submit", resetPassword);
document.getElementById("contactForm").addEventListener("submit", saveContact);
document.getElementById("searchForm").addEventListener("submit", searchContacts);
document.getElementById("searchName").addEventListener("input", debounce(searchContacts, 300));

// Authentication view controls
function showLogin() {
    registerPage.style.display = "none";
    resetPasswordPage.style.display = "none";
    loginPage.style.display = "block";
    forgotPasswordForm.style.display = "none";
    resetPasswordOtpForm.style.display = "none";
    verifyRegistrationForm.style.display = "none";
}

function showRegister() {
    loginPage.style.display = "none";
    resetPasswordPage.style.display = "none";
    registerPage.style.display = "block";
    forgotPasswordForm.style.display = "none";
    resetPasswordOtpForm.style.display = "none";
}

function showLoginFromReset() {
    resetToken = null;
    window.history.replaceState({}, document.title, "/app.html");
    document.getElementById("resetPasswordForm").reset();
    showLogin();
}

function toggleForgotPassword() {
    const shouldShow = forgotPasswordForm.style.display === "none";
    forgotPasswordForm.style.display = shouldShow ? "block" : "none";
    resetPasswordOtpForm.style.display = shouldShow ? "block" : "none";
    if (shouldShow) {
        resetOtpEmail.value = lemail.value.trim();
    }
}

function showResetPassword() {
    registerPage.style.display = "none";
    loginPage.style.display = "none";
    resetPasswordPage.style.display = "block";
}

function showMessage(text, type = "success") {
    const target = homePage.style.display === "block" ? message : authMessage;
    target.className = `alert alert-${type}`;
    if (target === message) {
        target.classList.add("no-print");
    }
    target.textContent = text;
    target.classList.remove("d-none");
    setTimeout(() => target.classList.add("d-none"), 3000);
}

function validateEmail(value) {
    return emailRegex.test(value);
}

// User authentication
function register(event) {
    event.preventDefault();

    if (!rname.value.trim() || !validateEmail(remail.value) || rpass.value.length < 4) {
        alert("Enter name, valid email, and password with at least 4 characters.");
        return;
    }

    fetch("/api/auth/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            name: rname.value.trim(),
            email: remail.value.trim(),
            password: rpass.value
        })
    })
        .then(res => res.text())
        .then(data => {
            if (data.includes("OTP sent")) {
                pendingRegistrationEmail = remail.value.trim();
                verifyRegistrationForm.style.display = "block";
                showMessage(data);
                return;
            }
            alert(data);
        });
}

function verifyRegistrationOtp(event) {
    event.preventDefault();

    if (!pendingRegistrationEmail || !registrationOtp.value.trim()) {
        showMessage("Enter the OTP sent to your email.", "danger");
        return;
    }

    fetch("/api/auth/verify-registration", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            email: pendingRegistrationEmail,
            otp: registrationOtp.value.trim()
        })
    })
        .then(res => res.text())
        .then(text => {
            if (!text) {
                showMessage("Registration verification failed.", "danger");
                return;
            }

            if (text.startsWith("{")) {
                const data = JSON.parse(text);
                currentUser = data;
                sessionStorage.setItem("contactDiaryUser", JSON.stringify(data));
                authSection.style.display = "none";
                homePage.style.display = "block";
                userInfo.textContent = `Logged in as ${data.name} (${data.email})`;
                document.getElementById("registerForm").reset();
                document.getElementById("verifyRegistrationForm").reset();
                verifyRegistrationForm.style.display = "none";
                pendingRegistrationEmail = "";
                loadContacts(false);
                return;
            }

            showMessage(text, "danger");
        })
        .catch(() => showMessage("Registration verification failed.", "danger"));
}

function login(event) {
    event.preventDefault();

    if (!validateEmail(lemail.value) || !lpass.value) {
        alert("Enter a valid email and password.");
        return;
    }

    fetch("/api/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            email: lemail.value.trim(),
            password: lpass.value
        })
    })
        .then(res => res.text())
        .then(text => text ? JSON.parse(text) : null)
        .then(data => {
            if (!data || !data.id) {
                alert("Invalid login. Please register first or check your password.");
                return;
            }

            currentUser = data;
            sessionStorage.setItem("contactDiaryUser", JSON.stringify(data));
            authSection.style.display = "none";
            homePage.style.display = "block";
            userInfo.textContent = `Logged in as ${data.name} (${data.email})`;
            loadContacts(false);
        });
}

function forgotPassword(event) {
    event.preventDefault();

    const email = forgotEmail.value.trim();
    if (!validateEmail(email)) {
        showMessage("Enter a valid registered email.", "danger");
        return;
    }

    fetch("/api/auth/forgot-password", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email})
    })
        .then(async res => ({
            ok: res.ok,
            text: await res.text()
        }))
        .then(messageText => {
            resetOtpEmail.value = email;
            showMessage(messageText.text, messageText.ok ? "success" : "danger");
        })
        .catch(() => showMessage("Unable to send reset link right now.", "danger"));
}

function resetPasswordWithOtp(event) {
    event.preventDefault();

    const email = resetOtpEmail.value.trim();
    const otp = resetOtp.value.trim();
    const password = otpNewPassword.value;
    const confirm = otpConfirmPassword.value;

    if (!validateEmail(email) || !otp) {
        showMessage("Enter your email and reset OTP.", "danger");
        return;
    }

    if (password.length < 4) {
        showMessage("Password must be at least 4 characters.", "danger");
        return;
    }

    if (password !== confirm) {
        showMessage("Passwords do not match.", "danger");
        return;
    }

    fetch("/api/auth/reset-password-otp", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            email,
            otp,
            password
        })
    })
        .then(res => res.text())
        .then(messageText => {
            if (messageText.includes("successful")) {
                document.getElementById("forgotPasswordForm").reset();
                document.getElementById("resetPasswordOtpForm").reset();
                forgotPasswordForm.style.display = "none";
                resetPasswordOtpForm.style.display = "none";
                showMessage(messageText);
                return;
            }
            showMessage(messageText, "danger");
        })
        .catch(() => showMessage("Password reset failed.", "danger"));
}

function resetPassword(event) {
    event.preventDefault();

    const password = newPassword.value;
    const confirm = confirmPassword.value;

    if (!resetToken) {
        showMessage("Reset link is missing or invalid.", "danger");
        return;
    }

    if (password.length < 4) {
        showMessage("Password must be at least 4 characters.", "danger");
        return;
    }

    if (password !== confirm) {
        showMessage("Passwords do not match.", "danger");
        return;
    }

    fetch("/api/auth/reset-password", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            token: resetToken,
            password
        })
    })
        .then(res => res.text())
        .then(messageText => {
            if (messageText.includes("successful")) {
                showMessage(messageText);
                showLoginFromReset();
                return;
            }
            showMessage(messageText, "danger");
        })
        .catch(() => showMessage("Password reset failed.", "danger"));
}

function logout() {
    currentUser = null;
    editId = null;
    contacts = [];
    sessionStorage.removeItem("contactDiaryUser");
    homePage.style.display = "none";
    authSection.style.display = "block";
    loginPage.style.display = "block";
    registerPage.style.display = "none";
    resetPasswordPage.style.display = "none";
    document.getElementById("loginForm").reset();
}

// Contact form validation and save
function validateContact() {
    if (!cname.value.trim() || !cphone.value.trim()) {
        alert("Name and phone number are required.");
        return false;
    }

    if (cemail.value.trim() && !validateEmail(cemail.value.trim())) {
        alert("Enter a valid contact email.");
        return false;
    }

    if (!phoneRegex.test(cphone.value)) {
        alert("Phone number must be exactly 10 digits.");
        return false;
    }

    return true;
}

function saveContact(event) {
    event.preventDefault();

    if (!currentUser || !validateContact()) {
        return;
    }

    const url = editId ? `/api/contacts/${editId}` : `/api/contacts/${currentUser.id}`;
    const method = editId ? "PUT" : "POST";

    fetch(url, {
        method,
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            name: cname.value.trim(),
            phone: cphone.value.trim(),
            email: cemail.value.trim(),
            address: caddress.value.trim(),
            relation: crelation.value.trim(),
            favorite: false
        })
    })
        .then(res => {
            if (!res.ok) {
                throw new Error("Contact validation failed.");
            }
            return res.json();
    })
        .then(() => {
            newContact();
            if (activeView === "none") {
                activeView = "all";
            }
            loadContacts(true);
            showMessage("Contact saved.");
        })
        .catch(error => showMessage(error.message, "danger"));
}

function newContact() {
    editId = null;
    formTitle.textContent = "Add Contact";
    document.getElementById("contactForm").reset();
}

// Contact loading, filtering, and rendering
function loadContacts(shouldRender = true) {
    fetch(`/api/contacts/${currentUser.id}`)
        .then(res => res.json())
        .then(data => {
            contacts = data;
            if (shouldRender) {
                refreshActiveView();
            } else {
                activeView = "none";
                renderEmptyState();
            }
        });
}

function searchContacts(event) {
    if (event && event.preventDefault) {
        event.preventDefault();
    }

    const name = searchName.value.trim();
    if (!name) {
        activeView = "none";
        renderEmptyState();
        return;
    }

    activeView = "search";
    renderContacts(contacts.filter(contact => contact.name.toLowerCase().includes(name.toLowerCase())));
}

function clearSearch() {
    searchName.value = "";
    activeView = "none";
    renderEmptyState();
}

function showAllContacts() {
    searchName.value = "";
    activeView = "all";
    renderContacts(contacts);
}

function showFavoriteContacts() {
    searchName.value = "";
    activeView = "favorite";
    renderContacts(contacts.filter(contact => contact.favorite));
}

function refreshActiveView() {
    if (activeView === "all") {
        renderContacts(contacts);
        return;
    }
    if (activeView === "favorite") {
        renderContacts(contacts.filter(contact => contact.favorite));
        return;
    }
    if (activeView === "search" && searchName.value.trim()) {
        searchContacts({preventDefault: () => {}});
        return;
    }
    renderEmptyState();
}

function renderEmptyState() {
    list.innerHTML = '<p class="text-secondary">Tap All Contact, Favorite, or search by name to show contacts.</p>';
}

function renderContacts(data) {
    list.innerHTML = "";

    if (!data.length) {
        list.innerHTML = '<p class="text-secondary">No contacts found.</p>';
        return;
    }

    [...data]
        .sort((a, b) => Number(b.favorite) - Number(a.favorite) || a.name.localeCompare(b.name))
        .forEach(contact => {
        const col = document.createElement("div");
        col.className = "col-12";

        const card = document.createElement("div");
        card.className = `card contact-card shadow-sm ${contact.favorite ? "border-warning" : ""}`;

        const body = document.createElement("div");
        body.className = "card-body";

        body.innerHTML = `
            <div class="d-flex flex-column flex-md-row justify-content-between gap-3">
                <div>
                    <h3 class="h5 mb-1">${escapeHtml(contact.name)} ${contact.favorite ? '<span class="badge text-bg-warning">Favorite</span>' : ""}</h3>
                    <p class="mb-1">${escapeHtml(contact.phone)}${contact.email ? ` | ${escapeHtml(contact.email)}` : ""}</p>
                    <p class="mb-1 text-secondary">${escapeHtml(contact.address || "No address")}</p>
                    ${contact.relation ? `<span class="badge text-bg-light">${escapeHtml(contact.relation)}</span>` : ""}
                </div>
                <div class="d-flex flex-wrap gap-2 align-content-start no-print">
                    <button class="btn btn-sm ${contact.favorite ? "btn-warning" : "btn-outline-warning"}" type="button">
                        ${contact.favorite ? "Favorite" : "Add Favorite"}
                    </button>
                    <button class="btn btn-sm btn-warning" type="button">Edit</button>
                    <button class="btn btn-sm btn-success" type="button">WhatsApp</button>
                    <button class="btn btn-sm btn-danger" type="button">Delete</button>
                </div>
            </div>
        `;

        const buttons = body.querySelectorAll("button");
        buttons[0].addEventListener("click", () => toggleFavorite(contact.id));
        buttons[1].addEventListener("click", () => editContact(contact));
        buttons[2].addEventListener("click", () => shareContact(contact));
        buttons[3].addEventListener("click", () => deleteContact(contact.id));

        card.appendChild(body);
        col.appendChild(card);
        list.appendChild(col);
    });
}

function editContact(contact) {
    editId = contact.id;
    formTitle.textContent = "Edit Contact";
    cname.value = contact.name;
    cphone.value = contact.phone;
    cemail.value = contact.email;
    caddress.value = contact.address || "";
    crelation.value = contact.relation || "";
    window.scrollTo({top: 0, behavior: "smooth"});
}

// Contact actions
function deleteContact(id) {
    if (!confirm("Delete this contact?")) {
        return;
    }

    fetch(`/api/contacts/${id}`, {method: "DELETE"})
        .then(() => {
            loadContacts(true);
            showMessage("Contact deleted.");
        });
}

function shareContact(contact) {
    const parts = [
        "Contact Diary",
        `Name: ${contact.name}`,
        `Phone: ${contact.phone}`
    ];

    if (contact.email) {
        parts.push(`Email: ${contact.email}`);
    }
    if (contact.address) {
        parts.push(`Address: ${contact.address}`);
    }
    if (contact.relation) {
        parts.push(`Relation: ${contact.relation}`);
    }

    const messageText = encodeURIComponent(parts.join("\n"));
    window.open(`https://wa.me/?text=${messageText}`, "_blank");
}

function toggleFavorite(id) {
    fetch(`/api/contacts/${id}/favorite`, {method: "PATCH"})
        .then(res => {
            if (!res.ok) {
                throw new Error("Favorite update failed.");
            }
            return res.json();
        })
        .then(updated => {
            contacts = contacts.map(contact => contact.id === updated.id ? updated : contact);
            if (activeView === "none") {
                activeView = "all";
            }
            refreshActiveView();
        })
        .catch(error => showMessage(error.message, "danger"));
}

// Export and print actions
function exportContacts() {
    if (currentUser) {
        window.location.href = `/api/contacts/${currentUser.id}/export`;
    }
}

function printContacts() {
    window.print();
}

// Utility helpers
function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function debounce(callback, delay) {
    let timeoutId;
    return function (event) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => callback(event), delay);
    };
}

const savedUser = sessionStorage.getItem("contactDiaryUser");
const queryParams = new URLSearchParams(window.location.search);
resetToken = queryParams.get("resetToken");

if (resetToken) {
    showResetPassword();
}

if (savedUser && !resetToken) {
    currentUser = JSON.parse(savedUser);
    authSection.style.display = "none";
    homePage.style.display = "block";
    userInfo.textContent = `Logged in as ${currentUser.name} (${currentUser.email})`;
    loadContacts(false);
}
