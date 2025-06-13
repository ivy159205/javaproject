package com.example.servlet;

import com.example.dao.OrderDAO;
import com.example.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import com.example.model.OrderWithUser;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private OrderDAO orderDAO;

    @Override
    public void init() {
        orderDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null)
            action = "list";

        switch (action) {
            case "new":
                req.getRequestDispatcher("/jsp/order-form.jsp").forward(req, resp);
                break;
            case "edit":
                showEditForm(req, resp);
                break;
            case "delete":
                deleteOrder(req, resp);
                break;
            default:
                listOrders(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("update".equals(action))
            updateOrder(req, resp);
        else
            insertOrder(req, resp);
    }

    private void listOrders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<OrderWithUser> orders = orderDAO.getAllOrdersWithUsers();
        req.setAttribute("orders", orders);
        req.getRequestDispatcher("/jsp/order-list.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        Order order = orderDAO.getOrderById(id);
        req.setAttribute("order", order);
        req.getRequestDispatcher("/jsp/order-form.jsp").forward(req, resp);
    }

    private void insertOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int userId = Integer.parseInt(req.getParameter("userId"));
        BigDecimal total = new BigDecimal(req.getParameter("totalAmount"));
        orderDAO.addOrder(new Order(userId, total));
        resp.sendRedirect("orders");
    }

    private void updateOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        int userId = Integer.parseInt(req.getParameter("userId"));
        BigDecimal total = new BigDecimal(req.getParameter("totalAmount"));
        orderDAO.updateOrder(new Order(id, userId, null, total));
        resp.sendRedirect("orders");
    }

    private void deleteOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        orderDAO.deleteOrder(id);
        resp.sendRedirect("orders");
    }
}
