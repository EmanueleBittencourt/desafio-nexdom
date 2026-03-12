package com.nexdom.estoque.controller;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.service.MovimentoEstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimentos")
@RequiredArgsConstructor
public class MovimentoEstoqueController {

    private final MovimentoEstoqueService movimentoEstoqueService;

    @GetMapping
    public ResponseEntity<List<MovimentoEstoque>> listarTodos() {
        return ResponseEntity.ok(movimentoEstoqueService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimentoEstoque> buscarPorId(@PathVariable Long id) {
        return movimentoEstoqueService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MovimentoEstoque> criar(@RequestBody MovimentoEstoque movimento) {
        MovimentoEstoque salvo = movimentoEstoqueService.salvar(movimento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }
}
