# new-level-spring
API com Spring

# User

## User

```txt
POST   /users          // cria usuário
GET    /users/me       // usuário logado
GET    /users/:id      // busca usuário
PATCH  /users/:id      // atualiza usuário
DELETE /users/:id      // remove usuário
```

Estrutura:

```txt
user

- id
- clerkId
- name
- email
- level
- currentXp
- createdAt
```

# Task

## Task

```txt
POST   /tasks          // cria tarefa
GET    /tasks          // lista tarefas do usuário
GET    /tasks/:id      // busca tarefa
PATCH  /tasks/:id      // atualiza tarefa
PATCH  /tasks/:id/done // conclui tarefa
DELETE /tasks/:id      // remove tarefa
```

Estrutura:

```txt
task

- id
- title
- description
- userId
- user
- createdAt
- dueDate
- completedAt
- category
- difficulty
- updatedAt
```


# XP

```txt
GET /xp-bar            // retorna level e xp atual
```

Resposta:

```txt
{
  level,
  currentXp
}
```

# .env exemple

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