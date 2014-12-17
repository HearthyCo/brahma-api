---------------------------------------
-- Brahma initial database evolution --
---------------------------------------


# --- !Ups


-- Schema --

CREATE SCHEMA IF NOT EXISTS brahmatest;
SET search_path TO brahmatest, PUBLIC;


-- Enums --

CREATE TYPE user_type AS ENUM ('PROFESSIONAL', 'CLIENT', 'TUTOR', 'COORDINATOR');
CREATE TYPE gender AS ENUM ('MALE', 'FEMALE', 'OTHER');
CREATE TYPE service_mode AS ENUM ('ASYNC', 'VIDEO');
CREATE TYPE session_action AS ENUM ('ACCEPT', 'JOIN', 'CLOSE', 'FINISH', 'REJECT', 'AWAY', 'ABORT');
CREATE TYPE session_state AS ENUM ('REQUESTED', 'PROGRAMMED', 'UNDERWAY', 'CLOSED', 'FINISHED', 'CANCELED');


-- Tables --

CREATE TABLE IF NOT EXISTS collective (
  id   SERIAL NOT NULL,
  name TEXT   NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS institution (
  id   SERIAL NOT NULL,
  name TEXT   NOT NULL,
  meta JSON   NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "user" (
  id                         SERIAL    NOT NULL,
  type                       user_type NOT NULL,
  manager_user_id            INT       NULL,
  tutor_user_id              INT       NULL,
  collective_collective_id   INT       NULL,
  institution_institution_id INT       NULL,
  login                      TEXT      NOT NULL,
  password                   TEXT      NULL,
  can_login                  BOOLEAN   NOT NULL,
  name                       TEXT      NOT NULL,
  surname1                   TEXT      NULL,
  surname2                   TEXT      NULL,
  birthdate                  DATE      NOT NULL,
  avatar                     TEXT      NULL,
  national_id                TEXT      NULL,
  gender                     gender    NOT NULL,
  online_limit               TIMESTAMP NULL,
  admin_level                INT       NULL DEFAULT 0,
  meta                       JSON      NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_user_user1
  FOREIGN KEY (manager_user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_user_user2
  FOREIGN KEY (tutor_user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_user_collective1
  FOREIGN KEY (collective_collective_id)
  REFERENCES collective (id),
  CONSTRAINT fk_user_institution1
  FOREIGN KEY (institution_institution_id)
  REFERENCES institution (id)
);
CREATE UNIQUE INDEX user_login ON "user" (login ASC);

CREATE TABLE IF NOT EXISTS notification (
  id                SERIAL    NOT NULL,
  user_id           INT       NOT NULL,
  type              TEXT      NOT NULL,
  meta              JSON      NOT NULL,
  creation_date     TIMESTAMP NOT NULL,
  notification_date TIMESTAMP NOT NULL,
  delivered_date    TIMESTAMP NULL,
  viewed_date       TIMESTAMP NULL,
  done_date         TIMESTAMP NULL,
  reply             JSON      NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_notification_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id)
);
CREATE INDEX notification_user_id ON notification (user_id ASC);

CREATE TABLE IF NOT EXISTS field (
  id   SERIAL NOT NULL,
  name TEXT   NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS service_type (
  id       SERIAL       NOT NULL,
  field_id INT          NOT NULL,
  price    INT          NOT NULL,
  name     TEXT         NOT NULL,
  mode     service_mode NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_service_type_field1
  FOREIGN KEY (field_id)
  REFERENCES field (id)
);

CREATE TABLE IF NOT EXISTS service (
  id              SERIAL NOT NULL,
  user_id         INT    NOT NULL,
  service_type_id INT    NOT NULL,
  earnings        INT    NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_service_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_service_service_type1
  FOREIGN KEY (service_type_id)
  REFERENCES service_type (id)
);
CREATE INDEX service_user_id ON service (user_id ASC);
CREATE INDEX service_service_type_id ON service (service_type_id ASC);

CREATE TABLE IF NOT EXISTS availability (
  id                  SERIAL NOT NULL,
  user_id             INT    NOT NULL,
  repeat_start_date   DATE   NOT NULL,
  repeat_end_date     DATE   NULL,
  schedule_start_time TIME   NOT NULL,
  schedule_end_time   TIME   NOT NULL,
  repeat              INT    NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_availability_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id)
);
CREATE INDEX availability_user_id ON availability (user_id ASC);

CREATE TABLE IF NOT EXISTS session (
  id                           SERIAL        NOT NULL,
  client_user_id               INT           NOT NULL,
  client_notification_id       INT           NULL,
  professional_user_id         INT           NOT NULL,
  professional_notification_id INT           NULL,
  service_id                   INT           NOT NULL,
  availability_id              INT           NOT NULL,
  start_date                   TIMESTAMP     NOT NULL,
  end_date                     TIMESTAMP     NULL,
  state                        session_state NOT NULL,
  report                       TEXT          NULL,
  meta                         JSON          NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_session_user1
  FOREIGN KEY (client_user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_session_user2
  FOREIGN KEY (professional_user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_session_notification1
  FOREIGN KEY (client_notification_id)
  REFERENCES notification (id),
  CONSTRAINT fk_session_notification2
  FOREIGN KEY (professional_notification_id)
  REFERENCES notification (id),
  CONSTRAINT fk_session_service1
  FOREIGN KEY (service_id)
  REFERENCES service (id),
  CONSTRAINT fk_session_availability1
  FOREIGN KEY (availability_id)
  REFERENCES availability (id)
);
CREATE INDEX session_client_user_id ON session (client_user_id ASC);
CREATE INDEX session_professional_user_id ON session (professional_user_id ASC);
CREATE INDEX session_availability_id ON session (availability_id ASC);

CREATE TABLE IF NOT EXISTS session_log (
  id         SERIAL         NOT NULL,
  session_id INT            NOT NULL,
  user_id    INT            NOT NULL,
  timestamp  TIMESTAMP      NOT NULL,
  action     session_action NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_session_log_session1
  FOREIGN KEY (session_id)
  REFERENCES session (id),
  CONSTRAINT fk_session_log_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id)
);
CREATE INDEX session_log_session_id ON session_log (user_id ASC);

CREATE TABLE IF NOT EXISTS transaction (
  id         SERIAL    NOT NULL,
  user_id    INT       NOT NULL,
  session_id INT       NULL,
  amount     INT       NOT NULL,
  timestamp  TIMESTAMP NOT NULL,
  reason     TEXT      NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_transaction_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_transaction_session1
  FOREIGN KEY (session_id)
  REFERENCES session (id)
);
CREATE INDEX transaction_user_id ON transaction (user_id ASC);

CREATE TABLE IF NOT EXISTS history_entry_type (
  id TEXT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS history_current (
  id                   SERIAL    NOT NULL,
  client_user_id       INT       NOT NULL,
  professional_user_id INT       NULL,
  modification_date    TIMESTAMP NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_history_current_user1
  FOREIGN KEY (client_user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_history_current_user2
  FOREIGN KEY (professional_user_id)
  REFERENCES "user" (id)
);
CREATE INDEX history_current_client_user_id ON history_current (client_user_id ASC);

CREATE TABLE IF NOT EXISTS history_entry (
  id                    SERIAL    NOT NULL,
  history_current_id    INT       NOT NULL,
  history_entry_type_id TEXT      NOT NULL,
  title                 TEXT      NOT NULL,
  timestamp             TIMESTAMP NOT NULL,
  description           TEXT      NULL,
  meta                  JSON      NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_history_entry_history_current1
  FOREIGN KEY (history_current_id)
  REFERENCES history_current (id),
  CONSTRAINT fk_history_entry_history_entry_type1
  FOREIGN KEY (history_entry_type_id)
  REFERENCES history_entry_type (id)
);
CREATE INDEX history_entry_history_current_id ON history_entry (history_current_id ASC);


CREATE TABLE IF NOT EXISTS history_archive (
  id                   SERIAL    NOT NULL,
  client_user_id       INT       NOT NULL,
  professional_user_id INT       NULL,
  creation_date        TIMESTAMP NOT NULL,
  archive_date         TIMESTAMP NOT NULL,
  meta                 JSON      NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_history_archive_user1
  FOREIGN KEY (client_user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_history_archive_user2
  FOREIGN KEY (professional_user_id)
  REFERENCES "user" (id)
);
CREATE INDEX history_archive_client_user_id ON history_archive (client_user_id ASC);

CREATE TABLE IF NOT EXISTS access_log (
  id                 SERIAL    NOT NULL,
  user_id            INT       NOT NULL,
  history_current_id INT       NOT NULL,
  timestamp          TIMESTAMP NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_access_log_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id),
  CONSTRAINT fk_access_log_history_current1
  FOREIGN KEY (history_current_id)
  REFERENCES history_current (id)
);
CREATE INDEX access_log_user_id ON access_log (user_id ASC);
CREATE INDEX access_log_history_current_id ON access_log (history_current_id ASC);

CREATE TABLE IF NOT EXISTS prescription (
  id               SERIAL    NOT NULL,
  history_entry_id INT       NOT NULL,
  name             TEXT      NOT NULL,
  timestamp        TIMESTAMP NOT NULL,
  length           INT       NOT NULL,
  interval         INT       NOT NULL,
  dosage           FLOAT     NOT NULL,
  meta             JSON      NULL,
  notification_id  INT       NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_prescription_history_entry1
  FOREIGN KEY (history_entry_id)
  REFERENCES history_entry (id),
  CONSTRAINT fk_prescription_notification1
  FOREIGN KEY (notification_id)
  REFERENCES notification (id)
);
CREATE INDEX prescription_history_entry_id ON prescription (history_entry_id ASC);

CREATE TABLE IF NOT EXISTS attachment (
  id               SERIAL NOT NULL,
  history_entry_id INT    NULL,
  session_id       INT    NULL,
  user_id          INT    NULL,
  path             TEXT   NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_attachment_history_entry1
  FOREIGN KEY (history_entry_id)
  REFERENCES history_entry (id),
  CONSTRAINT fk_attachment_session1
  FOREIGN KEY (session_id)
  REFERENCES session (id),
  CONSTRAINT fk_attachment_user1
  FOREIGN KEY (user_id)
  REFERENCES "user" (id)
);
CREATE INDEX attachment_history_entry_id ON attachment (history_entry_id ASC);
CREATE INDEX attachment_session_id ON attachment (session_id ASC);
CREATE INDEX attachment_user_id ON attachment (user_id ASC);


# --- !Downs


-- Delete everything --
SET search_path TO brahmatest, PUBLIC;
DROP SCHEMA IF EXISTS brahmatest CASCADE;