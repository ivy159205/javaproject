<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <title>Danh sách đơn hàng</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>

        <body>
            <div class="container py-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="text-primary">🧾 Danh sách đơn hàng</h2>
                    <a href="orders?action=new" class="btn btn-success">+ Thêm đơn hàng mới</a>
                </div>

                <c:if test="${empty orders}">
                    <div class="alert alert-warning">Chưa có đơn hàng nào!</div>
                </c:if>

                <c:if test="${not empty orders}">
                    <div class="table-responsive">
                        <table class="table table-bordered table-hover align-middle text-center">
                            <thead class="table-dark">
                                <tr>
                                    <th>ID</th>
                                    <th>Người đặt</th>
                                    <th>Tổng tiền</th>
                                    <th>Ngày đặt</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${orders}">
                                    <tr>
                                        <td>${order.id}</td>
                                        <td>${order.userName}</td>
                                        <td><strong>${order.totalAmount}</strong> đ</td>
                                        <td>${order.orderDate}</td>
                                        <td>
                                            <a href="orders?action=edit&id=${order.id}"
                                                class="btn btn-warning btn-sm">Sửa</a>
                                            <a href="orders?action=delete&id=${order.id}" class="btn btn-danger btn-sm"
                                                onclick="return confirm('Bạn có chắc muốn xóa đơn hàng này?')">Xóa</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:if>

                <a href="users" class="btn btn-secondary mt-3">← Quản lý người dùng</a>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>