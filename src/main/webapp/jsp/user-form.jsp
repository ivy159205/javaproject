<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html>

        <head>
            <title>${user != null ? 'Sửa' : 'Thêm'} người dùng</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
        </head>

        <body>
            <div class="container mt-4">
                <h2>${user != null ? 'Sửa' : 'Thêm'} người dùng</h2>

                <form method="post" action="users">
                    <c:if test="${user != null}">
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="id" value="${user.id}">
                    </c:if>

                    <div class="mb-3">
                        <label for="name" class="form-label">Tên:</label>
                        <input type="text" class="form-control" id="name" name="name"
                            value="${user != null ? user.name : ''}" required>
                    </div>

                    <div class="mb-3">
                        <label for="email" class="form-label">Email:</label>
                        <input type="email" class="form-control" id="email" name="email"
                            value="${user != null ? user.email : ''}" required>
                    </div>

                    <div class="mb-3">
                        <label for="phone" class="form-label">Số điện thoại:</label>
                        <input type="text" class="form-control" id="phone" name="phone"
                            value="${user != null ? user.phone : ''}">
                    </div>

                    <button type="submit" class="btn btn-success">
                        ${user != null ? 'Cập nhật' : 'Thêm mới'}
                    </button>
                    <a href="users" class="btn btn-secondary">Hủy</a>
                </form>
            </div>
        </body>

        </html>