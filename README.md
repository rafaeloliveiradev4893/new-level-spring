# new-level-spring

API backend do **New Level**, um gerenciador de tarefas e eventos com gamificação: ao concluir tarefas e eventos o usuário ganha XP e sobe de nível, de acordo com a dificuldade.

**Stack:** Java, Spring Boot, Spring Security (OAuth2 Resource Server), Spring Data JPA/Hibernate, PostgreSQL, Lombok, Auth0.

## Como funciona

- A autenticação é feita pelo **Auth0**. O front faz o login no Auth0 e envia o token JWT no header `Authorization: Bearer <token>`. A API apenas valida o token (issuer e audience) e usa o campo `sub` como identificador do usuário (`auth0Id`).
- Todas as rotas `/api/**` exigem token válido, e cada usuário só acessa os próprios dados.
- Concluir uma tarefa ou um evento soma XP ao usuário conforme a `difficulty`.

## Setup

### Pré-requisitos

- JDK (mesma versão definida no `pom.xml`)
- Docker e Docker Compose
- Uma conta Auth0 com uma API cadastrada (para obter o issuer e o audience)

### 1. Variáveis de ambiente

Copie o arquivo de exemplo e preencha os valores:

```bash
cp .env.example .env
```

```env
# Database
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_NAME=new_level
DB_PORT=5432

# Auth0
AUTH_ISSUER=https://your-tenant.us.auth0.com/
AUTH0_AUDIENCE=https://your-api-identifier
```

| Variável | Descrição |
|---|---|
| `DB_USER` / `DB_PASSWORD` | Credenciais do PostgreSQL |
| `DB_NAME` | Nome do banco (`new_level`) |
| `DB_PORT` | Porta do PostgreSQL (`5432`) |
| `AUTH_ISSUER` | URL do tenant Auth0, **com a barra no final** |
| `AUTH0_AUDIENCE` | Identifier da API cadastrada no Auth0 |

### 2. Subir o banco com Docker Compose

```bash
docker compose up -d
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`.

## Testes

```bash
./mvnw test
```

## Deploy

1. Gere o artefato: `./mvnw clean package`
2. Defina em produção as mesmas variáveis do `.env` (`DB_*`, `AUTH_ISSUER`, `AUTH0_AUDIENCE`) como variáveis de ambiente do servidor, sem commitar o `.env`.
3. Execute o jar gerado em `target/`: `java -jar target/<nome-do-jar>.jar`

## Rotas implementadas

Todas exigem `Authorization: Bearer <token>`. Datas em formato ISO, sem fuso: `LocalDate` como `2026-10-18` e `LocalDateTime` como `2026-10-15T14:00:00`.

### User

```txt
POST   /api/user          // cria usuário
GET    /api/user/me       // usuário logado
PATCH  /api/user          // atualiza usuário
DELETE /api/user          // remove usuário
```

Campos: `auth0Id`, `name`, `level`, `currentXp`, `createdAt`, `updatedAt`.

### Task

```txt
GET    /api/task                 // lista tarefas do usuário
GET    /api/task/:taskId         // busca tarefa
POST   /api/task                 // cria tarefa (aceita subtarefas)
PUT    /api/task/:taskId         // atualiza toda a tarefa
PATCH  /api/task/:taskId         // atualiza parcialmente a tarefa
PATCH  /api/task/:taskId/done    // conclui tarefa e soma XP
DELETE /api/task/:taskId         // remove tarefa
```

**TaskRequestDTO** (POST e PUT)

```json
{
  "title": "Limpar casa",
  "description": "Faxina geral do fim de semana",
  "dueDate": "2026-10-18",
  "category": "HOME",
  "difficulty": "EASY",
  "subtasks": [
    { "title": "Cozinha" },
    { "title": "Banheiro" },
    { "title": "Sala" }
  ]
}
```

`subtasks` é opcional e só é considerado na criação. O PATCH aceita os mesmos campos (exceto `subtasks`), todos opcionais.

**TaskResponseDTO**: `id`, `title`, `description`, `dueDate`, `completedAt`, `createdAt`, `updatedAt`, `category`, `difficulty`, `isCompleted`, `subtasks` (lista de `SubtaskResponseDTO`).

### Subtask

```txt
POST   /api/task/:taskId/subtasks                  // cria subtarefa
PATCH  /api/task/:taskId/subtasks/:subtaskId       // atualiza o título
PATCH  /api/task/:taskId/subtasks/:subtaskId/done  // conclui subtarefa
DELETE /api/task/:taskId/subtasks/:subtaskId       // remove subtarefa
```

**SubtaskRequestDTO / SubtaskUpdateDTO**: `{ "title": "Quarto" }`

**SubtaskResponseDTO**: `id`, `title`, `isCompleted`, `completedAt`.

Todas as rotas de subtarefa devolvem a tarefa completa (`TaskResponseDTO`).

### Event

Um evento é como uma tarefa, mas com data e hora de início e término.

```txt
GET    /api/event                 // lista eventos do usuário
GET    /api/event/period          // lista eventos de um período (?start=...&end=...)
GET    /api/event/:eventId        // busca evento
POST   /api/event                 // cria evento
PUT    /api/event/:eventId        // atualiza todo o evento
PATCH  /api/event/:eventId        // atualiza parcialmente o evento
PATCH  /api/event/:eventId/done   // conclui evento e soma XP
DELETE /api/event/:eventId        // remove evento
```

Exemplo do período: `GET /api/event/period?start=2026-10-01T00:00:00&end=2026-10-31T23:59:59` (retorna eventos que se sobrepõem ao intervalo).

**EventRequestDTO** (POST e PUT)

```json
{
  "title": "Reunião com cliente",
  "description": "Apresentar a proposta do site de captação de leads",
  "startDate": "2026-10-15T14:00:00",
  "endDate": "2026-10-15T15:30:00",
  "category": "WORK",
  "difficulty": "MEDIUM"
}
```

A data de término não pode ser anterior à de início. O PATCH aceita os mesmos campos, todos opcionais.

**EventResponseDTO**: `id`, `title`, `description`, `startDate`, `endDate`, `category`, `difficulty`, `isCompleted`, `completedAt`, `createdAt`, `updatedAt`.

> Os valores válidos de `category` e `difficulty` são os dos enums `Category` e `Difficulty` em `types/`.

## Ainda não implementado

Itens planejados que **não estão disponíveis** na API hoje:

- Endpoint dedicado de XP (o XP é somado automaticamente ao concluir tarefas e eventos, mas não há rota para consultá-lo ou alterá-lo)
- Desfazer a conclusão de uma tarefa, subtarefa ou evento
- Regra entre subtarefas e tarefa (exigir subtarefas concluídas, ou concluir a tarefa automaticamente)