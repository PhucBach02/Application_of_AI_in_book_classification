const API_BASE = `/backend`;
let currentUsername = "";
let booksPerPage = 8;
let currentPage = 1;
let filteredBooks = [];
let allBooks = [];

async function fetchUserSession() {
    try {
        const response = await fetch(`${API_BASE}/con/check_session`, {
            credentials: 'include'
        });
        const data = await response.json();

        if (data.status === 'logged_in') {
            currentUsername = data.username;
            document.getElementById("readerName").textContent = ` ${currentUsername}`;
        }
         else {
            window.location.href = "/login.html";
        }
    } catch (error) {
        console.error("Lỗi khi lấy session:", error);
       window.location.href = "/login.html";
    }
}

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

async function fetchUserBooks(username) {
    try {
        const res = await fetch(`${API_BASE}/res/getuserbook`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({ username })
        });

        let books = await res.json();

        if (typeof books === "string") {
            books = JSON.parse(books);
        }

        displayBooksforUser(books, 'userBookList');
    } catch (e) {
        console.error("Lỗi lấy sách gợi ý:", e);
    }
}



async function fetchBooks() {
  
   
  try {
    const res = await fetch(`${API_BASE}/res/getbook`);
    let books = await res.json();
    if (typeof books === "string") books = JSON.parse(books);

    allBooks = books;
    filteredBooks = books;
    currentPage = 1;
    renderPaginationBooks(filteredBooks);
  } catch (e) {
    console.error("Lỗi lấy sách:", e);
  }
}
function logout() {
    fetch(`${API_BASE}/con/logout`, {
        method: "POST",
        credentials: "include"
    }).then(() => {
        window.location.href = "/login.html";
    });
}
function displayBooks(books, containerId) {
  const container = document.getElementById(containerId);
  container.innerHTML = '';

  if (!books || books.length === 0) {
    container.innerHTML = '<p class="text-gray-500">Không có sách nào.</p>';
    return;
  }

  books.forEach(book => {
    const div = document.createElement('div');
    div.className = 'bg-white p-4 rounded-xl shadow hover:shadow-lg transition cursor-pointer flex flex-col items-center self-start h-auto';

    div.innerHTML = `
      <img src="${book.imageUrl}" alt="${book.title}" class="w-full h-40 object-contain rounded-lg mb-3" />
      <h3 class="text-sm font-semibold text-center line-clamp-2 h-14">${book.title}</h3>`
    ;

    div.addEventListener("click", () => showBookDetail(book));
    container.appendChild(div);
  });
}
function displayBooksforUser(books, containerId) {
  const container = document.getElementById(containerId);
  container.innerHTML = '';

  if (!books || books.length === 0) {
    container.innerHTML = '<p class="text-gray-500">Không có sách nào.</p>';
    return;
  }

  books.forEach(book => {
    const div = document.createElement('div');
    div.className = 'bg-white p-4 rounded-xl shadow hover:shadow-lg transition cursor-pointer flex flex-col items-center w-60 flex-shrink-0';

    div.innerHTML = `
      <img src="${book.imageUrl}" alt="${book.title}" class="w-full h-40 object-contain rounded-lg mb-3 " />
      <h3 class="text-sm font-semibold text-center line-clamp-2 h-14">${book.title}</h3>
    `;

    div.addEventListener("click", () => showBookDetail(book));
    container.appendChild(div);
  });
}

function renderPaginationBooks(books) {
  const start = (currentPage - 1) * booksPerPage;
  const end = start + booksPerPage;
  const pageBooks = books.slice(start, end);

  displayBooks(pageBooks, 'bookList');
  renderPaginationControls(books.length);
}

