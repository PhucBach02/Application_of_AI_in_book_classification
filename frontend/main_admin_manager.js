const API_BASE = `/backend`;
  // ====== gợi ý thể loại ======

  const genreInput = document.getElementById("genre");
  const descriptionInput = document.getElementById("description");
  const suggestGenreBtn = document.getElementById("suggestGenreAI");

  suggestGenreBtn.addEventListener("click", async () => {
    const description = descriptionInput.value.trim();

    if (!description) {
      alert("Vui lòng nhập mô tả trước khi gợi ý thể loại.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE}/res/suggest_genre`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ description })
      });

      const genre = await response.text(); // hoặc `await response.json()` nếu trả về JSON
      genreInput.value = genre;

    } catch (error) {
      console.error("Lỗi khi gọi API:", error);
      alert("Không thể gợi ý thể loại từ AI.");
    }
  });

// ====== Gợi ý tags ======

const tagsInput = document.getElementById("tags");
const suggestTagsBtn = document.getElementById("suggestTagsAI");

suggestTagsBtn.addEventListener("click", async () => {
  const description = document.getElementById("description").value.trim();

  if (!description) {
    alert("Vui lòng nhập mô tả trước khi gợi ý từ khóa.");
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/res/suggest_tags`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ description })
    });

    const tags = await response.text(); // hoặc response.json() nếu server trả về JSON
    tagsInput.value = tags.replace(/\.$/, ""); // xóa dấu chấm cuối nếu có

  } catch (error) {
    console.error("Lỗi khi gọi API:", error);
    alert("Không thể gợi ý từ khóa từ AI.");
  }
});

// ====== Xác nhận thêm sách ======
document.getElementById("addBookForm").addEventListener("submit", async function (e) {
    e.preventDefault(); // Ngăn reload trang

    const title = document.getElementById("title").value.trim();
    const description = document.getElementById("description").value.trim();
    const genre = document.getElementById("genre").value.trim();
    const tags = document.getElementById("tags").value.trim();
    const targetAudience = document.getElementById("targetAudience").value.trim();
    const ageRange = parseInt(document.getElementById("ageRange").value.trim());
    const difficulty = document.getElementById("difficulty").value;
    const imageUrl = document.getElementById("imageUrl").value.trim();

    // Kiểm tra dữ liệu đầu vào
    if (!title || !description || !genre || isNaN(ageRange) || ageRange < 5 || !difficulty) {
        alert("Vui lòng nhập đầy đủ thông tin hợp lệ (tuổi ≥ 5, chọn độ khó).");
        return;
    }

    // Tạo object sách mới
    const newBook = {
        title,
        description,
        genre,
        tags,
        targetAudience,
        ageRange,
        difficulty,
        imageUrl
    };

    try {
        // Gửi lên backend (nếu có API)
        const response = await fetch(`${API_BASE}/res/addbook`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newBook)
        });

        const resultText = await response.text(); // giả sử backend trả về "true" hoặc "false"
        const responseEl = document.getElementById("post-response");

        if (resultText.trim() === "true") {
            responseEl.innerText = "Thêm sách thành công!";
            responseEl.classList.remove("text-red-600");
            responseEl.classList.add("text-green-600");

            // Đóng modal & reset form
            document.getElementById("addBookModal").classList.add("hidden");
            document.getElementById("addBookForm").reset();

            if (typeof loadBooks === 'function') {
                loadBooks();
            }
        } else {
            responseEl.innerText = "Đã xảy ra lỗi khi thêm sách.";
            responseEl.classList.remove("text-green-600");
            responseEl.classList.add("text-red-600");
        }

        // Tự động ẩn thông báo sau 3 giây
        setTimeout(() => {
            responseEl.innerText = " ";
        }, 3000);

    } catch (error) {
        const responseEl = document.getElementById("post-response");
        responseEl.innerText = "Đã xảy ra lỗi khi thêm sách.";
        responseEl.classList.remove("text-green-600");
        responseEl.classList.add("text-red-600");

        setTimeout(() => {
            responseEl.innerText = " ";
        }, 3000);

        console.error(error);
    }
});
// ====== load sách ======
window.addEventListener("DOMContentLoaded", loadBooks);

async function loadBooks() {
    try {
        const response = await fetch(`${API_BASE}/res/getbook`);
        const books = await response.json();

        const bookList = document.getElementById("bookList");
        bookList.innerHTML = ""; // Xóa cũ

        books.forEach(book => {
            const card = document.createElement("div");
            card.className = 'bg-white p-4 rounded-xl shadow hover:shadow-lg transition cursor-pointer flex flex-col items-center self-start h-auto';
            card.innerHTML = `
                <img src="${book.imageUrl}" alt="${book.title}" class="w-full h-64 object-cover rounded-md mb-3"/>
                <h3 class="text-lg font-bold text-center text-[#0e141b]">${book.title}</h3>
            `;

            // Bấm vào sẽ hiện modal chi tiết
            card.addEventListener("click", () => showBookDetail(book));

            bookList.appendChild(card);
        });

    } catch (error) {
        console.error("Lỗi khi tải danh sách sách:", error);
    }
}
let selectedBookId = null;

function showBookDetail(book) {
    selectedBookId = book.id; // ← cần có thuộc tính id trong dữ liệu từ backend
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
// Gán dropdown chọn số sách mỗi trang
document.getElementById("booksPerPageSelect").addEventListener("change", e => {
  booksPerPage = parseInt(e.target.value);
  currentPage = 1;
  renderPaginationBooks(filteredBooks);
});
// ====== xóa sách ======
document.getElementById("deleteBookBtn").addEventListener("click", async () => {
    if (!selectedBookId) return;
    if (!confirm("Bạn có chắc chắn muốn xóa cuốn sách này?")) return;

    try {
        const response = await fetch(`${API_BASE}/res/deletebook`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ id: selectedBookId })
        });

        const resultText = await response.text();
        if (resultText.trim() === "true") {
            alert("Xóa sách thành công!");
            document.getElementById("bookDetailModal").classList.add("hidden");
            loadBooks();
        } else {
            alert("Xóa sách thất bại!");
        }
    } catch (error) {
        console.error("Lỗi khi xóa sách:", error);
        alert("Có lỗi xảy ra khi xóa sách.");
    }
});
// ====== Cập nhập sách ======

document.getElementById("updateBookBtn").addEventListener("click", () => {
  // Lấy thông tin sách hiện tại từ modal chi tiết
  document.getElementById("updateTitle").value = document.getElementById("detailTitle").innerText;
  document.getElementById("updateDescription").value = document.getElementById("detailDescription").innerText;
  document.getElementById("updateGenre").value = document.getElementById("detailGenre").innerText;
  document.getElementById("updateTags").value = document.getElementById("detailTags").innerText;
  document.getElementById("updateTargetAudience").value = document.getElementById("detailTargetAudience").innerText;
  document.getElementById("updateAgeRange").value = document.getElementById("detailAgeRange").innerText;
  document.getElementById("updateDifficulty").value = document.getElementById("detailDifficulty").innerText;
  document.getElementById("updateImageUrl").value = document.getElementById("detailImage").src;

  document.getElementById("bookDetailModal").classList.add("hidden");
  document.getElementById("updateBookModal").classList.remove("hidden");
  document.getElementById("updateBookModal").classList.add("flex");
});

document.getElementById("closeUpdateModal").addEventListener("click", () => {
  document.getElementById("updateBookModal").classList.add("hidden");
});
document.getElementById("cancelUpdateBtn").addEventListener("click", () => {
  document.getElementById("updateBookModal").classList.add("hidden");
});

document.getElementById("updateBookForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const updatedBook = {
    id: selectedBookId, // ID lưu từ khi showBookDetail
    title: document.getElementById("updateTitle").value.trim(),
    description: document.getElementById("updateDescription").value.trim(),
    genre: document.getElementById("updateGenre").value.trim(),
    tags: document.getElementById("updateTags").value.trim(),
    targetAudience: document.getElementById("updateTargetAudience").value.trim(),
    ageRange: parseInt(document.getElementById("updateAgeRange").value.trim()),
    difficulty: document.getElementById("updateDifficulty").value,
    imageUrl: document.getElementById("updateImageUrl").value.trim()
  };

  try {
    const response = await fetch(`${API_BASE}/res/updatebook`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(updatedBook)
    });

    const resultText = await response.text();
    if (resultText.trim() === "true") {
      alert("Cập nhật sách thành công!");
      document.getElementById("updateBookModal").classList.add("hidden");
      loadBooks();
    } else {
      alert("Cập nhật sách thất bại!");
    }
  } catch (error) {
    console.error("Lỗi khi cập nhật sách:", error);
    alert("Có lỗi xảy ra khi cập nhật sách.");
  }
});
// ====== Gợi ý ======
// ====== Gợi ý thể loại trong form cập nhật ======
document.getElementById("suggestUpdateGenreAI").addEventListener("click", async () => {
  const description = document.getElementById("updateDescription").value.trim();
  if (!description) {
    alert("Vui lòng nhập mô tả trước khi gợi ý thể loại.");
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/res/suggest_genre`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ description })
    });

    const genre = await response.text();
    document.getElementById("updateGenre").value = genre;

  } catch (error) {
    console.error("Lỗi khi gọi API gợi ý thể loại:", error);
    alert("Không thể gợi ý thể loại từ AI.");
  }
});

