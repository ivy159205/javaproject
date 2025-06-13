package com.example.servlet;

import com.example.dao.UserAddressDAO;
import com.example.model.UserAddress;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/addresses")
public class UserAddressServlet extends HttpServlet {
    private UserAddressDAO addressDAO;

    @Override
    public void init() {
        addressDAO = new UserAddressDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action == null)
            action = "list";

        switch (action) {
            case "new":
                req.getRequestDispatcher("/jsp/address-form.jsp").forward(req, resp);
                break;
            case "edit":
                showEditForm(req, resp);
                break;
            case "delete":
                deleteAddress(req, resp);
                break;
            default:
                listAddresses(req, resp);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("update".equals(action))
            updateAddress(req, resp);
        else
            insertAddress(req, resp);
    }

    private void listAddresses(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<UserAddress> addresses = addressDAO.getAllUserAddresses();
        req.setAttribute("addresses", addresses);
        req.getRequestDispatcher("/jsp/address-list.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        UserAddress address = addressDAO.getAddressById(id);
        req.setAttribute("address", address);
        req.getRequestDispatcher("/jsp/address-form.jsp").forward(req, resp);
    }

    private void insertAddress(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int userId = Integer.parseInt(req.getParameter("userId"));
        String address = req.getParameter("address");
        String city = req.getParameter("city");
        String postalCode = req.getParameter("postalCode");

        addressDAO.addUserAddress(new UserAddress(userId, address, city, postalCode));
        resp.sendRedirect("addresses");
    }

    private void updateAddress(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        int userId = Integer.parseInt(req.getParameter("userId"));
        String address = req.getParameter("address");
        String city = req.getParameter("city");
        String postalCode = req.getParameter("postalCode");

        addressDAO.updateUserAddress(new UserAddress(id, userId, address, city, postalCode));
        resp.sendRedirect("addresses");
    }

    private void deleteAddress(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        addressDAO.deleteUserAddress(id);
        resp.sendRedirect("addresses");
    }
}
