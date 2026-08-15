import express from "express";
import http from "http";
import cors from "cors";
import { Server } from "socket.io";

const app = express();
const httpServer = http.createServer(app);
const io = new Server(httpServer, { cors: { origin: "*" } });
const rooms = new Map();
const difficulties = new Set(["NOVATO", "AMADOR", "MESTRE", "VETERANO"]);

app.use(cors());
app.get("/health", (_req, res) => res.json({ ok: true, rooms: rooms.size }));

function publicRoom(room) {
  return { code: room.code, name: room.name, players: room.players.size, maxPlayers: 8, bots: room.bots, botDifficulty: room.botDifficulty };
}

function roomList() { return [...rooms.values()].map(publicRoom); }

io.on("connection", socket => {
  socket.emit("room:list", roomList());

  socket.on("room:create", ({ name = "Sala JF2", playerName = "Jogador" } = {}, ack) => {
    let code;
    do code = Math.random().toString(36).slice(2, 8).toUpperCase(); while (rooms.has(code));
    const room = { code, name, players: new Map(), bots: 0, botDifficulty: "NOVATO", started: false };
    room.players.set(socket.id, { id: socket.id, name: String(playerName).slice(0, 20), host: true });
    rooms.set(code, room);
    socket.join(code);
    ack?.({ ok: true, room: publicRoom(room) });
    io.emit("room:list", roomList());
    io.to(code).emit("room:update", publicRoom(room));
  });

  socket.on("room:join", ({ code, playerName = "Jogador" } = {}, ack) => {
    const room = rooms.get(String(code || "").toUpperCase());
    if (!room) return ack?.({ ok: false, error: "Sala não encontrada" });
    if (room.players.size + room.bots >= 8) return ack?.({ ok: false, error: "Sala cheia" });
    room.players.set(socket.id, { id: socket.id, name: String(playerName).slice(0, 20), host: false });
    socket.join(room.code);
    ack?.({ ok: true, room: publicRoom(room) });
    io.to(room.code).emit("room:update", publicRoom(room));
    io.emit("room:list", roomList());
  });

  socket.on("room:bots", ({ code, count, difficulty } = {}, ack) => {
    const room = rooms.get(String(code || "").toUpperCase());
    if (!room || !room.players.has(socket.id)) return ack?.({ ok: false, error: "Acesso negado" });
    if (!difficulties.has(difficulty)) return ack?.({ ok: false, error: "Dificuldade inválida" });
    room.bots = Math.max(0, Math.min(7, Number(count) || 0));
    room.botDifficulty = difficulty;
    if (room.players.size + room.bots > 8) room.bots = 8 - room.players.size;
    io.to(room.code).emit("room:update", publicRoom(room));
    io.emit("room:list", roomList());
    ack?.({ ok: true, room: publicRoom(room) });
  });

  socket.on("game:start", ({ code } = {}, ack) => {
    const room = rooms.get(String(code || "").toUpperCase());
    if (!room || !room.players.has(socket.id)) return ack?.({ ok: false, error: "Acesso negado" });
    room.started = true;
    io.to(room.code).emit("game:start", { code: room.code, botDifficulty: room.botDifficulty });
    ack?.({ ok: true });
  });

  socket.on("game:state", ({ code, state } = {}) => {
    const room = rooms.get(String(code || "").toUpperCase());
    if (room?.players.has(socket.id) && room.started) socket.to(room.code).emit("game:state", { playerId: socket.id, state });
  });

  socket.on("disconnect", () => {
    for (const [code, room] of rooms) {
      if (room.players.delete(socket.id)) {
        if (room.players.size === 0) rooms.delete(code);
        else io.to(code).emit("room:update", publicRoom(room));
        io.emit("room:list", roomList());
      }
    }
  });
});

const port = process.env.PORT || 3000;
httpServer.listen(port, () => console.log(`JF2 multiplayer server listening on ${port}`));