// ====== Gợi ý tags trong form cập nhật ======
document.getElementById("suggestUpdateTagsAI").addEventListener("click", async () => {
  const description = document.getElementById("updateDescription").value.trim();
  if (!description) {
    alert("Vui lòng nhập mô tả trước khi gợi ý từ khóa.");
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/res/suggest_tags`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ description })
    });

    const tags = await response.text();
    document.getElementById("updateTags").value = tags.replace(/\.$/, "");

  } catch (error) {
    console.error("Lỗi khi gọi API gợi ý tags:", error);
    alert("Không thể gợi ý từ khóa từ AI.");
  }
});
// ====== logout ======

async function logout() {
  try {
    const response = await fetch("/backend/con/logout", {
      method: "POST",
      credentials: "include"
    });

    if (!response.ok) {
      throw new Error("Đăng xuất không thành công");
    }

    // Sau khi logout thành công, chuyển về trang login
    window.location.href = "login.html";
  } catch (error) {
    console.error("Lỗi khi đăng xuất:", error);
    alert("Đã xảy ra lỗi khi đăng xuất. Vui lòng thử lại.");
  }
}
// ====== tìm kiếm sách ======
  const searchInput = document.getElementById("searchInput");

  searchInput.addEventListener("input", function () {
    const query = this.value.trim().toLowerCase();

    const filteredBooks = allBooks.filter((book) => {
      return (
        book.title.toLowerCase().includes(query) ||
        book.description.toLowerCase().includes(query) ||
        book.genre.toLowerCase().includes(query) ||
        book.tags.toLowerCase().includes(query) ||
        (book.targetAudience && book.targetAudience.toLowerCase().includes(query)) ||
        (book.difficulty && book.difficulty.toLowerCase().includes(query))
      );
    });

    currentPage = 1;
    renderPaginationBooks(filteredBooks);
});

  // Lưu dữ liệu để tìm kiếm
  let allBooks = [];
  let booksPerPage = 8;
  let currentPage = 1;
  let filteredBooks = [];
  async function loadBooks() {
    try {
      const response = await fetch(`${API_BASE}/res/getbook`);
      const books = await response.json();
//      if (typeof books === "string") books = JSON.parse(books);

      allBooks = books; // Lưu để dùng cho tìm kiếm
      filteredBooks = books;
      currentPage = 1;
      renderPaginationBooks(filteredBooks);
    } catch (error) {
      console.error("Lỗi khi tải danh sách sách:", error);
    }
  }

  function displayBooks(books) {
    const bookList = document.getElementById("bookList");
    bookList.innerHTML = "";

    books.forEach(book => {
      const card = document.createElement("div");
       card.className = 'bg-white p-4 rounded-xl shadow hover:shadow-lg transition cursor-pointer flex flex-col items-center self-start h-auto';
      card.innerHTML = `
        <img src="${book.imageUrl}" alt="${book.title}" class="w-full h-40 object-contain rounded-md mb-3"/>
        <h3 class="text-lg font-bold text-center text-[#0e141b] line-clamp-2 h-14">${book.title}</h3>
      `;

      card.addEventListener("click", () => showBookDetail(book));
      bookList.appendChild(card);
    });
  }

