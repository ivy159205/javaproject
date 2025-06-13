<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page session="true" %>
        <% long startTime=0; if (session.getAttribute("startTime")==null) { startTime=System.currentTimeMillis();
            session.setAttribute("startTime", startTime); } else { startTime=(Long) session.getAttribute("startTime"); }
            %>
            <!DOCTYPE html>
            <html>

            <head>
                <meta charset="UTF-8">
                <title>Trang chủ</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
            </head>

            <body>
                <div class="container mt-4">
                    <h2 class="mb-4">Trang chủ hệ thống hduc 2</h2>
                    <nav class="mb-4">
                        <a href="users" class="btn btn-primary me-2">Quản lý người dùng</a>
                        <a href="orders" class="btn btn-success">Danh sách đơn hàng</a>
                    </nav>

                    <div class="alert alert-info">
                        Thời gian bạn đã sử dụng hệ thống: <span id="timeUsed">0 giây</span>
                    </div>

                    <!-- Hidden input để truyền dữ liệu từ server sang client -->
                    <input type="hidden" id="startTimeValue" value="<%= startTime %>">
                </div>

                <script>
                    // Lấy giá trị từ hidden input - Cách an toàn nhất
                    const serverStartTime = parseInt(document.getElementById('startTimeValue').value);

                    console.log('Start time:', serverStartTime);

                    function updateTimeUsed() {
                        const now = Date.now();
                        const diff = Math.floor((now - serverStartTime) / 1000);
                        const minutes = Math.floor(diff / 60);
                        const seconds = diff % 60;

                        const element = document.getElementById("timeUsed");
                        if (element) {
                            element.textContent = minutes + " phút " + seconds + " giây";
                        }
                    }

                    // Đợi DOM load xong
                    document.addEventListener('DOMContentLoaded', function () {
                        updateTimeUsed(); // Cập nhật ngay
                        setInterval(updateTimeUsed, 1000); // Cập nhật mỗi giây
                    });
                </script>
            </body>

            </html>