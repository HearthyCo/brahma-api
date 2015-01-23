-- Sample Data --


# --- !Ups


SET search_path TO brahma, PUBLIC;

INSERT INTO collective (id, name) VALUES
  (90100, 'Collective1'),
  (90101, 'Collective2');

INSERT INTO institution (id, name, meta) VALUES
  (90600, 'Institution1', '{}'),
  (90601, 'Institution2', '{}');

INSERT INTO "user" (id, type, manager_user_id, tutor_user_id, collective_collective_id, institution_institution_id, login, password, can_login, name, surname1, surname2, birthdate, national_id, gender, balance) VALUES
  (90000, 'CLIENT', null, null, null, null, 'testClient1', '$2a$10$ourB/XLcJaHUAEEAvrNAZ.WcNBInH1o56UuqNiXWwLVXyE00U7IA2', true, 'Test', 'Client', 'User1', '1987-12-25', '12345678Z', 'FEMALE', 20000000),
  (90001, 'CLIENT', null, null, 90100, null, 'testClient2', '$2a$10$cTIH1wLXliX.3f8jRzaFfu.OJJk57xKLg1YBaze9m4o83IqUltkWa', true, 'Test', 'Client', 'User2', '1985-06-21', '23456789Z', 'MALE', 20),
  (90002, 'TUTOR', null, null, null, null, 'testTutor1', '$2a$10$JeqiNGmF1AuLNReD5dOrR.olhb3WKgVQNrmOovlWHM80nOeT7l2IS', true, 'Test', 'Tutor', 'User1', '1999-01-01', '34567890Z', 'FEMALE', 4),
  (90003, 'CLIENT', null, 90002, null, null, 'testPet1', '$2a$10$B2oezu2FWCjEbFqarUFoeOlRy7bN0WKKY7Xxpd8EzBjLPQ.XqCeUa', false, 'Test', 'Client', 'Pet1', '2014-12-01', null, 'MALE', 1000),
  (90004, 'CLIENT', null, 90002, 90101, null, 'testPet2', null, false, 'Test', 'Client', 'Pet2', '2014-11-01', null, 'FEMALE', 20000),
  (90005, 'PROFESSIONAL', null, null, null, null, 'testProfessional1', '$2a$10$E/4oidCPkiPwSHDOhIIkLOO6mzfNiZRsKaIbRxqZ1yc0wqEcvt3sm', false, 'Test', 'Professional', 'User1', '1970-01-01', '99999999Z', 'MALE', 2300),
  (90006, 'PROFESSIONAL', null, null, null, 90600, 'testProfessional2', '$2a$10$bx/i5DemogHrF1zpclbdZOaXd7EvbWdkHDYNgZAFIfJZRQ7SKoTYy', false, 'Test', 'Professional', 'User2', '1970-12-31', '88888888Z', 'FEMALE', 31000),
  (90007, 'COORDINATOR', null, null, null, null, 'testCoordinator1', '$2a$10$iQfb4CosbqhfVeifnr7Cx.tAUx1gmikEzHXznqd4JgFCe1STaaqsy', false, 'Test', 'Coordinator', 'User1', '1970-12-25', '77777777Z', 'MALE', 9000),
  (90008, 'PROFESSIONAL', 90007, null, null, null, 'testHired1', '$2a$10$5/Rw1/iWVdjxFPOneDDKVetj5c0zoizB7xTyK.eb58inPKC0mLvzG', false, 'Test', 'Hired', 'User1', '1970-02-28', '66666666Z', 'FEMALE', 3400),
  (90009, 'PROFESSIONAL', 90007, null, null, 90601, 'testHired2', '$2a$10$xOvmgoioTN0LVetRpBrjSOfkk/aGVwVysXXstdoOVuVQZSr/lwYum', false, 'Test', 'Hired', 'User2', '1970-03-25', '55555555Z', 'MALE', 9000);

INSERT INTO field (id, name) VALUES
  (90200, 'Field1'),
  (90201, 'Field2'),
  (90202, 'Field3');

