INSERT INTO apartment (id, name) VALUES (1000, 'Irgendwas mit Medien')

INSERT INTO memberdetail (id, birthday, color) VALUES (1, '1990-01-01', 'RED')
INSERT INTO memberdetail (id, birthday, color) VALUES (2, '1993-05-13', 'GREEN')
INSERT INTO memberdetail (id, birthday, color) VALUES (3, '1992-09-09', 'BLUE')

INSERT INTO members (id, memberrole, name, password, details_id) VALUES (1, 'ADMIN', 'Lucca', '123', 1)
INSERT INTO members (id, memberrole, name, password, details_id) VALUES (2, 'ADMIN', 'Christoph', '123', 2)
INSERT INTO members (id, memberrole, name, password, details_id) VALUES (3, 'ADMIN', 'Annika', '123', 3)

INSERT INTO apartment_members (apartment_id, members_id) VALUES (1000, 1)
INSERT INTO apartment_members (apartment_id, members_id) VALUES (1000, 2)
INSERT INTO apartment_members (apartment_id, members_id) VALUES (1000, 3)

INSERT INTO event (id, apartment_id, datetime_begin, category, datetime_end, title) VALUES (1, 1000, '2020-06-30 15:30:00', 'APPOINTMENT', '2020-06-30 16:00:00', 'Termin Handwerker')
INSERT INTO event (id, apartment_id, datetime_begin, category, datetime_end, title) VALUES (2, 1000, '2020-07-21 12:00:00', 'APPOINTMENT', '2020-07-21 14:00:00', 'Termin Vermieter')

INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (1, 1000, 'URGENT', 'Bitte Putzplan einhalten!', '2020-07-21 12:13:20', 2)

INSERT INTO payment (id, apartment_id, day, description, repayment, amount, giver_id) VALUES (1, 1000, '2020-06-01', 'Einkauf', 0, 25.50, 3)
INSERT INTO payment (id, apartment_id, day, description, repayment, amount, giver_id) VALUES (2, 1000, '2020-06-02', 'Getränke', 0, 12.99, 2)
INSERT INTO payment (id, apartment_id, day, description, repayment, amount, giver_id) VALUES (3, 1000, '2020-06-03', 'Geld geliehen', 1, 10.00, 1)

INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (1, 1)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (1, 2)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (2, 1)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (3, 2)

INSERT INTO shoppingitem (id, amount, apartment_id, day, checked, name) VALUES (1, 2, 1000, '2020-06-01', 0, 'Mineralwasser')