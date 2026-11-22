#!/usr/bin/env bash
# Uso: bash scripts/test-etapa2.sh   (API rodando em http://localhost:8080)
BASE="${BASE_URL:-http://localhost:8080}"
EMAIL="teste$(date +%s)@drywall.com"
TMP="$(mktemp)"
FAILED=0

status() { curl -s -o "$TMP" -w "%{http_code}" "$@"; }
check() {
  if [ "$2" = "$3" ]; then echo "OK   $1 ($3)"; else echo "FAIL $1 (esperado $2, obtido $3)"; FAILED=1; fi
}
JSON=(-H "Content-Type: application/json")

check "health publico" 200 "$(status "$BASE/health")"

BODY="{\"name\":\"Joao Silva\",\"companyName\":\"JS Drywall\",\"email\":\"$EMAIL\",\"phone\":\"21999990000\",\"password\":\"Senha1234\",\"confirmPassword\":\"Senha1234\"}"
check "cadastro" 201 "$(status -X POST "$BASE/auth/register" "${JSON[@]}" -d "$BODY")"
check "cadastro duplicado" 409 "$(status -X POST "$BASE/auth/register" "${JSON[@]}" -d "$BODY")"

WEAK="{\"name\":\"Ana\",\"email\":\"ana$EMAIL\",\"phone\":\"21999990000\",\"password\":\"abc\",\"confirmPassword\":\"abc\"}"
check "senha fraca" 400 "$(status -X POST "$BASE/auth/register" "${JSON[@]}" -d "$WEAK")"

MISMATCH="{\"name\":\"Ana\",\"email\":\"b$EMAIL\",\"phone\":\"21999990000\",\"password\":\"Senha1234\",\"confirmPassword\":\"Senha9999\"}"
check "senhas diferentes" 400 "$(status -X POST "$BASE/auth/register" "${JSON[@]}" -d "$MISMATCH")"

check "login senha errada" 401 "$(status -X POST "$BASE/auth/login" "${JSON[@]}" -d "{\"email\":\"$EMAIL\",\"password\":\"Errada123\"}")"
check "login email inexistente" 401 "$(status -X POST "$BASE/auth/login" "${JSON[@]}" -d '{"email":"nao@existe.com","password":"Senha1234"}')"

check "login ok" 200 "$(status -X POST "$BASE/auth/login" "${JSON[@]}" -d "{\"email\":\"$EMAIL\",\"password\":\"Senha1234\"}")"
TOKEN="$(sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p' "$TMP")"
[ -n "$TOKEN" ] || { echo "FAIL token nao encontrado"; FAILED=1; }

check "/users/me sem token" 401 "$(status "$BASE/users/me")"
check "/users/me token invalido" 401 "$(status "$BASE/users/me" -H "Authorization: Bearer ${TOKEN}x")"
check "/users/me com token" 200 "$(status "$BASE/users/me" -H "Authorization: Bearer $TOKEN")"

rm -f "$TMP"
[ "$FAILED" = 0 ] && echo "TUDO OK" || { echo "HA FALHAS"; exit 1; }
