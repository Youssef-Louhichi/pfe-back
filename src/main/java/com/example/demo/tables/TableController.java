package com.example.demo.tables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired
    private TableRepository tableRepository; 

    @GetMapping("/{id}")
    public ResponseEntity<DbTable> getTableById(@PathVariable Long id) {
        Optional<DbTable> table = tableRepository.findById(id);
        return table.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
