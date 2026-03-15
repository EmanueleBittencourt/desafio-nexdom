package com.nexdom.estoque.controller;

import com.nexdom.estoque.dto.LucroProdutoResponse;
import com.nexdom.estoque.dto.TipoProdutoOption;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;
import com.nexdom.estoque.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    private static final Map<TipoProduto, String> LABELS_TIPO = Map.of(
            TipoProduto.ELETRONICO, "Eletrônico",
            TipoProduto.ELETRODOMESTICO, "Eletrodoméstico",
            TipoProduto.MOVEL, "Móvel"
    );

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoProdutoOption>> listarTipos() {
        List<TipoProdutoOption> tipos = Arrays.stream(TipoProduto.values())
                .map(t -> new TipoProdutoOption(t.name(), LABELS_TIPO.getOrDefault(t, t.name())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> buscarTodos() {
        return ResponseEntity.ok(produtoService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return produtoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/lucro")
    public ResponseEntity<LucroProdutoResponse> buscarLucroPorProduto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(produtoService.getLucroPorProduto(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Produto> criar(@RequestBody Produto produto) {
        Produto salvo = produtoService.criar(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        try {
            Produto atualizado = produtoService.atualizar(id, produto);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            produtoService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
