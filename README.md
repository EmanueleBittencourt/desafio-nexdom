# Desafio NEXDOM — Sistema de Controle de Estoque

Sistema fullstack de controle de estoque: **backend** em Java (Spring Boot) e **frontend** em Vue 3 (Vite + TypeScript).

---

## Pré-requisitos

- **Java 21** (JDK)
- **Node.js** 20.19+ ou 22.12+ (recomendado LTS)
- **Maven** (opcional; o projeto usa o wrapper `mvnw`)

Para conferir as versões:

```bash
java -version
node -v
npm -v
```

---

## Passo a passo para rodar o projeto

### 1. Clonar o repositório (se ainda não tiver)

```bash
git clone <url-do-repositorio>
cd desafio-nexdom
```

---

### 2. Subir o backend (API)

O backend usa **Spring Boot**, banco **H2** em arquivo e roda na porta **8080**.

**No terminal, na pasta do projeto:**

```bash
cd backend
```

**Windows (PowerShell ou CMD):**

```bash
.\mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

Aguarde até aparecer algo como: `Started EstoqueApiApplication`. A API estará em:

- **Base URL da API:** `http://localhost:8080` (abrir só a raiz no navegador dá 404 — é normal; a API não tem página em `/`)
- **Endpoints:** `http://localhost:8080/api/...`
- **Console H2:** `http://localhost:8080/h2-console`

Para usar o sistema no navegador, acesse o **frontend** (passo 3); a interface fica em `http://localhost:5173`.

**Banco H2 (para acessar pelo console):**

- JDBC URL: `jdbc:h2:file:./data/estoque_db`
- Usuário: `sa`
- Senha: *(deixe em branco)*

---

### 3. Subir o frontend

Deixe o backend rodando e abra **outro terminal**. Na raiz do projeto:

```bash
cd frontend
npm install
npm run dev
```

O frontend sobe com **Vite**. Abra no navegador o endereço indicado no terminal (`http://localhost:5173`).

O frontend já está configurado para falar com a API em `http://localhost:8080/api`.

---

**Ordem recomendada:** iniciar o backend primeiro e depois o frontend.

---

## Testes

**Backend (na pasta `backend`):**

```bash
.\mvnw.cmd test
```

**Frontend (na pasta `frontend`):**

```bash
npm run test:unit
```

---
