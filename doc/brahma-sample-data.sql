TRUNCATE brahma.availability, brahma.service, brahma.service_type, brahma.field, brahma.user, brahma.collective, brahma.institution CASCADE;


INSERT INTO brahma.collective (id, name) VALUES
    (90100, 'Collective1'),
    (90101, 'Collective2');

INSERT INTO brahma.institution (id, name, meta) VALUES
    (90600, 'Institution1', '{}'),
    (90601, 'Institution2', '{}');

INSERT INTO brahma.user (id, type, manager_user_id, tutor_user_id, collective_collective_id, institution_institution_id, login, password, can_login, name, surname1, surname2, birthdate, national_id, gender) VALUES
    (90000, 'CLIENT', null, null, null, null, 'testClient1', '$2a$10$ourB/XLcJaHUAEEAvrNAZ.WcNBInH1o56UuqNiXWwLVXyE00U7IA2', true, 'Test', 'Client', 'User1', '1987-12-25', '12345678Z', 'FEMALE'),
    (90001, 'CLIENT', null, null, 90100, null, 'testClient2', '$2a$10$cTIH1wLXliX.3f8jRzaFfu.OJJk57xKLg1YBaze9m4o83IqUltkWa', true, 'Test', 'Client', 'User2', '1985-06-21', '23456789Z', 'MALE'),
    (90002, 'TUTOR', null, null, null, null, 'testTutor1', '$2a$10$JeqiNGmF1AuLNReD5dOrR.olhb3WKgVQNrmOovlWHM80nOeT7l2IS', true, 'Test', 'Tutor', 'User1', '1999-01-01', '34567890Z', 'FEMALE'),
    (90003, 'CLIENT', null, 90002, null, null, 'testPet1', null, false, 'Test', 'Client', 'Pet1', '2014-12-01', null, 'MALE'),
    (90004, 'CLIENT', null, 90002, 90101, null, 'testPet2', null, false, 'Test', 'Client', 'Pet2', '2014-11-01', null, 'FEMALE'),
    (90005, 'PROFESSIONAL', null, null, null, null, 'testProfessional1', '$2a$10$E/4oidCPkiPwSHDOhIIkLOO6mzfNiZRsKaIbRxqZ1yc0wqEcvt3sm', false, 'Test', 'Professional', 'User1', '1970-01-01', '99999999Z', 'MALE'),
    (90006, 'PROFESSIONAL', null, null, null, 90600, 'testProfessional2', '$2a$10$bx/i5DemogHrF1zpclbdZOaXd7EvbWdkHDYNgZAFIfJZRQ7SKoTYy', false, 'Test', 'Professional', 'User2', '1970-12-31', '88888888Z', 'FEMALE'),
    (90007, 'COORDINATOR', null, null, null, null, 'testCoordinator1', '$2a$10$iQfb4CosbqhfVeifnr7Cx.tAUx1gmikEzHXznqd4JgFCe1STaaqsy', false, 'Test', 'Coordinator', 'User1', '1970-12-25', '77777777Z', 'MALE'),
    (90008, 'PROFESSIONAL', 90007, null, null, null, 'testHired1', '$2a$10$5/Rw1/iWVdjxFPOneDDKVetj5c0zoizB7xTyK.eb58inPKC0mLvzG', false, 'Test', 'Hired', 'User1', '1970-02-28', '66666666Z', 'FEMALE'),
    (90009, 'PROFESSIONAL', 90007, null, null, 90601, 'testHired2', '$2a$10$xOvmgoioTN0LVetRpBrjSOfkk/aGVwVysXXstdoOVuVQZSr/lwYum', false, 'Test', 'Hired', 'User2', '1970-03-25', '55555555Z', 'MALE');

INSERT INTO brahma.field (id, name) VALUES
    (90200, 'Field1'),
    (90201, 'Field2');

INSERT INTO brahma.service_type (id, name, field_id, price, mode) VALUES
    (90300, 'Service Field1 Async', 90200, 500, 'ASYNC'),
    (90301, 'Service Field1 Video', 90200, 1000, 'VIDEO'),
    (90302, 'Service Field1 Video2', 90200, 1500, 'VIDEO');

INSERT INTO brahma.service (id, user_id, service_type_id, earnings) VALUES
    (90400, 90005, 90300, 400),
    (90401, 90005, 90301, 900),
    (90402, 90008, 90301, 1400);

INSERT INTO brahma.availability (id, user_id, repeat_start_date, repeat_end_date, schedule_start_time, schedule_end_time, repeat) VALUES
    (90500, 90005, '2014-12-01', null, '08:00:00', '13:00:00', 31),
    (90501, 90005, '2014-12-02', null, '16:00:00', '19:00:00', 31),
    (90502, 90008, '2014-12-01', null, '08:00:00', '13:00:00', 96);

