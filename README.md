# TCC - Campus Central de Eventos UCSAL

Aplicativo mobile para divulgação de eventos e ligas acadêmicas da UCSAL.

- `backend/` - API REST em Java 21 + Spring Boot, com PostgreSQL
- `frontend/` - aplicativo em React Native (Expo) + TypeScript
- `postman/` - workspace do Postman para testar a API

## Pré-requisitos

- Java 21 (JDK)
- PostgreSQL
- Node.js (versão LTS) e npm
- App **Expo Go** no celular (Android ou iOS)

## Backend

A API lê as configurações do banco e do JWT de variáveis de ambiente.

| Variável | Descrição |
| --- | --- |
| `DB_URL` | URL JDBC do banco, ex.: `jdbc:postgresql://localhost:5432/campus` |
| `DB_USERNAME` | usuário do PostgreSQL |
| `DB_PASSWORD` | senha do PostgreSQL |
| `JWT_SECRET` | chave para assinar os tokens, em Base64 (mínimo de 32 bytes) |
| `CAMPUS_SEED_ENABLED` | `true` para criar os dados iniciais (opcional) |
| `SEED_INITIAL_PASSWORD` | senha dos usuários criados pela carga inicial (obrigatória se a carga estiver ativa) |

1. Crie o banco no PostgreSQL:

   ```sql
   CREATE DATABASE campus;
   ```

   As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`).

2. Defina as variáveis e suba a API (PowerShell, dentro de `backend/`):

   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/campus"
   $env:DB_USERNAME="postgres"
   $env:DB_PASSWORD="sua_senha"
   $env:JWT_SECRET=[Convert]::ToBase64String([byte[]](1..32 | ForEach-Object { Get-Random -Maximum 256 }))
   $env:CAMPUS_SEED_ENABLED="true"
   $env:SEED_INITIAL_PASSWORD="senha123"

   .\mvnw.cmd spring-boot:run
   ```

   No Linux/macOS use `export VAR=valor`, gere a chave com `openssl rand -base64 32` e rode `./mvnw spring-boot:run`.

   Como a chave acima é gerada a cada execução, os tokens antigos deixam de valer quando a API reinicia. Para manter os logins, gere a chave uma vez e reutilize o mesmo valor.

3. Teste: <http://localhost:8082/api/health> deve responder `API funcionando`.

Com a carga inicial ativa, são criados os usuários `admin@ucsal.edu.br`, `caex@ucsal.edu.br`, `estagio@ucsal.edu.br`, `carlos@ucsal.edu.br` (professor/coordenador) e `maria@ucsal.edu.br` (aluna), todos com a senha definida em `SEED_INITIAL_PASSWORD`. A carga só roda se o banco estiver vazio.

## Frontend

Dentro de `frontend/`:

```bash
npm install
npx expo start -c
```

Escaneie o QR code com o Expo Go (o celular precisa estar na mesma rede Wi-Fi do computador). No terminal do Expo, `w` abre a versão web e `a` abre o emulador Android.

### Modo mock x API

Sem a variável `EXPO_PUBLIC_API_URL` o app usa dados de exemplo e não precisa do backend. No login, qualquer usuário e senha funcionam: `admin` entra como administrador, `professor` como professor e os demais como aluno.

Para conectar na API, crie o arquivo `frontend/.env` a partir do `.env.example`:

```
EXPO_PUBLIC_API_URL=http://192.168.0.10:8082
```

- Use o **IP do computador na rede** (veja com `ipconfig`), não `localhost`: no celular, `localhost` é o próprio celular.
- Libere a porta 8082 no firewall do Windows, se necessário.
- Depois de alterar o `.env`, reinicie o Expo com `npx expo start -c`.
