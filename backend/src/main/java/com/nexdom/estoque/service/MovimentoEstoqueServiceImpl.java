package com.nexdom.estoque.service;

import com.nexdom.estoque.model.MovimentoEstoque;
import com.nexdom.estoque.repository.MovimentoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimentoEstoqueServiceImpl implements MovimentoEstoqueService {

    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MovimentoEstoque> listarTodos() {
        return movimentoEstoqueRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovimentoEstoque> buscarPorId(Long id) {
        return movimentoEstoqueRepository.findById(id);
    }

    @Override
    @Transactional
    public MovimentoEstoque salvar(MovimentoEstoque movimento) {
        return movimentoEstoqueRepository.save(movimento);
    }
}
