package com.nexdom.estoque.repository;

import com.nexdom.estoque.model.Produto;
import com.nexdom.estoque.model.TipoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAllByDataExclusaoIsNull();

    List<Produto> findByTipo(TipoProduto tipo);

    List<Produto> findByTipoAndDataExclusaoIsNull(TipoProduto tipo);
}
