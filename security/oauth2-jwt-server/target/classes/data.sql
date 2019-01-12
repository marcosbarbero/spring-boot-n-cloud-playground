INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, authorities, access_token_validity)
  VALUES ('clientId', '$2a$10$Svlz1QlcUnz3GxC3v.i3k.oJ/bSnecF0Fo53SZemoRrVnfrwid4Ge', 'read,write', 'password,refresh_token,client_credentials', 'ROLE_CLIENT', 300);

INSERT INTO users (id, username, password, enabled) VALUES (1, 'user', '$2a$10$6LlkbDfCDD2MSFer/JjDY.vbw90GY8wRJVQhMo7THIx3m5nKAI1ne', 1);
INSERT INTO authorities (username, authority) VALUES ('user', 'ROLE_USER');