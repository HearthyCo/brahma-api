-- Sample Data --


# --- !Ups


SET search_path TO brahma, PUBLIC;

INSERT INTO collective (id, name) VALUES
  (90100, 'Collective1'),
  (90101, 'Collective2');

INSERT INTO institution (id, name, meta) VALUES
  (90600, 'Institution1', '{}'),
  (90601, 'Institution2', '{}');

INSERT INTO "user" (id, type, manager_user_id, tutor_user_id, collective_collective_id, institution_institution_id, email, password, state, name, surname1, surname2, birthdate, national_id, gender, balance, meta, avatar) VALUES
  (90000, 'CLIENT', null, null, null, null, 'testclient1@glue.gl', '$2a$10$WsjgBbDB0uILyHoz7U7qIuTmWueom1f5AQ9fzlRlrQS1jtO9oeday', 'CONFIRMED', 'Amanda', 'Chick', null, '1987-12-25', '12345678Z', 'FEMALE', 20000000, '{}', 'http://i.imgur.com/nRYY3OK.jpg'),
  (90001, 'CLIENT', null, null, 90100, null, 'testclient2@glue.gl', '$2a$10$Fet.SSfzU013yDhiBFoyh.PTHE1HV/TR6DQZQ1beWVjTk.V2yllTe', 'CONFIRMED', 'Mr', 'Bean', null, '1985-06-21', '23456789Z', 'MALE', 20, '{}', 'http://i.imgur.com/6xdJREZ.jpg'),
  (90002, 'TUTOR', null, null, null, null, 'testtutor1@glue.gl', '$2a$10$hyaM.y3UyCIKWmq/hXMr7Oqcd4KmryMIdFi7mQxH9iBH7ASM53CBe', 'CONFIRMED', 'Test', 'Tutor', 'User1', '1999-01-01', '34567890Z', 'FEMALE', 4, '{}', null),
  (90003, 'CLIENT', null, 90002, null, null, 'testpet1@glue.gl', '$2a$10$9wC2cRkENN0hyQdfV9wny.GFdhd01iJixfCSynb8DKKnaBfWkj5q.', 'DELEGATED', 'Test', 'Client', 'Pet1', '2014-12-01', null, 'MALE', 1000, '{}', null),
  (90004, 'CLIENT', null, 90002, 90101, null, 'testpet2@glue.gl', null, 'DELEGATED', 'Test', 'Client', 'Pet2', '2014-11-01', null, 'FEMALE', 20000, '{}', null),
  (90005, 'PROFESSIONAL', null, null, null, null, 'testprofessional1@glue.gl', '$2a$10$N8/bNSwCRvgkLllU7dPqwOhnLRhunhrFuZAsq6hHoQceXDvQsdMAq', 'CONFIRMED', 'Ingeniero', 'Apple', null, '1970-01-01', '99999999Z', 'MALE', 2300, '{}', 'http://i.imgur.com/aOWKXhG.jpg'),
  (90006, 'PROFESSIONAL', null, null, null, 90600, 'testprofessional2@glue.gl', '$2a$10$ujNb2RvpINeFHTDVtrH9HeVsFP0MxtRFeQf1tENXegtk7dOh9hl9K', 'CONFIRMED', 'Rufo', null, null, '1970-12-31', '88888888Z', 'FEMALE', 31000, '{}', 'http://i.imgur.com/GrLSBRE.jpg'),
  (90007, 'COORDINATOR', null, null, null, null, 'testcoordinator1@glue.gl', '$2a$10$GKckVNVdU0MeMHoKi6B7ke8SW5BVOIscKjgm4gxYMV5hcT9ujW1rq', 'BANNED', 'Test', 'Coordinator', 'User1', '1970-12-25', '77777777Z', 'MALE', 9000, '{}', null),
  (90008, 'PROFESSIONAL', 90007, null, null, null, 'testhired1@glue.gl', '$2a$10$rk..ceMCVA73nlIKnll.d.qRtB61iIC2.Ruy.x5b2bR0JCgf1MSQ2', 'BANNED', 'Test', 'Hired', 'User1', '1970-02-28', '66666666Z', 'FEMALE', 3400, '{}', null),
  (90009, 'PROFESSIONAL', 90007, null, null, 90601, 'testhired2@glue.gl', '$2a$10$nDkLcWlv7BlDY0u/DlYFMOPQvoerzbnqd.QAMNYDz2N5b8DxHLG.W', 'BANNED', 'Test', 'Hired', 'User2', '1970-03-25', '55555555Z', 'MALE', 9000, '{}', null),
  (90010, 'ADMIN', null, null, null, null, 'testadmin1@glue.gl', '$2a$10$Zv4qmaHQgMwbN8pyoKp5Ue2wKWbF80DhMj7HWwCDI0LZraQ5Xm6s6', 'CONFIRMED', 'Bastard', 'Operator', 'From Hell', '1950-01-01', '98765432J', 'MALE', 0, '{}', 'http://i.imgur.com/b0zW8TK.jpg');

