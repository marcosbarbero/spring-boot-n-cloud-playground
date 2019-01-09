insert into oauth_client_details (client_id, client_secret, scope, authorized_grant_types, authorities, access_token_validity)
VALUES
  ('clientId', '$2a$10$Svlz1QlcUnz3GxC3v.i3k.oJ/bSnecF0Fo53SZemoRrVnfrwid4Ge', 'read,write', 'password,client_credentials', 'ROLE_CLIENT', 300);