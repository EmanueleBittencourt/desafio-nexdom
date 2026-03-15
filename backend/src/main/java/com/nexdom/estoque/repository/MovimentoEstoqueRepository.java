package com.nexdom.estoque.repository;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.TipoMovimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    @Query("SELECT m FROM MovimentoEstoque m JOIN FETCH m.produto ORDER BY m.dataVenda DESC")
    List<MovimentoEstoque> findAllWithProduto();

    List<MovimentoEstoque> findByProdutoIdAndTipo(Long produtoId, TipoMovimento tipo);

    @Query("SELECT m.produto.id, SUM(m.quantidadeMovimentada) FROM MovimentoEstoque m WHERE m.tipo = 'SAIDA' GROUP BY m.produto.id")
    List<Object[]> sumQuantidadeSaidaByProdutoId();

    @Query("SELECT COALESCE(SUM(m.quantidadeMovimentada), 0) FROM MovimentoEstoque m WHERE m.tipo = :tipo AND m.produto.id IN :produtoIds")
    Long sumQuantidadeByTipoAndProdutoIdIn(TipoMovimento tipo, List<Long> produtoIds);

    List<MovimentoEstoque> findByProdutoIdInAndTipo(List<Long> produtoIds, TipoMovimento tipo);
}