INSERT INTO field (id, name) VALUES
  (90200, 'General');

INSERT INTO service_type (id, name, field_id, price, mode, poolsize) VALUES
  (90300, 'Video Session', 90200, 1500, 'VIDEO', 5),
  (90301, 'Instant Session', 90200, 250, 'ASYNC', 10),
  (90302, 'Free Session', 90200, 0, 'ASYNC', 100);

INSERT INTO service (id, user_id, service_type_id, earnings) VALUES
  (90400, 90005, 90300, 400),
  (90401, 90005, 90301, 900),
  (90402, 90008, 90301, 900),
  (90403, 90005, 90302, 100);

INSERT INTO availability (id, user_id, repeat_start_date, repeat_end_date, schedule_start_time, schedule_end_time, repeat) VALUES
  (90500, 90005, '2014-12-01', null, '08:00:00', '13:00:00', 31),
  (90501, 90005, '2014-12-02', null, '16:00:00', '19:00:00', 31),
  (90502, 90008, '2014-12-01', null, '08:00:00', '13:00:00', 96);

INSERT INTO notification (id, user_id, type, meta, creation_date, notification_date) VALUES
  (90600, 90000, 'session', '{}', '2014-12-15 08:00:00', '2015-03-02 17:00:00'),
  (90601, 90005, 'session', '{}', '2014-12-15 09:00:00', '2015-03-02 17:00:00'),
  (90602, 90006, 'session', '{}', '2014-12-15 09:00:00', '2015-03-02 17:00:00');