INSERT INTO service_type (id, name, field_id, price, mode) VALUES
  (90300, 'Service Field1 Async', 90200, 500, 'ASYNC'),
  (90301, 'Service Field1 Video', 90200, 1000, 'VIDEO'),
  (90302, 'Service Field1 Video2', 90200, 1500, 'VIDEO');

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

INSERT INTO session (id, title, start_date, end_date, state, timestamp) VALUES
  (90700, 'testSession1', '2015-03-02 17:00:00', '2015-03-02 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90701, 'testSession2', '2015-03-01 12:00:00', '2015-03-01 12:15:00', 'CANCELED', '2014-12-15 09:00:00'),
  (90702, 'testSession3', '2015-03-03 13:00:00', '2015-03-01 12:15:00', 'CLOSED', '2014-12-15 19:00:00'),
  (90703, 'testSession4', '2015-03-04 14:00:00', '2015-03-01 12:15:00', 'FINISHED', '2014-12-15 12:00:00'),
  (90704, 'testSession5', '2015-03-05 17:00:00', '2015-03-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90705, 'testSession6', '2015-04-06 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90706, 'testSession7', '2015-04-07 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90707, 'testSession8', '2015-04-08 17:00:00', '2015-04-12 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90708, 'testSession9', '2015-04-09 17:00:00', '2015-04-12 17:15:00', 'CLOSED', '2014-12-15 08:00:00'),
  (90709, 'testSession10', '2015-05-12 17:00:00', '2015-05-20 17:15:00', 'CLOSED', '2014-12-15 08:00:00'),
  (90710, 'testSession11', '2015-05-22 17:00:00', '2015-05-23 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00'),
  (90711, 'testSession12', '2015-06-27 17:00:00', '2015-06-30 17:15:00', 'PROGRAMMED', '2014-12-15 08:00:00');

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

INSERT INTO history_current (id, client_user_id, professional_user_id, modification_date) VALUES
  (90900, 90000, 90005, '2014-12-16 10:00:00'),
  (90901, 90001, 90008, '2014-12-16 11:00:00');

INSERT INTO access_log (id, user_id, history_current_id, timestamp) VALUES
  (91000, 90005, 90900, '2014-12-16 10:00:00'),
  (91001, 90005, 90900, '2014-12-17 09:00:00');

INSERT INTO history_entry_type (id) VALUES
  ('Allergies'),
  ('Treatments');

INSERT INTO history_entry (id, history_current_id, history_entry_type_id, title, timestamp) VALUES
  (91100, 90900, 'Allergies', 'Lactosa', '2014-12-15 08:30:00'),
  (91101, 90900, 'Allergies', 'Gluten', '2014-12-15 08:30:00'),
  (91102, 90900, 'Treatments', 'Caminar', '2014-12-15 08:30:00');

INSERT INTO attachment (id, history_entry_id, session_id, user_id, path) VALUES
  (91200, 91102, null, 90005, 'infographic.pdf'),
  (91201, null, 90700, 90000, 'xray.jpg');

INSERT INTO transaction (id, user_id, session_id, amount, timestamp, reason) VALUES
  (91300, 90000, 90700, 20001000, '2014-12-15 06:00:00', 'Incremento de saldo'),
  (91301, 90000, 90700, -1000, '2014-12-15 08:00:00', 'Reserva de sesión'),
  (91302, 90000, 90701, -1000, '2014-12-15 09:00:00', 'Reserva de sesión'),
  (91303, 90000, 90701, 1000, '2014-12-15 12:00:00', 'Devolución sesión cancelada');

INSERT INTO history_archive (id, client_user_id, professional_user_id, creation_date, archive_date, meta) VALUES
  (91400, 90000, 90005, '2014-12-16 10:00:00', '2014-12-16 10:00:00', '{}');


# --- !Downs


SET search_path TO brahma, PUBLIC;
TRUNCATE history_archive, transaction, attachment, history_entry, history_entry_type, access_log, history_current, session_log, session, notification, availability, service, service_type, field, "user", institution, collective CASCADE;