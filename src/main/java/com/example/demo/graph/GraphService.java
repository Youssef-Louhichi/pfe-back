package com.example.demo.graph;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

	 @Autowired
	    private GraphRepository graphRepository;

	    public List<Graph> getAllGraphs() {
	        return graphRepository.findAll();
	    }

	    public Optional<Graph> getGraphById(Long id) {
	        return graphRepository.findById(id);
	    }

	    public Graph saveGraph(Graph graph) {
	        return graphRepository.save(graph);
	    }

	    public Graph updateGraph(Long id, Graph updatedGraph) {
	        return graphRepository.findById(id)
	                .map(graph -> {
	                    graph.setChartType(updatedGraph.getChartType());
	                    graph.setColors(updatedGraph.getColors());
	                    graph.setColumnX(updatedGraph.getColumnX());
	                    graph.setColumnY(updatedGraph.getColumnY());
	                    graph.setData(updatedGraph.getData());
	                    graph.setFormat(updatedGraph.getFormat());
	                    graph.setHeaders(updatedGraph.getHeaders());
	                    graph.setHeight(updatedGraph.getHeight());
	                    graph.setWidth(updatedGraph.getWidth());
	                    graph.setLeftpos(updatedGraph.getLeftpos());
	                    graph.setToppos(updatedGraph.getToppos());
	                    graph.setRapport(updatedGraph.getRapport());
	                    return graphRepository.save(graph);
	                }).orElse(null);
	    }

	    public void deleteGraph(Long id) {
	        graphRepository.deleteById(id);
	    }
	
	
}
