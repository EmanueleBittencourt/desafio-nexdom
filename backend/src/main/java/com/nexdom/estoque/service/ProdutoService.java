package com.nexdom.estoque.service;

import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {

    List<Produto> listarTodos();

    Optional<Produto> buscarPorId(Long id);

    List<Produto> listarPorTipo(TipoProduto tipo);

    Produto salvar(Produto produto);

    Produto atualizar(Long id, Produto produto);

    void excluir(Long id);
}
