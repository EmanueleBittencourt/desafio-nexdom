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
}
