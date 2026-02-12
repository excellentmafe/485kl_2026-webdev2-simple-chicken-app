package com.example.chicken;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.net.URI;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*") // Autoriser toutes les origines (à restreindre en production)
@RestController
@RequestMapping("/api/chickens")
class ChickenController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Map<String, Object>> getAllChickens() {
        // Fix: alias required to fix issue uppercasing column names in H2 !
        return jdbcTemplate.queryForList("SELECT id as \"id\", name as \"name\", age as \"age\", weight as \"weight\" FROM chickens");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getChicken(@PathVariable Long id) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT id as \"id\", name as \"name\", age as \"age\", weight as \"weight\" FROM chickens WHERE id = ?", id);

        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Fix: Return 404 instead of causing 500 error
        }
        return ResponseEntity.ok(results.get(0));

    }

    @PostMapping
    public int addChicken(@RequestBody Map<String, Object> chicken) {
        // Fix: use SimpleJdbcInsert returning generated key. 
        // require to mask differences between H2 and Postgres 
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("chickens")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = Map.of(
                "name", (String) chicken.get("name"),
                "age", Integer.parseInt(chicken.get("age").toString()),
                "weight", Double.parseDouble(chicken.get("weight").toString()));

        Number key = insert.executeAndReturnKey(parameters);
        return key.intValue();
    }

    @DeleteMapping("/{id}")
    public void deleteChicken(@PathVariable Long id) {
        String sql="DELETE FROM chickens WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
