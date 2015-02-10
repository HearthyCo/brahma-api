-- Sample Data --


# --- !Ups


SET search_path TO brahma, PUBLIC;

INSERT INTO collective (id, name) VALUES
  (90100, 'Collective1'),
  (90101, 'Collective2');

INSERT INTO institution (id, name, meta) VALUES
  (90600, 'Institution1', '{}'),
  (90601, 'Institution2', '{}');

INSERT INTO "user" (id, type, manager_user_id, tutor_user_id, collective_collective_id, institution_institution_id, email, password, can_login, name, surname1, surname2, birthdate, national_id, gender, balance, meta) VALUES
  (90000, 'CLIENT', null, null, null, null, 'testclient1@glue.gl', '$2a$10$WsjgBbDB0uILyHoz7U7qIuTmWueom1f5AQ9fzlRlrQS1jtO9oeday', true, 'Test', 'Client', 'User1', '1987-12-25', '12345678Z', 'FEMALE', 20000000, '{}'),
  (90001, 'CLIENT', null, null, 90100, null, 'testclient2@glue.gl', '$2a$10$Fet.SSfzU013yDhiBFoyh.PTHE1HV/TR6DQZQ1beWVjTk.V2yllTe', true, 'Test', 'Client', 'User2', '1985-06-21', '23456789Z', 'MALE', 20, '{}'),
  (90002, 'TUTOR', null, null, null, null, 'testtutor1@glue.gl', '$2a$10$hyaM.y3UyCIKWmq/hXMr7Oqcd4KmryMIdFi7mQxH9iBH7ASM53CBe', true, 'Test', 'Tutor', 'User1', '1999-01-01', '34567890Z', 'FEMALE', 4, '{}'),
  (90003, 'CLIENT', null, 90002, null, null, 'testpet1@glue.gl', '$2a$10$9wC2cRkENN0hyQdfV9wny.GFdhd01iJixfCSynb8DKKnaBfWkj5q.', false, 'Test', 'Client', 'Pet1', '2014-12-01', null, 'MALE', 1000, '{}'),
  (90004, 'CLIENT', null, 90002, 90101, null, 'testpet2@glue.gl', null, false, 'Test', 'Client', 'Pet2', '2014-11-01', null, 'FEMALE', 20000, '{}'),
  (90005, 'PROFESSIONAL', null, null, null, null, 'testprofessional1@glue.gl', '$2a$10$N8/bNSwCRvgkLllU7dPqwOhnLRhunhrFuZAsq6hHoQceXDvQsdMAq', false, 'Test', 'Professional', 'User1', '1970-01-01', '99999999Z', 'MALE', 2300, '{}'),
  (90006, 'PROFESSIONAL', null, null, null, 90600, 'testprofessional2@glue.gl', '$2a$10$ujNb2RvpINeFHTDVtrH9HeVsFP0MxtRFeQf1tENXegtk7dOh9hl9K', false, 'Test', 'Professional', 'User2', '1970-12-31', '88888888Z', 'FEMALE', 31000, '{}'),
  (90007, 'COORDINATOR', null, null, null, null, 'testcoordinator1@glue.gl', '$2a$10$GKckVNVdU0MeMHoKi6B7ke8SW5BVOIscKjgm4gxYMV5hcT9ujW1rq', false, 'Test', 'Coordinator', 'User1', '1970-12-25', '77777777Z', 'MALE', 9000, '{}'),
  (90008, 'PROFESSIONAL', 90007, null, null, null, 'testhired1@glue.gl', '$2a$10$rk..ceMCVA73nlIKnll.d.qRtB61iIC2.Ruy.x5b2bR0JCgf1MSQ2', false, 'Test', 'Hired', 'User1', '1970-02-28', '66666666Z', 'FEMALE', 3400, '{}'),
  (90009, 'PROFESSIONAL', 90007, null, null, 90601, 'testhired2@glue.gl', '$2a$10$nDkLcWlv7BlDY0u/DlYFMOPQvoerzbnqd.QAMNYDz2N5b8DxHLG.W', false, 'Test', 'Hired', 'User2', '1970-03-25', '55555555Z', 'MALE', 9000, '{}');

INSERT INTO field (id, name) VALUES
  (90200, 'Field1'),
  (90201, 'Field2'),
  (90202, 'Field3');

INSERT INTO service_type (id, name, field_id, price, mode, poolsize) VALUES
  (90300, 'Service Field1 Free', 90200, 0, 'ASYNC', 100),
  (90301, 'Service Field1 Instant', 90200, 500, 'ASYNC', 0),
  (90302, 'Service Field1 Video', 90200, 1500, 'VIDEO', 0);

INSERT INTO service (id, user_id, service_type_id, earnings) VALUES
  (90400, 90005, 90300, 400),
  (90401, 90005, 90301, 900),
  (90402, 90008, 90301, 900);

INSERT INTO availability (id, user_id, repeat_start_date, repeat_end_date, schedule_start_time, schedule_end_time, repeat) VALUES
  (90500, 90005, '2014-12-01', null, '08:00:00', '13:00:00', 31),
  (90501, 90005, '2014-12-02', null, '16:00:00', '19:00:00', 31),
  (90502, 90008, '2014-12-01', null, '08:00:00', '13:00:00', 96);

INSERT INTO notification (id, user_id, type, meta, creation_date, notification_date) VALUES
  (90600, 90000, 'session', '{}', '2014-12-15 08:00:00', '2015-03-02 17:00:00'),
  (90601, 90005, 'session', '{}', '2014-12-15 09:00:00', '2015-03-02 17:00:00'),
  (90602, 90006, 'session', '{}', '2014-12-15 09:00:00', '2015-03-02 17:00:00');

INSERT INTO session (id, service_type_id, title, start_date, end_date, state, timestamp) VALUES
  (90700, 90302, 'testSession1', '2015-03-02 17:00:00', '2015-03-02 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90701, 90302, 'testSession2', '2015-03-01 12:00:00', '2015-03-01 12:15:00', 'CANCELED', '2014-12-15 09:00:00'),
  (90702, 90300, 'testSession3', '2015-03-03 13:00:00', '2015-03-01 12:15:00', 'CLOSED', '2014-12-15 19:00:00'),
  (90703, 90300, 'testSession4', '2015-03-04 14:00:00', '2015-03-01 12:15:00', 'FINISHED', '2014-12-15 12:00:00'),
  (90704, 90302, 'testSession5', '2015-03-05 17:00:00', '2015-03-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90705, 90302, 'testSession6', '2015-04-06 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90706, 90302, 'testSession7', '2015-04-07 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90707, 90302, 'testSession8', '2015-04-08 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90708, 90300, 'testSession9', '2015-04-09 17:00:00', '2015-04-12 17:15:00', 'CLOSED', '2014-12-15 08:00:00'),
  (90709, 90300, 'testSession10', '2015-05-12 17:00:00', '2015-05-20 17:15:00', 'CLOSED', '2014-12-15 08:00:00'),
  (90710, 90302, 'testSession11', '2015-05-22 17:00:00', '2015-05-23 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90711, 90302, 'testSession12', '2015-06-27 17:00:00', '2015-06-30 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00');

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
  (91616, 90711, 90001, null, 90401, 90501, '{}');


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

INSERT INTO attachment (id, history_entry_id, session_id, user_id, path) VALUES
  (91200, 91102, null, 90005, 'infographic.pdf'),
  (91201, null, 90700, 90000, 'xray.jpg');

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
