package com.example.demo.graph;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/graphs")
public class GraphController {

    @Autowired
    private GraphService graphService;

    @GetMapping
    public List<Graph> getAllGraphs() {
        return graphService.getAllGraphs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Graph> getGraphById(@PathVariable Long id) {
        Optional<Graph> graph = graphService.getGraphById(id);
        return graph.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Graph createGraph(@RequestBody Graph graph) {
        return graphService.saveGraph(graph);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Graph> updateGraph(@PathVariable Long id, @RequestBody Graph graph) {
        Graph updatedGraph = graphService.updateGraph(id, graph);
        return updatedGraph != null ? ResponseEntity.ok(updatedGraph) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGraph(@PathVariable Long id) {
        graphService.deleteGraph(id);
        return ResponseEntity.noContent().build();
    }
}
