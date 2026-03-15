/**
 * Interface que representa um produto no sistema de estoque.
 */
export interface Produto {
  id: number;
  codigo: string;
  descricao: string;
  tipo: string;
  valorFornecedor: number;
  quantidadeEstoque: number;
}

/**
 * Opção de tipo de produto retornada pela API (value + label).
 */
export interface TipoProdutoOption {
  value: string;
  label: string;
}

/**
 * Interface que representa uma movimentação de estoque (resposta da API).
 */
export interface Movimentacao {
  id: number;
  produto: { id: number; codigo: string; descricao: string } | null;
  tipo: string;
  valorVenda: number;
  quantidadeMovimentada: number;
  dataVenda: string; // ISO 8601 (ex: "2025-03-14T12:00:00")
}