function renderPaginationControls(totalBooks) {
  const paginationContainer = document.getElementById("pagination");
  paginationContainer.innerHTML = '';

  const totalPages = Math.ceil(totalBooks / booksPerPage);
  const maxVisiblePages = 5;
  const pages = [];

  if (totalPages <= 1) return;

  // ⏮ Nút Trước
  const prevBtn = document.createElement("button");
  prevBtn.innerText = "⏮";
  prevBtn.className = "px-4 py-2 rounded-xl border bg-white text-gray-700 hover:bg-gray-100";
  prevBtn.disabled = currentPage === 1;
  prevBtn.classList.toggle("opacity-50", currentPage === 1);
  prevBtn.addEventListener("click", () => {
    if (currentPage > 1) {
      currentPage--;
      renderPaginationBooks(filteredBooks);
    }
  });
  paginationContainer.appendChild(prevBtn);

  // Các trang
  if (totalPages <= maxVisiblePages + 2) {
    for (let i = 1; i <= totalPages; i++) pages.push(i);
  } else {
    pages.push(1);

    let start = Math.max(currentPage - 2, 2);
    let end = Math.min(currentPage + 2, totalPages - 1);

    if (start > 2) pages.push("...");
    for (let i = start; i <= end; i++) pages.push(i);
    if (end < totalPages - 1) pages.push("...");

    pages.push(totalPages);
  }

  pages.forEach(p => {
    const btn = document.createElement("button");
    btn.innerText = p;
    btn.className =
      typeof p === "number"
        ? `px-4 py-2 rounded-xl border ${
            p === currentPage ? "bg-blue-500 text-white" : "bg-white text-gray-700 hover:bg-gray-100"
          }`
        : "px-3 py-2 text-gray-500 pointer-events-none";

    if (typeof p === "number" && p !== currentPage) {
      btn.addEventListener("click", () => {
        currentPage = p;
        renderPaginationBooks(filteredBooks);
      });
    }

    paginationContainer.appendChild(btn);
  });

  // ⏭ Nút Sau
  const nextBtn = document.createElement("button");
  nextBtn.innerText = "⏭";
  nextBtn.className = "px-4 py-2 rounded-xl border bg-white text-gray-700 hover:bg-gray-100";
  nextBtn.disabled = currentPage === totalPages;
  nextBtn.classList.toggle("opacity-50", currentPage === totalPages);
  nextBtn.addEventListener("click", () => {
    if (currentPage < totalPages) {
      currentPage++;
      renderPaginationBooks(filteredBooks);
    }
  });
  paginationContainer.appendChild(nextBtn);
}
function showBookDetail(book) {
  document.getElementById("detailTitle").innerText = book.title;
  document.getElementById("detailImage").src = book.imageUrl;
  document.getElementById("detailDescription").innerText = book.description;
  document.getElementById("detailGenre").innerText = book.genre;
  document.getElementById("detailTags").innerText = book.tags;
  document.getElementById("detailTargetAudience").innerText = book.targetAudience;
  document.getElementById("detailAgeRange").innerText = book.ageRange;
  document.getElementById("detailDifficulty").innerText = book.difficulty;

  document.getElementById("bookDetailModal").classList.remove("hidden");
  document.getElementById("bookDetailModal").classList.add("flex");
}
// Gán dropdown chọn số sách mỗi trang
document.getElementById("booksPerPageSelect").addEventListener("change", e => {
  booksPerPage = parseInt(e.target.value);
  currentPage = 1;
  renderPaginationBooks(filteredBooks);
});
// Tìm kiếm
document.getElementById("searchInput").addEventListener("input", e => {
  const keyword = e.target.value.toLowerCase();
  filteredBooks = allBooks.filter(book =>
    book.title.toLowerCase().includes(keyword) ||
    book.description.toLowerCase().includes(keyword) ||
    (book.tags || '').toLowerCase().includes(keyword)
  );

  currentPage = 1;
  renderPaginationBooks(filteredBooks);
});


async function init() {
  await fetchUserSession();
  await fetchUserBooks(currentUsername);
  await fetchBooks();
}
setInterval(() => {
  checkSessionOnce();
}, 310000); //5p

init();
// Slider điều hướng
document.getElementById("prevSlide").addEventListener("click", () => {
  document.getElementById("userBookList").scrollBy({ left: -256, behavior: 'smooth' });
});

document.getElementById("nextSlide").addEventListener("click", () => {
  document.getElementById("userBookList").scrollBy({ left: 256, behavior: 'smooth' });
});
