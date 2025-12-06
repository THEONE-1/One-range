package com.one.range.controller.basevul.sqli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;


@RestController
@RequestMapping("/home/sqli/jdbc")
public class JDBC {

    @Value("${spring.datasource.url}")
    private String db_url;

    @Value("${spring.datasource.username}")
    private String db_user;

    @Value("${spring.datasource.password}")
    private String db_pass;


    @GetMapping("/error_based")
    public String error_based(String id) {

        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);

            Statement stmt = conn.createStatement();
            // 直接拼接，不进行任何过滤，用于SQL注入测试
            String sql = "select * from users where id = '" + id + "'";
            result.append("执行SQL语句: ").append(sql).append(System.lineSeparator());
            result.append("查询结果： ").append(System.lineSeparator());

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String res_name = rs.getString("username");
                String res_pass = rs.getString("password");
                String info = String.format("%s : %s%n", res_name, res_pass);
                result.append(info).append(System.lineSeparator());
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            // 返回完整的错误信息，包括SQL错误，用于报错注入
            result.append("错误信息: ").append(e.getMessage()).append(System.lineSeparator());
            result.append("错误类型: ").append(e.getClass().getName()).append(System.lineSeparator());
            if (e.getCause() != null) {
                result.append("原因: ").append(e.getCause().getMessage()).append(System.lineSeparator());
            }
        }
        return result.toString();
    }


    @GetMapping("/int_based")
    public String int_based(String id) {

        StringBuilder result = new StringBuilder();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);

            String sql = "select * from users where id = " + id;
            result.append("执行SQL语句: ").append(sql).append(System.lineSeparator());
            result.append("查询结果: ").append(System.lineSeparator());
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String res_name = rs.getString("username");
                String res_pass = rs.getString("password");
                String info = String.format("%s : %s%n", res_name, res_pass);
                result.append(info).append(System.lineSeparator());
            }
            rs.close();
            st.close();
            conn.close();
        } catch (Exception e) {
            result.append("错误: ").append(e.toString()).append(System.lineSeparator());
        }
        return result.toString();
    }

    @GetMapping("/blind_time_based")
    public String blind_time_based(String id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);

            String sql = "select * from users where id = " + id;
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                rs.close();
                st.close();
                conn.close();
                return "查询成功！";
            }
            rs.close();
            st.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "查询失败！";
    }

    @GetMapping("/blind_bool_based")
    public String blind_bool_based(String id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);

            // 漏洞：直接拼接用户输入，返回布尔结果
            String sql = "select * from users where id = " + id;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            boolean exists = rs.next();
            rs.close();
            stmt.close();
            conn.close();

            return exists ? "存在该用户" : "用户不存在";
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }

}