INSERT INTO session (id, service_type_id, title, start_date, end_date, state, timestamp, meta) VALUES
  (90700, 90302, 'testSession1', '2015-03-02 17:00:00', '2015-03-02 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90701, 90302, 'testSession2', '2015-03-01 12:00:00', '2015-03-01 12:15:00', 'CANCELED', '2014-12-15 09:00:00', '{}'),
  (90702, 90300, 'testSession3', '2015-03-03 13:00:00', '2015-03-01 12:15:00', 'CLOSED', '2014-12-15 19:00:00', '{}'),
  (90703, 90300, 'testSession4', '2015-03-04 14:00:00', '2015-03-01 12:15:00', 'FINISHED', '2014-12-15 12:00:00', '{}'),
  (90704, 90302, 'testSession5', '2015-03-05 17:00:00', '2015-03-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90705, 90302, 'testSession6', '2015-04-06 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90706, 90302, 'testSession7', '2015-04-07 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90707, 90302, 'testSession8', '2015-04-08 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90708, 90300, 'testSession9', '2015-04-09 17:00:00', '2015-04-12 17:15:00', 'CLOSED', '2014-12-15 08:00:00', '{}'),
  (90709, 90300, 'testSession10', '2015-05-12 17:00:00', '2015-05-20 17:15:00', 'CLOSED', '2014-12-15 08:00:00', '{}'),
  (90710, 90302, 'testSession11', '2015-05-22 17:00:00', '2015-05-23 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90711, 90302, 'testSession12', '2015-06-27 17:00:00', '2015-06-30 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00', '{}'),
  (90712, 90302, '#Random', '2015-02-11 17:00:00', '2015-02-11 17:15:00', 'UNDERWAY', '2014-12-15 08:00:00', '{}'),
  (90713, 90301, 'testPool2', '2015-02-14 18:00:00', '2015-02-14 18:18:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90714, 90300, 'testPool3', '2015-02-14 19:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{"opentokSession":"2_MX40NTE3OTYyMn5-MTQyNjQ5OTY3NjQxNH5HRkVjWlNHZ3lCNkdsL0tTNEQ0M2xpZXh-UH4"}'),
  (90715, 90300, 'testPool4', '2015-02-14 20:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{"opentokSession":"1_MX40NTE3OTYyMn5-MTQyNjQ5OTY5OTc0NX44SDJnZmpHK1NOTklJWGVBZUh2S3dPYzZ-UH4"}'),
  (90716, 90301, 'testPool5', '2015-02-15 17:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90717, 90302, 'testPool6', '2015-02-15 18:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90718, 90301, 'testPool7', '2015-02-15 19:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90719, 90301, 'testPool8', '2015-02-15 20:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90720, 90301, 'testPool9', '2015-02-15 21:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90721, 90301, 'testPool10', '2015-02-16 17:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90722, 90302, 'testPool11', '2015-02-17 17:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90723, 90302, 'testPool12', '2015-02-17 18:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90724, 90302, 'testPool13', '2015-02-17 19:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90725, 90302, 'testPool14', '2015-02-17 20:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90726, 90302, 'testPool15', '2015-02-17 21:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}'),
  (90727, 90302, 'testPool16', '2015-02-17 22:00:00', '2015-02-21 17:15:00', 'REQUESTED', '2014-12-15 08:00:00', '{}');

INSERT INTO "session_user" (id, session_id, user_id, notification_id, service_id, availability_id, meta) VALUES
  (91600, 90700, 90000, 90600, 90401, 90501, '{}'),
  (91601, 90701, 90001, null, 90402, 90502, '{}'),
  (91602, 90700, 90005, 90601, 90401, 90501, '{}'),
  (91603, 90701, 90008, null, 90402, 90502, '{}'),
  (91604, 90702, 90000, null, 90401, 90501, '{}'),
  (91605, 90703, 90000, null, 90402, 90502, '{}'),
  (91606, 90704, 90000, null, 90401, 90501, '{}'),
  (91607, 90704, 90008, null, 90401, 90501, '{}'),
  (91608, 90704, 90001, null, 90401, 90501, '{}'),
  (91609, 90705, 90006, 90602, 90401, 90501, '{}'),
  (91610, 90706, 90001, null, 90401, 90501, '{}'),
  (91611, 90706, 90003, null, 90401, 90501, '{}'),
  (91612, 90707, 90003, null, 90401, 90501, '{}'),
  (91613, 90708, 90004, null, 90401, 90501, '{}'),
  (91614, 90709, 90008, null, 90401, 90501, '{}'),
  (91615, 90710, 90009, null, 90401, 90501, '{}'),
  (91616, 90711, 90001, null, 90401, 90501, '{}'),
  (91617, 90712, 90000, null, null, null, '{}'),
  (91618, 90713, 90001, null, null, null, '{}'),
  (91619, 90714, 90002, null, null, null, '{"opentokToken":"T1==cGFydG5lcl9pZD00NTE3OTYyMiZzaWc9YTRhZDA3ODk2NzUyMjgwOTY2NmVjYzZmZDQ4OTM5MWVlY2RhM2JhNzpzZXNzaW9uX2lkPTJfTVg0ME5URTNPVFl5TW41LU1UUXlOalE1T1RZM05qUXhOSDVIUmtWaldsTkhaM2xDTmtkc0wwdFRORVEwTTJ4cFpYaC1VSDQmY3JlYXRlX3RpbWU9MTQyNjQ5OTY4NSZub25jZT0xMDMwMzkxMzA3JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE0MjkwOTE2ODU="}'),
  (91620, 90715, 90001, null, null, null, '{"opentokToken":"T1==cGFydG5lcl9pZD00NTE3OTYyMiZzaWc9M2Q4ZTMwN2ZhNDU1OTM4MjFlM2VlNmRkMWI0MzY5Y2NlMTNjYTQ0ZTpzZXNzaW9uX2lkPTFfTVg0ME5URTNPVFl5TW41LU1UUXlOalE1T1RZNU9UYzBOWDQ0U0RKblptcEhLMU5PVGtsSldHVkJaVWgyUzNkUFl6Wi1VSDQmY3JlYXRlX3RpbWU9MTQyNjQ5OTcwOCZub25jZT0tMTg1NzQ2MDg2NSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNDI5MDkxNzA4"}'),
  (91621, 90716, 90001, null, null, null, '{}'),
  (91622, 90717, 90000, null, null, null, '{}'),
  (91623, 90718, 90000, null, null, null, '{}'),
  (91624, 90719, 90000, null, null, null, '{}'),
  (91625, 90720, 90000, null, null, null, '{}'),
  (91626, 90721, 90001, null, null, null, '{}'),
  (91627, 90722, 90001, null, null, null, '{}'),
  (91628, 90723, 90000, null, null, null, '{}'),
  (91629, 90724, 90001, null, null, null, '{}'),
  (91630, 90725, 90001, null, null, null, '{}'),
  (91631, 90726, 90000, null, null, null, '{}'),
  (91632, 90727, 90000, null, null, null, '{}'),
  (91633, 90712, 90005, null, null, null, '{}'),
  (91634, 90712, 90006, null, null, null, '{}'),
  (91635, 90712, 90010, null, null, null, '{}'),
  (91636, 90712, 90001, null, null, null, '{}');

INSERT INTO session_log (id, session_id, user_id, timestamp, action) VALUES
  (90800, 90700, 90005, '2014-12-15 08:00:00', 'ACCEPT'),
  (90801, 90701, 90008, '2014-12-15 09:00:00', 'ACCEPT'),
  (90802, 90701, 90008, '2014-12-15 12:00:00', 'REJECT');

INSERT INTO access_log (id, owner_user_id, viewer_user_id, timestamp) VALUES
  (91000, 90000, 90005, '2014-12-16 10:00:00'),
  (91001, 90001, 90005, '2014-12-17 09:00:00');

INSERT INTO history_entry_type (id) VALUES
  ('allergies'),
  ('treatments');

INSERT INTO history_entry (id, owner_user_id, editor_user_id, history_entry_type_id, title, timestamp, removed, description, meta) VALUES
  (91100, 90000, 90005, 'allergies', 'Lactosa', '2014-12-15 08:30:00', false, 'Insuficiencia cardiorespiratoria.', '{"rating": 5}'),
  (91101, 90000, 90005, 'allergies', 'Gluten', '2014-12-15 08:30:00', false, 'Indigestión aguda.', '{"rating": 3}'),
  (91102, 90000, 90005, 'treatments', 'Caminar', '2014-12-15 08:30:00', false, 'Camina una hora al día', '{}');

INSERT INTO attachment (id, history_entry_id, session_id, user_id, url, filename, size) VALUES
  (91200, 91102, null, 90005, 'http://i.imgur.com/MmOMZ.jpg', 'got real.jpg', 26328),
  (91201, null, 90700, 90000, 'http://i.imgur.com/wwqMF.jpg', 'leaf.jpg', 145943);

INSERT INTO transaction (id, user_id, session_id, amount, state, sku, timestamp, reason) VALUES
  (91300, 90000, null, 20001000, 'APPROVED', 'TOPUPPPL_000000090000_0000001423154291', '2014-12-15 06:00:00', 'Incremento de saldo'),
  (91301, 90000, 90700, -1000, 'APPROVED', 'TOPUPPPL_000000090000_0000001423154294', '2014-12-15 08:00:00', 'Reserva de sesión'),
  (91302, 90000, 90701, -1000, 'APPROVED', 'TOPUPPPL_000000090000_0000001423154295', '2014-12-15 09:00:00', 'Reserva de sesión'),
  (91303, 90000, 90701, 1000, 'APPROVED', 'TOPUPPPL_000000090000_0000001423154296', '2014-12-15 12:00:00', 'Devolución sesión cancelada');

INSERT INTO history_archive (id, history_entry_id, editor_user_id, timestamp, meta) VALUES
  (91400, 91100, 90005, '2014-12-16 10:00:00', '{}');


# --- !Downs


SET search_path TO brahma, PUBLIC;
TRUNCATE history_archive, transaction, attachment, history_entry, history_entry_type, access_log, session_log, session, notification, availability, service, service_type, field, "user", institution, collective CASCADE;
