package com.nexdom.estoque.repository;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.model.TipoMovimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    List<MovimentoEstoque> findByProdutoIdAndTipo(Long produtoId, TipoMovimento tipo);
}
