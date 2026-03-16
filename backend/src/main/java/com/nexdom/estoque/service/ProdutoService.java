package com.nexdom.estoque.service;

import com.nexdom.estoque.dto.LucroProdutoResponse;
import com.nexdom.estoque.dto.ProdutoComResumoDTO;
import com.nexdom.estoque.dto.ResumoEstoqueDTO;
import com.nexdom.estoque.dto.ResumoVendasProdutoDTO;
import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {

    List<Produto> buscarTodos();

    Optional<Produto> buscarPorId(Long id);
    
    List<ProdutoComResumoDTO> buscarPorTipo(TipoProduto tipo);

    List<ProdutoComResumoDTO> buscarTodosComResumo();

    /** Lista todos os produtos (incluindo excluídos) para uso em filtros/dropdowns. */
    List<ProdutoComResumoDTO> listarTodosParaFiltro();

    Produto criar(Produto produto);

    Produto atualizar(Long id, Produto produto);

    void excluir(Long id);

    LucroProdutoResponse getLucroPorProduto(Long id);

    ResumoEstoqueDTO getResumoEstoque(Long produtoId);

    ResumoVendasProdutoDTO getResumoVendasPorProduto(Long produtoId);

    ResumoVendasProdutoDTO getResumoVendas(Long produtoId);
}
