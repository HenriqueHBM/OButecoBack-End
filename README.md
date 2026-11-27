# OButeco— Back-End 
Sistema de gestão de estoque para bares. API REST em Spring Boot 4 + PostgreSQL, consumida pelo front-end Angular [OButecoFront-End] (https://github.com/HenriqueHBM/OButecoFront-End).

## Funcionalidades
- Login com JWT (expira em 1h) e controle de acesso por cargo: ADMIN > GERENTE > AUXILIAR > ESTAGIARIO
- Cadastro e gestão de usuários (somente ADMIN)
- Produtos e insumos (ficha técnica)
- Estoque: entradas/saídas com validação de saldo, conversão de unidades e baixa automática dos insumos
- Edição/exclusão de movimentações com reversão do saldo
- Busca de endereço por CEP (ViaCEP) 

## Arquitetura 
``` Navegador --> Apache :80 --+--> /          arquivos do Angular (fallback SPA) +--> /api/*     proxy --> Spring Boot 127.0.0.1:8080 --> PostgreSQL 127.0.0.1:5432 ```
Camadas:
`controller` (+ `dto`) -> `model/service` -> `model/repository` -> `model/entity`; 
segurança em `model/infra/spring/security`; 
erros em `exception/GlobalExceptionHandler`.

## Deploy na AWS (EC2) 
1. Criar instância **Ubuntu 24.04** (t2.micro ou t3.micro) com um par de chaves `.pem`.
2. Security Group: | Porta | Origem | Motivo | |---|---|---| | 22 | Meu IP | SSH | | 80 | 0.0.0.0/0 | Aplicação | Portas **8080** e **5432** NÃO devem ser liberadas. 
3. Conectar: `ssh -i chave.pem ubuntu@IP_PUBLICO` 
4. Baixar o script, **editar o IP** e executar: ```bash curl -O https://raw.githubusercontent.com/HenriqueHBM/OButecoBackEnd/main/deploy/setup_ambiente.sh nano setup_ambiente.sh        # trocar SERVER_HOST="..." pelo IP público da instância chmod +x setup_ambiente.sh ./setup_ambiente.sh ``` 

O script instala Java 17, Maven, Node, PostgreSQL e Apache; gera senhas aleatórias; compila back e front; configura o Apache e registra o back-end como serviço `obuteco` no systemd. 
Ao final, conferir com `systemctl status obuteco`. 

5. Acessar `http://IP_PUBLICO`. Usuário: `henrique.madeira`. Senha: `sudo grep APP_ADMIN_SENHA ~/OButeco/credenciais.txt` 

## Atualizar a aplicação (redeploy) 
```bash # back-end cd ~/OButeco/OButecoBack-End && git pull && mvn clean package -DskipTests && sudo systemctl restart obuteco # front-end cd ~/OButeco/OButecoFront-End && git pull && npm install && npx ng build \ && sudo cp -rf dist/obuteco-front-end/browser/. /var/www/html/ ``` 

## Variáveis de ambiente 
Definidas no arquivo `.env` (nunca commitado — está no `.gitignore`). 
Modelo em `.env.example`: `DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, JWT_SECRET, APP_ADMIN_SENHA, CORS_ALLOWED_ORIGINS`.

## Rodar localmente 
```bash cp .env.example .env     # preencher os valores ./mvnw spring-boot:run   # API em http://localhost:8080 (Swagger: /swaggerui.html) ./mvnw test              # testes ``` 

## Solução de problemas
- Status dos serviços: `systemctl status obuteco apache2 postgresql`
- Logs do back-end: `journalctl -u obuteco -f`
- Logs do Apache: `sudo tail -f /var/log/apache2/error.log`