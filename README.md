# new-level-spring

API backend do **New Level**, um gerenciador de tarefas e eventos com gamificação: ao concluir tarefas e eventos o usuário ganha XP e sobe de nível, de acordo com a dificuldade.

**Stack:** Java 21, Spring Boot, Spring Security (OAuth2 Resource Server), Spring Data JPA/Hibernate, PostgreSQL, Flyway, Lombok e Auth0.

## Como funciona

- A autenticação é feita pelo **Auth0**. O front faz o login no Auth0 e envia o token JWT no header `Authorization: Bearer <token>`. A API apenas valida o token (issuer e audience) e usa o campo `sub` como identificador do usuário (`auth0Id`).
- Todas as rotas `/api/**` exigem token válido, e cada usuário só acessa os próprios dados.
- Concluir uma tarefa ou um evento soma XP ao atributo associado à categoria, conforme a dificuldade: `EASY` = 10 XP, `MEDIUM` = 20 XP e `HARD` = 40 XP. A cada 100 XP acumulados em um atributo, ele sobe um nível; o nível geral do usuário é atualizado a partir dos atributos.

## Setup

### Pré-requisitos

- JDK 21
- Docker com Docker Compose (para subir o PostgreSQL local)
- Uma conta Auth0 com uma API cadastrada (para obter o issuer e o audience)

### 1. Variáveis de ambiente

Copie o template existente e preencha os valores:

```bash
cp .env.exemple .env
```

```env
# Database
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_NAME=new_level
DB_PORT=5433

# Auth0
AUTH_ISSUER=https://your-tenant.us.auth0.com/
AUTH0_AUDIENCE=https://your-api-identifier
```

| Variável | Descrição |
|---|---|
| `DB_USER` / `DB_PASSWORD` | Credenciais do PostgreSQL |
| `DB_NAME` | Nome do banco (`new_level`) |
| `DB_PORT` | Porta local do PostgreSQL (`5433`, mapeada para a porta `5432` do container pelo Compose) |
| `AUTH_ISSUER` | URL do tenant Auth0, **com a barra no final** |
| `AUTH0_AUDIENCE` | Identifier da API cadastrada no Auth0 |

### 2. Subir o banco com Docker Compose

O Compose publica o PostgreSQL em `localhost:5433`.

```bash
docker compose up -d
```

### 3. Rodar a aplicação

O perfil padrão é `prod`; para usar o PostgreSQL local e o arquivo `.env`, inicie explicitamente com o perfil `dev`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A API fica disponível em `http://localhost:8080`.
No perfil `dev`, a origem CORS permitida por padrão é `http://localhost:4200`; use `CORS_ALLOWED_ORIGIN` para alterá-la.

## Testes

```bash
./mvnw test
```

## Deploy

1. Gere o artefato: `./mvnw clean package`
2. Em produção, configure no ambiente do servidor `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`, `AUTH_ISSUER` e `AUTH0_AUDIENCE`. A conexão PostgreSQL de produção está configurada no perfil `prod`; não use as variáveis locais `DB_*` para essa conexão. Opcionalmente, defina `CORS_ALLOWED_ORIGIN` e `PORT`. Não versione credenciais nem arquivos `.env`.
3. Execute o JAR gerado em `target/`: `java -jar target/<nome-do-jar>.jar`. O perfil padrão é `prod`; se necessário, defina `SPRING_PROFILES_ACTIVE`.

## API

Todas exigem `Authorization: Bearer <token>`. Datas em formato ISO, sem fuso: `LocalDate` como `2026-10-18` e `LocalDateTime` como `2026-10-15T14:00:00`.

### User

```txt
POST   /api/user          // cria usuário
GET    /api/user/me       // usuário logado
PATCH  /api/user          // atualiza usuário
DELETE /api/user          // remove usuário
```

`POST` e `PATCH` não recebem corpo: o nome é obtido da claim `https://newlevelauth.com/name` do token. A resposta contém `name`, `level`, `atributos`, `atributosXp` e `createdAt`.
Crie o perfil com `POST /api/user` antes de criar tarefas ou eventos para esse usuário.

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
  "category": "ROUTINE",
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

Ao concluir uma tarefa por `/done`, todas as subtarefas pendentes também são marcadas como concluídas. Se todas as subtarefas forem concluídas individualmente, a tarefa é concluída automaticamente e concede XP.

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
PATCH  /api/event/:eventId/start  // registra o início real do evento
PATCH  /api/event/:eventId/complete // registra o término real e conclui o evento
DELETE /api/event/:eventId        // remove evento
```

Exemplo do período: `GET /api/event/period?start=2026-10-01T00:00:00&end=2026-10-31T23:59:59` (retorna eventos que se sobrepõem ao intervalo).

Ao criar ou atualizar um evento, a data de início não pode estar em um dia passado e a data de término não pode ser anterior à de início. `/done` conclui o evento imediatamente; `/start` e `/complete` registram, respectivamente, o início e o término reais.

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

No POST e no PUT, `startDate` e `endDate` são obrigatórios. No PATCH, todos os campos são opcionais, mas as datas resultantes precisam continuar válidas.

**EventResponseDTO**: `id`, `title`, `description`, `startDate`, `endDate`, `category`, `difficulty`, `isCompleted`, `completedAt`, `createdAt`, `updatedAt`, `actualStartAt`, `actualEndAt`.

Valores válidos:

- `category`: `HEALTH`, `FITNESS`, `NUTRITION`, `SLEEP`, `MENTAL_HEALTH`, `STUDY`, `WORK`, `FINANCE`, `PRODUCTIVITY`, `PERSONAL_GROWTH`, `SOCIAL`, `SPIRITUALITY`, `HOBBIES`, `SELF_CARE`, `ROUTINE`, `OTHER`, `UNDEFINED`.
- `difficulty`: `EASY`, `MEDIUM`, `HARD`.
