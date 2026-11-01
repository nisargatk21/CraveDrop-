USE cravedrop;

ALTER TABLE users MODIFY role ENUM('customer','owner','admin') NOT NULL DEFAULT 'customer';

-- Create an account through the normal CraveDrop sign-up page first, then
-- replace the email below with that account's email and execute this statement.
-- This manual database action is the only way to grant administrator access.
UPDATE users
SET role = 'admin'
WHERE email = 'replace-with-your-admin-email@example.com';

-- Confirm the role change before signing in:
SELECT id, name, email, role
FROM users
WHERE email = 'replace-with-your-admin-email@example.com';
