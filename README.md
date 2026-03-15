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

- **Base URL:** `http://localhost:8080`
- **Endpoints da API:** `http://localhost:8080/api/...`
- **Console H2:** `http://localhost:8080/h2-console`

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

O frontend sobe com **Vite**. Abra no navegador o endereço indicado no terminal (geralmente `http://localhost:5173`).

O frontend já está configurado para falar com a API em `http://localhost:8080/api`.

---

### 4. Resumo rápido

| Componente | Comando              | URL principal           |
|-----------|----------------------|-------------------------|
| Backend   | `cd backend` → `.\mvnw.cmd spring-boot:run` | http://localhost:8080   |
| Frontend  | `cd frontend` → `npm install` → `npm run dev` | http://localhost:5173   |

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

## Build para produção

**Backend (JAR):**

```bash
cd backend
.\mvnw.cmd clean package -DskipTests
```

O JAR ficará em `backend/target/`. Para executar:

```bash
java -jar target/estoque-api-0.0.1-SNAPSHOT.jar
```

**Frontend (estático):**

```bash
cd frontend
npm run build
```

Os arquivos estarão em `frontend/dist/`. Para servir localmente:

```bash
npm run preview
```

---

## Estrutura do projeto

```
desafio-nexdom/
├── backend/          # API Spring Boot (Java 21, H2, JPA)
│   ├── src/
│   └── pom.xml
├── frontend/         # Vue 3 + Vite + TypeScript
│   ├── src/
│   └── package.json
└── README.md
```

---

## Problemas comuns

- **Frontend não carrega dados:** confira se o backend está rodando em `http://localhost:8080` e se não há firewall/proxy bloqueando.
- **Porta 8080 em uso:** altere `server.port` em `backend/src/main/resources/application.properties` e a `baseURL` em `frontend/src/services/api.ts`.
- **Erro ao rodar Maven:** use o wrapper do projeto (`mvnw` / `mvnw.cmd`) em vez do Maven global.
