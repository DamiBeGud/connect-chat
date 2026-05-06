const crypto = require("node:crypto");

const token = createJwt("143b4676-3693-4186-af84-5dfeb9692b7c");
const recipientId = "ac9b3a0a-bedb-45bc-975c-9a3b83a6ca09";
const ws = new WebSocket("ws://localhost:8083/ws/chat");

ws.onopen = () => {
  console.log("websocket opened");

  ws.send(
    "CONNECT\n" +
      "Authorization:Bearer " +
      token +
      "\n" +
      "accept-version:1.2\n" +
      "heart-beat:10000,10000\n\n" +
      "\u0000",
  );
};

ws.onmessage = (event) => {
  console.log("received:", event.data);

  if (event.data.startsWith("CONNECTED")) {
    ws.send(
      "SUBSCRIBE\n" +
        "id:private-messages\n" +
        "destination:/user/queue/private-messages\n\n" +
        "\u0000",
    );

    console.log("subscribed");

    ws.send(
      "SEND\n" +
        "destination:/app/chat.private\n" +
        "content-type:application/json\n\n" +
        JSON.stringify({
          recipientId,
          content: "Hello from simple websocket testdsa",
        }) +
        "\u0000",
    );

    console.log("message sent");
  }
};

ws.onerror = (event) => {
  console.error("websocket error:", event);
};

ws.onclose = (event) => {
  console.log("websocket closed:", event.code, event.reason);
};

function createJwt(subject) {
  const secret =
    process.env.IDENTITY_JWT_SECRET ||
    "connect-chat-local-jwt-secret-must-be-at-least-32-bytes";
  const issuer = process.env.IDENTITY_JWT_ISSUER || "http://localhost:8081";
  const nowSeconds = Math.floor(Date.now() / 1000);
  const header = { alg: "HS256", typ: "JWT" };
  const payload = {
    sub: subject,
    iss: issuer,
    token_type: "user",
    role: "USER",
    iat: nowSeconds,
    exp: nowSeconds + 15 * 60,
  };
  const unsignedToken = [
    Buffer.from(JSON.stringify(header)).toString("base64url"),
    Buffer.from(JSON.stringify(payload)).toString("base64url"),
  ].join(".");
  const signature = crypto
    .createHmac("sha256", secret)
    .update(unsignedToken)
    .digest("base64url");

  return `${unsignedToken}.${signature}`;
}
