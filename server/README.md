# JF2 Multiplayer Backend

Servidor inicial de salas multiplayer em tempo real usando Node.js, Express e Socket.IO.

## Recursos
- Lista de salas públicas
- Criar/entrar por código
- Até 8 participantes por sala
- Bots e dificuldade: NOVATO, AMADOR, MESTRE e VETERANO
- Início de partida
- Relay inicial de estado de jogo por Socket.IO

## Executar

```bash
npm install
npm start
```

A porta padrão é `3000`; hospedagens que definem `PORT` serão respeitadas.

> Este backend é uma base de desenvolvimento. Para produção, ainda precisamos de autenticação, persistência, validação autoritativa da partida, proteção contra abuso e hospedagem pública HTTPS/WSS.
