# Galarraga Eventos API

API REST em Java 21 com Spring Boot e H2. Esta versão contém o backend do Galarraga Eventos.

## Executar

Na pasta da API, rode pelo PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Com Gradle, use `.\gradlew.bat bootRun`. Pela IDE, execute `BackendApplication` com a pasta da API como diretório de trabalho. Veja as rotas no [Swagger](http://localhost:8080/swagger-ui.html).

O H2 cria `data/galarraga-eventos.mv.db` se o arquivo ainda não existir. Para usar os dados de uma instalação anterior, rode a nova versão com o mesmo arquivo H2 e a mesma URL JDBC.

## Criar o administrador

Crie `config/application.properties` na pasta da API e informe os dados abaixo. Use um e-mail e um CPF que ainda não estejam cadastrados como cliente.

```properties
app.admin.email=admin@galarraga.example
app.admin.senha=COLOQUE_SUA_SENHA_AQUI
app.admin.cpf=12345678909
```

Escolha uma senha com pelo menos oito caracteres. Na primeira inicialização com esses dados, a API cria a conta `ADMIN`. Depois, use o e-mail e a senha no frontend ou em `POST /auth/login`. O Swagger aceita o JWT no botão **Authorize**. O cadastro público em `POST /auth/register` cria contas `USER`.

Para cadastrar eventos de exemplo em um catálogo vazio, adicione `app.demo.catalogo=true` nesse mesmo arquivo.

## Console H2 local

Ative o perfil `local` na IDE ou rode:

```powershell
.\mvnw.cmd "-Dspring-boot.run.profiles=local" spring-boot:run
```

Ao ativar esse perfil, a API escuta em `127.0.0.1`. Abra o [console H2](http://127.0.0.1:8080/h2-console/), informe a URL `jdbc:h2:file:./data/galarraga-eventos` e o usuário `sa` e deixe a senha em branco.

## Estrutura

- `controller` recebe as requisições HTTP.
- `service` aplica regras de cadastro, estoque, pedidos e check-in.
- `model` reúne as entidades JPA e os enums.
- `dto` define os campos recebidos e devolvidos pela API.
- `repository` acessa os dados pelo Spring Data JPA.
- `config` configura segurança, inicialização e console local.
- `exception` trata os erros devolvidos pela API.

## Testes

```powershell
.\mvnw.cmd test
```

Com Gradle, use `.\gradlew.bat test`. Para rodar somente a classe de pedidos, use `.\mvnw.cmd "-Dtest=PedidosControllerTest" test`.

Em `src/test/java`, o pacote `controller` contém uma classe de teste por controller. Os testes usam MockMvc e H2 em memória. Os cenários de usuários e pedidos também exercitam o JWT.

## Fluxo de pedidos

O catálogo tem leitura pública. Alterações no catálogo, configurações, vendas e check-in exigem o perfil `ADMIN`. O cliente consulta seus pedidos em `GET /api/pedidos`. A compra simulada atualiza o estoque e emite os ingressos. O cancelamento repõe o estoque quando nenhum ingresso do pedido foi usado.
