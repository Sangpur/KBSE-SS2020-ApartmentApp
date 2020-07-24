INSERT INTO apartment (id, name) VALUES (1000, 'Irgendwas mit Medien')

INSERT INTO memberdetail (id, birthday, cashbalance, color) VALUES (1, '1990-01-01', 8.20, 'RED')
INSERT INTO memberdetail (id, birthday, cashbalance, color) VALUES (2, '1993-05-13', -12.09, 'GREEN')
INSERT INTO memberdetail (id, birthday, cashbalance, color) VALUES (3, '1992-09-09', 3.89, 'BLUE')

INSERT INTO members (id, apartment_id, memberrole, name, password, details_id) VALUES (1, 1000, 'ADMIN', 'Lucca', '123', 1)
INSERT INTO members (id, apartment_id, memberrole, name, password, details_id) VALUES (2, 1000, 'ADMIN', 'Christoph', '123', 2)
INSERT INTO members (id, apartment_id, memberrole, name, password, details_id) VALUES (3, 1000, 'ADMIN', 'Annika', '123', 3)

INSERT INTO event (id, apartment_id, datetime_begin, category, datetime_end, alldayevent, title, author_id) VALUES (1, 1000, '2020-07-01 12:00:00', 'APPOINTMENT', '2020-07-01 16:00:00', 0, 'Termin Handwerker', 1)
INSERT INTO event (id, apartment_id, datetime_begin, category, datetime_end, alldayevent, title, author_id) VALUES (2, 1000, '2020-07-01 12:00:00', 'APPOINTMENT', '2020-07-01 14:00:00', 0,  'Termin Vermieter', 2)
INSERT INTO event (id, apartment_id, datetime_begin, category, datetime_end, alldayevent, title, author_id) VALUES (3, 1000, '2020-07-28 12:00:00', 'VACATION', '2020-08-03 15:00:00', 0,  'Urlaub in Bayern', 3)
INSERT INTO event (id, apartment_id, datetime_begin, category, datetime_end, alldayevent, title, author_id) VALUES (4, 1000, '2020-08-10 18:00:00', 'VACATION', '2020-08-13 20:00:00', 0,  'Festival', 3)
 
INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (1, 1000, 'URGENT', 'Bitte Putzplan einhalten!', '2020-07-21 12:13:20', 2)
INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (2, 1000, 'TODO', 'Bitte einkaufen gehen!', '2020-07-21 17:13:20', 3)
INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (3, 1000, 'INFO', 'Ab 22 Uhr nur noch Zimmerlautstärke', '2020-07-22 09:13:20', 1)
INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (4, 1000, 'URGENT', 'Bitte Fahrräder nicht vor der Haustür abstellen!', '2020-07-15 12:13:20', 2)
INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (5, 1000, 'INFO', 'Bitte Wäsche rechtzeitig aus der Waschmaschine nehmen!', '2020-07-05 14:13:20', 1)
INSERT INTO note (id, apartment_id, category, message, datetime, author_id) VALUES (6, 1000, 'TODO', 'Geschirr gehört abgewaschen und nicht in die Spüle gestellt!', '2020-07-19 09:13:20', 3)

INSERT INTO payment (id, apartment_id, day, description, amount, giver_id) VALUES (1, 1000, '2020-06-01', 'Einkauf', 27.30, 1)
INSERT INTO payment (id, apartment_id, day, description, amount, giver_id) VALUES (2, 1000, '2020-06-02', 'Getränke', 12.99, 3)
INSERT INTO payment (id, apartment_id, day, description, amount, giver_id) VALUES (3, 1000, '2020-06-03', 'Geld geliehen', 10.00, 2)

INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (1, 1)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (1, 2)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (1, 3)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (2, 2)
INSERT INTO payment_members (payment_id, involvedmembers_id) VALUES (3, 1)

INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (1, 2, 1000, null, '2020-06-01', 0, 'Mineralwasser')
INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (2, 1, 1000, null, '2020-07-03', 0, 'Zahnpasta')
INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (3, 4, 1000, null, '2020-07-20', 0, 'Äpfel')
INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (4, 1, 1000, null, '2020-07-20', 0, 'Brot')
INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (5, 2, 1000, '2020-07-24', '2020-07-21', 1, 'Joghurt')
INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (6, 2, 1000, '2020-07-22', '2020-07-18', 1, 'Toast')
INSERT INTO shoppingitem (id, amount, apartment_id, checkday, day, checked, name) VALUES (7, 2, 1001, '2020-07-23', '2020-07-20', 1, 'Butter')