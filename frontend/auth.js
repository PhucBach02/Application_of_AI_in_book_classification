function checkSessionOnce() {
  fetch("/backend/con/check_session", { credentials: "include" })
    .then(res => {
      if (!res.ok) throw new Error("Lỗi server");
      return res.json();
    })
    .then(data => {
      if (data.status !== "logged_in") {
        alert("Phiên đăng nhập đã hết hạn!");
        window.location.href = "login.html";
      }
    })
    .catch(err => {
      console.error("Không xác thực được session:", err);
      window.location.href = "login.html";
    });
}

// Gọi kiểm tra ngay khi trang load
document.addEventListener("DOMContentLoaded", () => {
  checkSessionOnce(); // kiểm ngay lần đầu
});

// Sau đó kiểm tra định kỳ mỗi 30 giây
setInterval(() => {
  checkSessionOnce();
}, 310000); //5p
