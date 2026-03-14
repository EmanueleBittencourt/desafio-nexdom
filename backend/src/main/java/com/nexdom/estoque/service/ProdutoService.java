package com.nexdom.estoque.service;

import com.nexdom.estoque.dto.LucroProdutoResponse;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {

    List<Produto> buscarTodos();

    Optional<Produto> buscarPorId(Long id);

    List<Produto> buscarPorTipo(TipoProduto tipo);

    Produto criar(Produto produto);

    Produto atualizar(Long id, Produto produto);

    void excluir(Long id);

    LucroProdutoResponse getLucroPorProduto(Long id);
}
