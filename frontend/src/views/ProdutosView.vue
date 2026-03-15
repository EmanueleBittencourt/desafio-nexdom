<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { Produto, TipoProdutoOption } from '@/types'
import api from '@/services/api'
import Swal from 'sweetalert2'

const produtos = ref<Produto[]>([])
const tiposProduto = ref<TipoProdutoOption[]>([])
const loading = ref(false)
const error = ref('')
/** Indica se o backend está acessível (GET /api/health ou sucesso ao carregar produtos) */
const backendConectado = ref<boolean | null>(null)

/** Fallback quando a API de tipos não está disponível (ex.: CORS ou backend fora) */
const FALLBACK_TIPOS: TipoProdutoOption[] = [
  { value: 'ELETRONICO', label: 'Eletrônico' },
  { value: 'ELETRODOMESTICO', label: 'Eletrodoméstico' },
  { value: 'MOVEL', label: 'Móvel' },
]

const form = ref({
  codigo: '',
  descricao: '',
  tipo: 'ELETRONICO' as string,
  valorFornecedor: 0,
  quantidadeEstoque: 0,
})

async function carregarTiposProduto() {
  try {
    const { data } = await api.get<TipoProdutoOption[]>('/produtos/tipos')
    const list = Array.isArray(data) ? data : []
    tiposProduto.value = list.length > 0 ? list : FALLBACK_TIPOS
    const primeiro = tiposProduto.value[0]
    if (primeiro && !tiposProduto.value.some((t) => t.value === form.value.tipo)) {
      form.value.tipo = primeiro.value
    }
  } catch {
    tiposProduto.value = FALLBACK_TIPOS
  }
}

const showModal = ref(false)
const showModalNovoProduto = ref(false)
const produtoEmEdicao = ref<Produto | null>(null)
const modalTipo = ref<'ENTRADA' | 'SAIDA'>('ENTRADA')
const produtoSelecionado = ref<Produto | null>(null)
const movimentoForm = ref({
  quantidade: 1,
  valorVenda: 0,
})


async function verificarBackend() {
  try {
    await api.get<{ status: string }>('/health')
    backendConectado.value = true
    return true
  } catch {
    backendConectado.value = false
    return false
  }
}

function asProdutos(data: unknown): Produto[] {
  if (Array.isArray(data)) return data as Produto[]
  if (data && typeof data === 'object' && 'content' in data && Array.isArray((data as { content: unknown }).content)) {
    return (data as { content: Produto[] }).content
  }
  return []
}

async function carregarProdutos() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<Produto[]>('/produtos')
    produtos.value = asProdutos(data)
    backendConectado.value = true
  } catch (err: unknown) {
    backendConectado.value = false
    const msg =
      err && typeof err === 'object' && 'code' in err && (err as { code?: string }).code === 'ERR_NETWORK'
        ? 'Backend inacessível. Verifique se está rodando em http://localhost:8080 e se o CORS está habilitado.'
        : err && typeof err === 'object' && 'response' in err && (err as { response?: { status?: number } }).response?.status === 404
          ? 'Endpoint /api/produtos não encontrado. Confira a URL do backend.'
          : 'Não foi possível carregar os produtos. Verifique se o servidor está rodando em http://localhost:8080.'
    error.value = msg
  } finally {
    loading.value = false
  }
}

async function salvarProduto(e: Event) {
  e.preventDefault()
  if (loading.value) return
  if (!form.value.codigo.trim() || !form.value.descricao.trim()) return
  const payload = {
    codigo: form.value.codigo.trim(),
    descricao: form.value.descricao.trim(),
    tipo: form.value.tipo,
    valorFornecedor: Number(form.value.valorFornecedor) || 0,
    quantidadeEstoque: Number(form.value.quantidadeEstoque) || 0,
  }
  loading.value = true
  error.value = ''
  try {
    if (produtoEmEdicao.value) {
      await api.put<Produto>(`/produtos/${produtoEmEdicao.value.id}`, payload)
      error.value = ''
      await carregarProdutos()
      fecharModalNovoProduto()
    } else {
      await api.post<Produto>('/produtos', payload)
      await carregarProdutos()
      fecharModalNovoProduto()
    }
  } catch {
    error.value = produtoEmEdicao.value ? 'Erro ao atualizar produto.' : 'Erro ao cadastrar produto.'
  } finally {
    loading.value = false
  }
}

