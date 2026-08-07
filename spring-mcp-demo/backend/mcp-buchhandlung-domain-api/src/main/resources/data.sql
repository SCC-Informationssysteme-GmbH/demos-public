INSERT INTO books (id, isbn, title, author, price) VALUES
  (1, '978-0-13-468599-1', 'Effective Java', 'Joshua Bloch', 42.50),
  (2, '978-3-89864-868-6', 'Clean Code', 'Robert C. Martin', 39.90),
  (3, '978-3-96009-186-9', 'Domain-Driven Design', 'Eric Evans', 49.00),
  (4, '978-1-4919-5035-7', 'Designing Data-Intensive Applications', 'Martin Kleppmann', 55.00);

INSERT INTO orders (id, customer_id, book_id, quantity, order_date) VALUES
  (1, 1, 1, 2, '2026-02-10'),
  (2, 1, 3, 1, '2026-03-04'),
  (3, 2, 2, 1, '2026-04-18'),
  (4, 2, 4, 3, '2026-05-22');
