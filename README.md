# 🧠 chat-server

Backend do projeto **chat-p2p-client**: um sistema de mensagens peer-to-peer com foco em privacidade e descentralização.

---

## ⚙️ Stack

- Java 17
- Spring Boot
- Kafka
- Redis
- WebSocket

---

## 🧩 Funcionalidades

- Gerenciamento de sessões via Redis
- Recebimento de mensagens via WebSocket
- Encaminhamento de mensagens via Kafka
- Entrega de mensagens em tempo real ou armazenadas temporariamente
- Suporte a múltiplos peers conectados simultaneamente

---

## 🚀 Como rodar local

1. Clone o repositório:

```bash
git clone https://github.com/0xrodrigues/chat-server.git
cd chat-server
```

2. Rode os serviços dependentes (Kafka + Redis). Exemplo via Docker:

```bash
docker-compose up -d
```

3. Suba o servidor:

```bash
./mvnw spring-boot:run
```

> Porta padrão: `8080`

---

## 📡 WebSocket endpoint

```
ws://localhost:8080/ws/{username}
```

- Exemplo: `ws://localhost:8080/ws/alice`
- As mensagens devem ser enviadas em JSON:
```json
{
  "to": "bob",
  "message": "Hello Bob!"
}
```

---

## 📁 Estrutura

```
chat-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/chatserver/
│   │   └── resources/
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🧪 Testes

Em construção.

---

## 📣 Contribua
Se você também acredita em privacidade por padrão, abra uma issue, fork o projeto e compartilhe sua melhoria.

---

## 🧑‍💻 Autor
**@0xrodrigues** – Software Engineer, privacy advocate & digital builder.

---

## 🛡️ Licença
MIT License. Liberdade para usar, aprender e evoluir.
