-- V10: Garantir unicidade de e-mail normalizado (minúsculo e sem espaços)
UPDATE users SET email = lower(trim(email));

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower_trim ON users (lower(trim(email)));
