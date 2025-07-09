const API_BASE = `/backend`;

const loginForm = document.getElementById("loginForm");
if (loginForm) {
  loginForm.addEventListener("submit", async function (e) {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const isAdmin = document.getElementById("isAdmin").checked;
    const message = document.getElementById("message");

    try {
     const response = await fetch(`${API_BASE}/con/login`, {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ username, password, isAdmin }),
       credentials: "include" // ← Cần thiết để trình duyệt gửi và nhận session cookie
     });

if (!response.ok) {
  throw new Error("Lỗi kết nối đến máy chủ");
}

      const role = await response.text();

      if (role === "admin") {
        message.style.color = 'green';
        message.textContent = "Đăng nhập quản trị viên thành công!";
        setTimeout(() => window.location.href = "main_admin.html", 1500);
      } else if (role === "user") {
        message.style.color = 'green';
        message.textContent = "Đăng nhập người dùng thành công!";
        setTimeout(() => window.location.href = "main_user.html", 1500);
      } else {
        message.style.color = 'red';
        message.textContent = "Sai thông tin đăng nhập!";
      }
    } catch (error) {
      message.style.color = 'red';
      message.textContent = "Lỗi kết nối đến máy chủ!";
    }
  });
}


// ====== Đăng ký ======
const registerForm = document.getElementById("registerForm");
if (registerForm) {
  registerForm.addEventListener("submit", function (e) {

    e.preventDefault();
        const message = document.getElementById("registerMessage");

    const slide1Inputs = formSlides.children[0].querySelectorAll("input[required]");
    for (let input of slide1Inputs) {
        if (!input.value.trim()) {
            currentSlide = 0;
            updateSlide();
            message.textContent = `Vui lòng điền ${input.placeholder}!`;
            input.focus();
            return;
        }
    }
    const slide2Inputs = formSlides.children[1].querySelectorAll("input[required]");
    for (let input of slide2Inputs) {
        if (!input.value.trim()) {
            currentSlide = 1;
            updateSlide();
            message.textContent = `Vui lòng điền ${input.placeholder}!`;
            input.focus();
            return;
        }
    }
    const data = {
      username: document.getElementById("username").value.trim(),
      password: document.getElementById("password").value.trim(),
      email: document.getElementById("email").value.trim(),
      age: parseInt(document.getElementById("age").value),
      occupation: document.getElementById("occupation").value.trim(),
      major: document.getElementById("major").value.trim(),
      favoriteGenres: document.getElementById("favoriteGenres").value.trim()
    };

    fetch(`${API_BASE}/con/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    })
      .then(res => res.text())  // <-- CHỈNH Ở ĐÂY
      .then(result => {
        if (result === "true") {
          message.style.color = "green";
          message.textContent = "Đăng ký thành công!";
          setTimeout(() => window.location.href = "login.html", 2000);
        } else {
          message.style.color = "red";
          message.textContent = "Lỗi đăng ký!";
        }
      })
      .catch(err => {
        message.style.color = "red";
        message.textContent = "Lỗi server!";
        console.error(err);
      });

  });
}
// ====== quên mật khẩu ======
 const forgotForm = document.getElementById("forgotPasswordForm");
 if (forgotForm) {

    forgotForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const data = {
        username: document.getElementById("username").value.trim(),
        email: document.getElementById("email").value.trim(),
        newPassword: document.getElementById("newPassword").value.trim()
      };

      const message = document.getElementById("forgotMessage");

      try {
        const response = await fetch(`${API_BASE}/con/forgot_password`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(data)
        });

        const result = await response.text();
        if (result === "true") {
          message.style.color = "green";
          message.textContent = "Đặt lại mật khẩu thành công!";
          setTimeout(() => window.location.href = "login.html", 2000);
        } else {
          message.style.color = "red";
          message.textContent = "Tên đăng nhập hoặc email không đúng!";
        }
      } catch (err) {
        message.style.color = "red";
        message.textContent = "Lỗi server!";
        console.error(err);
      }
    });
}




const excludedPaths = ["login.html", "register.html", "forgot_password.html"];

// Lấy tên file hiện tại (VD: "main_admin.html")
const currentPath = window.location.pathname.split("/").pop();

if (!excludedPaths.includes(currentPath)) {
  fetch("/backend/con/check_session", { credentials: "include" })
    .then(res => res.json())
    .then(data => {
      if (data.status !== "logged_in") {
        window.location.href = "login.html";
      } else {
        console.log("Người dùng:", data.username);
        console.log("Vai trò:", data.role);
      }
    })
    .catch(() => window.location.href = "login.html");
}
