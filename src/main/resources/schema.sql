-- Borrar tablas si existen (orden inverso a las FK)
DROP TABLE IF EXISTS penalty CASCADE;
DROP TABLE IF EXISTS maintenance_request CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS facility CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users ( -- in PostgreSQL, 'user' is a reserved keyword
  userid SERIAL PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  email VARCHAR(254) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL
);

CREATE TABLE facility (
  facilityid SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE reservation (
  reservationid SERIAL PRIMARY KEY,
  userid INT NOT NULL,
  facilityid INT NOT NULL,
  date TIMESTAMP NOT NULL,
  starttime TIME NOT NULL,
  endtime TIME NOT NULL,
  participants INT NOT NULL,
  purpose VARCHAR(250),

  FOREIGN KEY (userid) REFERENCES users(userid) ON DELETE CASCADE,
  FOREIGN KEY (facilityid) REFERENCES facility(facilityid) ON DELETE CASCADE
);

CREATE TABLE maintenance_request (
  requestid SERIAL PRIMARY KEY,
  userid INT NOT NULL,
  facilityid INT NOT NULL,
  staffid INT NULL,
  description TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  reportdate TIMESTAMP NOT NULL,
  issuetype VARCHAR(100) NOT NULL,
  severity VARCHAR(100) NOT NULL,

  FOREIGN KEY (userid) REFERENCES users(userid) ON DELETE CASCADE,
  FOREIGN KEY (facilityid) REFERENCES facility(facilityid) ON DELETE CASCADE,
  FOREIGN KEY (staffid) REFERENCES users(userid) ON DELETE SET NULL
);

CREATE TABLE penalty (
  penaltyid SERIAL PRIMARY KEY,
  userid INT NOT NULL,
  description TEXT NOT NULL,
  datehour TIMESTAMP NOT NULL,

  FOREIGN KEY (userid) REFERENCES users(userid) ON DELETE CASCADE
);