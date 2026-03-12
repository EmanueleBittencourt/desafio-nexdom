package com.nexdom.estoque.service;

import com.nexdom.estoque.model.MovimentoEstoque;

import java.util.List;
import java.util.Optional;

public interface MovimentoEstoqueService {

    List<MovimentoEstoque> listarTodos();

    Optional<MovimentoEstoque> buscarPorId(Long id);

    MovimentoEstoque salvar(MovimentoEstoque movimento);
}
