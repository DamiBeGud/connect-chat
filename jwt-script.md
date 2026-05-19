python3 - <<'PY'
import base64, hashlib, hmac, json, time

env = {}
with open(".env") as f:
    for line in f:
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            env[k] = v

secret = env["IDENTITY_JWT_SECRET"].encode()
issuer = env.get("IDENTITY_JWT_ISSUER", "http://localhost:8081")
now = int(time.time())
exp = now + 3 * 24 * 60 * 60

header = {"alg": "HS256", "typ": "JWT"}
payload = {
    "sub": "chat-service",
    "iss": issuer,
    "token_type": "service",
    "role": "INTERNAL_SERVICE",
    "iat": now,
    "exp": exp,
}

def b64(data):
    return base64.urlsafe_b64encode(json.dumps(data, separators=(",", ":")).encode()).rstrip(b"=")

unsigned = b".".join([b64(header), b64(payload)])
signature = base64.urlsafe_b64encode(
    hmac.new(secret, unsigned, hashlib.sha256).digest()
).rstrip(b"=")

print((unsigned + b"." + signature).decode())
PY
