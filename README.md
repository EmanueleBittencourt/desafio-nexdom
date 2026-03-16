# Desafio NEXDOM — Sistema de Controle de Estoque

Sistema fullstack de controle de estoque: **backend** em Java (Spring Boot) e **frontend** em Vue 3 (Vite + TypeScript).

---

## Pré-requisitos

- **Java 21** (JDK)
- **Node.js** 20.19+ ou 22.12+ (recomendado LTS)
- **Maven** (opcional; o projeto usa o wrapper `mvnw`)

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

- **Base URL da API:** `http://localhost:8080`
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

#### Dados iniciais (`import.sql`)

Foi incluído no projeto o arquivo `backend/src/main/resources/import.sql`, que é executado automaticamente ao subir a aplicação. 
Ele insere produtos e movimentos de exemplo para uma melhor visualização da interface.

Isso depende da configuração em `backend/src/main/resources/application.properties` (JPA / Hibernate):

- `spring.jpa.hibernate.ddl-auto=create-drop` — recria o banco a cada execução e, ao encerrar a aplicação, apaga tudo.
- `spring.jpa.defer-datasource-initialization=true` — faz o Hibernate esperar a inicialização do datasource antes de criar o schema, permitindo que o `import.sql` rode após as tabelas existirem.
- `spring.sql.init.mode=always` — garante que o script SQL de inicialização seja executado sempre que a aplicação sobe.

**Se quiser manter os dados entre execuções (não dropar o banco ao fechar):**

1. Abra `backend/src/main/resources/application.properties`.
2. Troque a linha:
   ```properties
   spring.jpa.hibernate.ddl-auto=create-drop
   ```
   por uma das opções:
   - `spring.jpa.hibernate.ddl-auto=update` — mantém os dados e atualiza o schema se houver mudanças nas entidades (recomendado para desenvolvimento).
   - `spring.jpa.hibernate.ddl-auto=validate` — só valida o schema; não altera nem recria (use se o banco já estiver criado e você não quiser alterações).
3. (Opcional) Se não quiser que o `import.sql` rode em toda subida (evitar duplicar dados), altere:
   ```properties
   spring.sql.init.mode=always
   ```
   para:
   ```properties
   spring.sql.init.mode=never
   ```
   Assim o script de importação deixa de ser executado automaticamente.

  ---