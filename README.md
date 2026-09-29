# Restaurante Marketplace

Marketplace local para descoberta de restaurantes e produtos, montagem de um carrinho com itens de várias lojas, pagamento online e logística híbrida.

O projeto começará em uma única cidade. Cada restaurante administra seu próprio catálogo, preços, adicionais, disponibilidade e regras de entrega. Antes de operar, o estabelecimento precisa ser aprovado pela plataforma.

## Visão do produto

O cliente poderá:

- pesquisar por restaurante, tipo de estabelecimento ou item;
- personalizar pratos, removendo ingredientes e selecionando adicionais;
- adicionar ao mesmo carrinho produtos de restaurantes diferentes;
- escolher entre as modalidades de entrega disponíveis;
- pagar online em um único checkout;
- acompanhar separadamente a preparação e a entrega de cada parte do pedido.

O sistema dividirá uma compra em subpedidos por restaurante. Quando dois ou mais restaurantes utilizarem a plataforma de entrega e forem compatíveis em rota e horário, seus subpedidos poderão formar um único grupo de entrega.

## Princípios iniciais

1. Cada restaurante é responsável pelo próprio catálogo; não existe produto global compartilhado.
2. O cliente recebe o detalhamento completo de valores antes do pagamento.
3. Entregas próprias não são combinadas com pedidos de outros restaurantes.
4. Entregas da plataforma podem ser combinadas quando rota, capacidade e tempo de preparo forem compatíveis.
5. Os valores de produtos, restaurante, entrega, pagamento e plataforma são registrados separadamente.
6. A primeira versão será preparada para entregas agrupadas, mas a otimização de rotas será implantada por etapas.

## Documentação

- [Requisitos do produto](docs/requisitos-do-produto.md)
- [Modelo de domínio](docs/modelo-de-dominio.md)
- [Regras de entrega](docs/regras-de-entrega.md)
- [Roadmap](docs/roadmap.md)
- [Jornadas do produto](docs/jornadas-do-produto.md)
- [Arquitetura proposta](docs/arquitetura-proposta.md)
- [Observabilidade e mensageria](docs/observabilidade-e-mensageria.md)
- [Registro de decisões](docs/decisoes/README.md)

## Situação atual

O projeto entrou na fundação técnica. O backend Quarkus está em `apps/backend`; frontend, provedor de pagamento, empresa de logística, hospedagem e identidade visual ainda aguardam decisão final.

## Backend

A base utiliza Java 21, Quarkus 3.39.5, Maven, GraphQL, REST, PostgreSQL, Flyway, health checks e OpenTelemetry/Micrometer.

Para executar em desenvolvimento, com Java 21+ e Docker disponíveis:

```shell
cd apps/backend
./mvnw quarkus:dev
```

O Quarkus Dev Services inicia o PostgreSQL automaticamente. A GraphQL UI fica disponível em <http://localhost:8080/q/graphql-ui/> e os health checks na porta de gerenciamento `9000`.
