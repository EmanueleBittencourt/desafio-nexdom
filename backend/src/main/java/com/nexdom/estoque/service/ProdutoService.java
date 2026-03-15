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

    /**
     * Retorna resumo (total entrada, saída, lucro). Se produtoId for nulo, considera todos os produtos não excluídos;
     * caso contrário, apenas o produto com o id informado (se existir e não estiver excluído).
     */
    ResumoEstoqueDTO getResumoEstoque(Long produtoId);

    /**
     * Resumo de vendas do produto: quantidade total de saídas, valor total de venda e lucro.
     * Lucro = Σ (Valor de Venda - Custo de Aquisição) × Qtd Vendida.
     */
    ResumoVendasProdutoDTO getResumoVendasPorProduto(Long produtoId);

    /**
     * Resumo de vendas. Se produtoId for nulo, retorna totais de todos os produtos não excluídos;
     * caso contrário, retorna apenas do produto informado.
     */
    ResumoVendasProdutoDTO getResumoVendas(Long produtoId);
}