function editarProduto(produto: Produto) {
  if (tiposProduto.value.length === 0) {
    carregarTiposProduto()
  }
  produtoEmEdicao.value = produto
  form.value = {
    codigo: produto.codigo,
    descricao: produto.descricao,
    tipo: produto.tipo,
    valorFornecedor: produto.valorFornecedor ?? 0,
    quantidadeEstoque: produto.quantidadeEstoque ?? 0,
  }
  showModalNovoProduto.value = true
}

async function excluirProduto(produto: Produto) {
  const result = await Swal.fire({
    title: 'Excluir produto?',
    html: `Tem certeza que deseja excluir <strong>${produto.descricao}</strong>?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#03161a',
    cancelButtonColor: '#6b7280',
    confirmButtonText: 'Sim, excluir',
    cancelButtonText: 'Cancelar',
  })
  if (!result.isConfirmed) return
  loading.value = true
  error.value = ''
  try {
    await api.delete(`/produtos/${produto.id}`)
    await carregarProdutos()
    await Swal.fire({
      title: 'Excluído',
      text: 'Produto excluído com sucesso.',
      icon: 'success',
      confirmButtonColor: '#03161a',
    })
  } catch (err: unknown) {
    const res = err && typeof err === 'object' && 'response' in err
      ? (err as { response?: { status?: number; data?: { message?: string } } }).response
      : null
    const msg = res?.status === 400
      ? res?.data?.message ?? 'Produto só pode ser excluído quando a quantidade em estoque for zero.'
      : 'Não foi possível excluir o produto.'
    await Swal.fire({
      title: 'Erro',
      text: msg,
      icon: 'error',
      confirmButtonColor: '#03161a',
    })
  } finally {
    loading.value = false
  }
}

function abrirModalNovoProduto() {
  if (tiposProduto.value.length === 0) {
    carregarTiposProduto()
  }
  showModalNovoProduto.value = true
}

function fecharModalNovoProduto() {
  showModalNovoProduto.value = false
  produtoEmEdicao.value = null
  form.value = {
    codigo: '',
    descricao: '',
    tipo: tiposProduto.value[0]?.value ?? 'ELETRONICO',
    valorFornecedor: 0,
    quantidadeEstoque: 0,
  }
}

function abrirModal(produto: Produto, tipo: 'ENTRADA' | 'SAIDA') {
  produtoSelecionado.value = produto
  modalTipo.value = tipo
  movimentoForm.value = {
    quantidade: tipo === 'ENTRADA' ? 1 : Math.min(1, produto.quantidadeEstoque),
    valorVenda: 0,
  }
  showModal.value = true
}

function fecharModal() {
  showModal.value = false
  produtoSelecionado.value = null
}

async function registrarMovimento(e: Event) {
  e.preventDefault()
  const produto = produtoSelecionado.value
  if (!produto) return
  const qtd = Number(movimentoForm.value.quantidade) || 0
  if (qtd <= 0) return
  if (modalTipo.value === 'SAIDA' && qtd > produto.quantidadeEstoque) {
    error.value = 'Quantidade maior que o estoque disponível.'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await api.post('/movimentos', {
      produto: { id: produto.id },
      tipo: modalTipo.value,
      valorVenda: modalTipo.value === 'SAIDA' ? Number(movimentoForm.value.valorVenda) || 0 : 0,
      dataVenda: new Date().toISOString(),
      quantidadeMovimentada: qtd,
    })
    fecharModal()
    await carregarProdutos()
  } catch (err: unknown) {
    const msg = err && typeof err === 'object' && 'response' in err
      ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
      : null
    error.value = msg || 'Erro ao registrar movimentação.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await verificarBackend()
  carregarTiposProduto()
  await carregarProdutos()
})
</script>

<template>
  <div class="produtos-view">
    <header class="page-header">
      <h1>Produtos</h1>
      <button type="button" class="btn btn-primary btn-criar" @click="abrirModalNovoProduto">
        Criar
      </button>
    </header>

    <section class="card table-card">
      <div v-if="loading && produtos.length === 0" class="loading">
        Carregando…
      </div>
      <div v-else-if="produtos.length === 0" class="empty">
        Nenhum produto cadastrado.
      </div>
      <div v-else class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Descrição</th>
              <th>Tipo</th>
              <th>Estoque</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in produtos" :key="p.id">
              <td data-label="Código">{{ p.codigo }}</td>
              <td data-label="Descrição">{{ p.descricao }}</td>
              <td data-label="Tipo">{{ p.tipo }}</td>
              <td data-label="Estoque">{{ p.quantidadeEstoque }}</td>
              <td data-label="Ações" class="cell-actions">
                <button type="button" class="btn btn-sm btn-entrada" :disabled="loading"
                  @click="abrirModal(p, 'ENTRADA')">
                  Entrada
                </button>
                <span
                  class="btn-tooltip"
                  :class="{ 'btn-tooltip--disabled': (p.quantidadeEstoque ?? 0) === 0 }"
                >
                  <button
                    type="button"
                    class="btn btn-sm btn-saida"
                    :disabled="loading || p.quantidadeEstoque === 0"
                    @click="abrirModal(p, 'SAIDA')"
                  >
                    Saída
                  </button>
                  <span class="btn-tooltip__text" aria-hidden="true">
                    Só é possível registrar saída quando houver quantidade em estoque.
                  </span>
                </span>
                <button type="button" class="btn btn-sm btn-editar" :disabled="loading || p.quantidadeEstoque === 0"
                  @click="editarProduto(p)">
                  Editar
                </button>

                <span
                  class="btn-tooltip"
                  :class="{ 'btn-tooltip--disabled': (p.quantidadeEstoque ?? 0) !== 0 }"
                >
                  <button
                    type="button"
                    class="btn btn-sm btn-excluir btn-icon"
                    :disabled="loading || (p.quantidadeEstoque ?? 0) !== 0"
                    aria-label="Excluir"
                    @click="excluirProduto(p)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <polyline points="3 6 5 6 21 6" />
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                      <line x1="10" y1="11" x2="10" y2="17" />
                      <line x1="14" y1="11" x2="14" y2="17" />
                    </svg>
                  </button>
                  <span class="btn-tooltip__text" aria-hidden="true"
                    >Só é possível excluir quando o estoque for zero.</span
                  >
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <Teleport to="body">
      <div v-if="showModal" class="modal-backdrop" @click.self="fecharModal">
        <div class="modal" role="dialog" aria-labelledby="modal-title">
          <div class="modal-header">
            <h2 id="modal-title">
              {{ modalTipo === 'ENTRADA' ? 'Entrada' : 'Saída' }} de estoque
              <span v-if="produtoSelecionado" class="produto-nome">{{ produtoSelecionado.descricao }}</span>
            </h2>
            <button type="button" class="btn-close" aria-label="Fechar" @click="fecharModal">
              ×
            </button>
          </div>
          <form @submit="registrarMovimento" class="modal-form">
            <div class="field">
              <label for="mov-qtd">Quantidade</label>
              <input id="mov-qtd" v-model.number="movimentoForm.quantidade" type="number" :min="1"
                :max="modalTipo === 'SAIDA' && produtoSelecionado ? produtoSelecionado.quantidadeEstoque : undefined"
                required />
              <span v-if="modalTipo === 'SAIDA' && produtoSelecionado" class="hint">
                Disponível: {{ produtoSelecionado.quantidadeEstoque }}
              </span>
            </div>
            <div v-if="modalTipo === 'SAIDA'" class="field">
              <label for="mov-valor">Valor de venda - unitário (R$)</label>
              <input id="mov-valor" v-model.number="movimentoForm.valorVenda" type="number" min="0" step="0.01"
                placeholder="0,00" />
            </div>
            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" @click="fecharModal">
                Cancelar
              </button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                {{ loading ? 'Salvando…' : 'Confirmar' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <div v-if="showModalNovoProduto" class="modal-backdrop" @click.self="fecharModalNovoProduto">
      <div class="modal modal-novo-produto" role="dialog" :aria-labelledby="produtoEmEdicao ? 'modal-editar-title' : 'modal-novo-produto-title'">
        <div class="modal-header">
          <h2 :id="produtoEmEdicao ? 'modal-editar-title' : 'modal-novo-produto-title'">
            {{ produtoEmEdicao ? 'Editar produto' : 'Novo produto' }}
          </h2>
          <button type="button" class="btn-close" aria-label="Fechar" @click="fecharModalNovoProduto">
            ×
          </button>
        </div>
        <form @submit.prevent="salvarProduto" class="modal-form form-grid-modal">
          <div class="field field-full">
            <label for="descricao">Nome do Produto</label>
            <input id="descricao" v-model.trim="form.descricao" type="text" required placeholder="Nome do produto" />
          </div>
          <div class="field">
            <label for="codigo">Código</label>
            <input id="codigo" v-model.trim="form.codigo" type="text" required placeholder="Ex: PROD-001" />
          </div>
          <div class="field">
            <label for="tipo">Tipo</label>
            <select id="tipo" v-model="form.tipo">
              <option v-for="t in tiposProduto" :key="t.value" :value="t.value">
                {{ t.label }}
              </option>
            </select>
          </div>
          <div class="field">
            <label for="valorFornecedor">Valor fornecedor (R$)</label>
            <input id="valorFornecedor" v-model.number="form.valorFornecedor" type="number" min="0" step="0.01"
              placeholder="0,00" />
          </div>
          <div class="field">
            <label for="quantidadeEstoque">{{ produtoEmEdicao ? 'Estoque (somente leitura)' : 'Estoque inicial' }}</label>
            <input
              id="quantidadeEstoque"
              v-model.number="form.quantidadeEstoque"
              type="number"
              min="0"
              placeholder="0"
              :disabled="!!produtoEmEdicao"
            />
          </div>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="fecharModalNovoProduto">
              Cancelar
            </button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? 'Salvando…' : (produtoEmEdicao ? 'Salvar' : 'Cadastrar') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.produtos-view {
  --card-bg: #e5e1e6;
  --card-border: #d4d0d5;
  --text: #1f2937;
  --text-muted: #6b7280;
  --primary: var(--palette-verde-escuro);
  --primary-hover: var(--palette-verde-medio);
  --entrada: var(--palette-verde-claro);
  --entrada-hover: #5ab885;
  --saida: var(--palette-laranja);
  --saida-hover: #e6722f;
  --error: #b91c1c;
  --radius: 12px;
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  color: var(--text);
}

.btn-criar {
  margin-left: auto;
  margin-top: 10px;
}

.modal-novo-produto {
  max-width: min(600px, calc(100vw - 32px));
  width: 100%;
  background: var(--card-bg);
  max-height: calc(100vh - 32px);
  overflow-y: auto;
  overflow-x: hidden;
  overscroll-behavior: contain;
}

.modal-novo-produto .modal-header {
  flex-shrink: 0;
}

.modal-novo-produto .modal-form {
  padding: 16px 20px 20px;
}

.modal-novo-produto .modal-form .field {
  margin-bottom: 0;
}

.form-grid-modal {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 20px;
}

.form-grid-modal .field-full {
  grid-column: 1 / -1;
}

.form-grid-modal .modal-actions {
  grid-column: 1 / -1;
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid var(--card-border);
}

@media (max-width: 480px) {
  .modal-novo-produto {
    max-width: calc(100vw - 16px);
    max-height: calc(100vh - 16px);
  }

  .form-grid-modal {
    grid-template-columns: 1fr;
  }

  .form-grid-modal .field-full {
    grid-column: 1;
  }
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  align-items: end;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-actions {
  grid-column: 1 / -1;
}

.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
}

.field input,
.field select {
  padding: 8px 12px;
  border: 1px solid var(--card-border);
  border-radius: 8px;
  font-size: 14px;
  background: var(--card-bg);
  color: var(--text);
}

.field input:focus,
.field select:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(3, 22, 26, 0.15);
}

.hint {
  font-size: 12px;
  color: var(--text-muted);
}

.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background 0.15s, opacity 0.15s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-hover);
}

.btn-secondary {
  background: var(--card-border);
  color: var(--text);
}

.btn-secondary:hover:not(:disabled) {
  background: #d1d5db;
}

.btn-sm {
  padding: 6px 10px;
  font-size: 13px;
}

.btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
}

.btn-icon svg {
  display: block;
}

.btn-entrada {
  background: var(--entrada);
  color: #fff;
}

.btn-entrada:hover:not(:disabled) {
  background: var(--entrada-hover);
}

.btn-saida {
  background: var(--saida);
  color: #fff;
}

.btn-excluir {
  background: var(--error);
  color: #fff;
}

.btn-saida:hover:not(:disabled) {
  background: var(--saida-hover);
}

.btn-tooltip {
  display: inline-block;
  position: relative;
}

.btn-tooltip__text {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  padding: 6px 10px;
  background: var(--primary);
  color: #fff;
  font-size: 12px;
  white-space: nowrap;
  border-radius: 6px;
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.2s ease, visibility 0.2s ease;
  z-index: 20;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.btn-tooltip__text::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  margin-left: -4px;
  border: 4px solid transparent;
  border-top-color: #1f2937;
}

/* Quando desabilitado, o botão não captura o hover; o span recebe e mostra o tooltip */
/* Quando desabilitado, o botão não captura o hover; o span recebe e mostra o tooltip */
/* Quando desabilitado, o botão não captura o hover; o span recebe e mostra o tooltip */
.btn-tooltip--disabled .btn {
  pointer-events: none;
}

.btn-tooltip--disabled:hover .btn-tooltip__text {
  opacity: 1;
  visibility: visible;
}

.cell-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.backend-online {
  color: var(--color-success, #059669);
  font-size: 14px;
  margin-bottom: 8px;
}

.backend-offline {
  color: var(--error);
  font-size: 14px;
  margin-bottom: 8px;
}

.backend-offline code {
  background: rgba(0, 0, 0, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85em;
}

.modal-backdrop {
  --card-bg: #e5e1e6;
  --card-border: #d4d0d5;
  --primary: var(--palette-verde-escuro);
  --primary-hover: var(--palette-verde-medio);
  --radius: 12px;
  --shadow-lg: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  --text: #1f2937;
  --text-muted: #6b7280;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

.modal {
  background: var(--card-bg);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--card-border);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.produto-nome {
  display: block;
  font-size: 0.85rem;
  font-weight: 400;
  margin-top: 0.2rem;
}

.btn-close {
  width: 2rem;
  height: 2rem;
  border: none;
  background: transparent;
  font-size: 1.5rem;
  line-height: 1;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: 6px;
  flex-shrink: 0;
}

.btn-close:hover {
  background: #f3f4f6;
  color: var(--text);
}

.modal-form {
  padding: 1.5rem;
}

.modal-form .field {
  margin-bottom: 1rem;
}

.modal-form .field:last-of-type {
  margin-bottom: 1.25rem;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}
</style>
