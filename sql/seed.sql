INSERT INTO categories (name)
VALUES ('Fiksi'), ('Teknologi')
ON CONFLICT (name) DO NOTHING;

INSERT INTO authors (name)
VALUES ('Andrea Hirata'), ('Robert C. Martin'), ('Joshua Bloch'),
       ('Erich Gamma'), ('Richard Helm'), ('Ralph Johnson'), ('John Vlissides')
ON CONFLICT (name) DO NOTHING;

INSERT INTO books (title, isbn, publication_year, category_id)
VALUES
  ('Laskar Pelangi', '9789793062792', 2005, (SELECT id FROM categories WHERE name = 'Fiksi')),
  ('Clean Code', '9780132350884', 2008, (SELECT id FROM categories WHERE name = 'Teknologi')),
  ('Effective Java', '9780134685991', 2018, (SELECT id FROM categories WHERE name = 'Teknologi')),
  ('Design Patterns', '9780201633610', 1994, (SELECT id FROM categories WHERE name = 'Teknologi'))
ON CONFLICT (isbn) DO NOTHING;

INSERT INTO book_authors (book_id, author_id)
SELECT b.id, a.id
FROM (VALUES
  ('9789793062792', 'Andrea Hirata'),
  ('9780132350884', 'Robert C. Martin'),
  ('9780134685991', 'Joshua Bloch'),
  ('9780201633610', 'Erich Gamma'),
  ('9780201633610', 'Richard Helm'),
  ('9780201633610', 'Ralph Johnson'),
  ('9780201633610', 'John Vlissides')
) AS v(isbn, author_name)
JOIN books b ON b.isbn = v.isbn
JOIN authors a ON a.name = v.author_name
ON CONFLICT DO NOTHING;

INSERT INTO members (name, email, joined_at)
VALUES ('Budi Santoso', 'budi@mail.com', CURRENT_DATE),
       ('Sari Dewi', 'sari@mail.com', CURRENT_DATE)
ON CONFLICT (email) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS one_active_loan_per_book
ON loans (book_id) WHERE return_date IS NULL;
