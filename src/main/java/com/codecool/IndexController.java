package com.codecool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Controller
public class IndexController {

    @Value("${spring.datasource.url}")
    private String dsUrl;

    @Value("${spring.datasource.username}")
    private String dsUser;

    @Value("${spring.datasource.password}")
    private String dsPass;

    @Value("${demo.name}")
    private String demoName;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("demoName", demoName);
        model.addAttribute("results", List.of());
        return "index";
    }

    @PostMapping("/")
    public String index(@RequestParam(name = "sql") String sql, Model model) {
        model.addAttribute("demoName", demoName);
        if ("jdbc:postgresql://changeit:5432/changeit".equals(dsUrl)
                || "changeit".equals(dsUser)
                || "changeit".equals(dsPass)) {
            model.addAttribute("alertType", "warning");
            model.addAttribute("alertMessage", new StringBuilder()
                    .append("Must set ")
                    .append("SPRING_DATASOURCE_URL, ")
                    .append("SPRING_DATASOURCE_USERNAME, ")
                    .append("SPRING_DATASOURCE_PASSWORD")
                    .toString());
        } else {
            try {
                if (sql.matches("^\\s*?SELECT.*$")) {
                    var results = jdbcTemplate.queryForList(sql);
                    model.addAttribute("results", results);
                } else if (sql.matches("^\\s*?(INSERT|UPDATE|DELETE).*$")) {
                    int rows = jdbcTemplate.update(sql);
                    model.addAttribute("alertType", "info");
                    model.addAttribute("alertMessage", "Successful: " + rows + " row(s) affected");
                } else {
                    model.addAttribute("alertType", "danger");
                    model.addAttribute("alertMessage", "Unsupported: " + sql);
                }
            } catch (DataAccessException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                model.addAttribute("alertType", "danger");
                model.addAttribute("alertMessage", "Query error: " + sw.toString());
            }
        }
        return "index";
    }
}
