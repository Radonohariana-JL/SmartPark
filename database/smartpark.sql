-- ============================================================
-- SmartPark - Base de donnees PostgreSQL
-- Conception Merise - Projet Java Swing / JDBC
-- Execution : psql -U postgres -h localhost -d smartpark -f database/smartpark.sql
-- ============================================================

-- ------------------------------------------------------------
-- 1. Creation de la base (a executer une seule fois, depuis psql)
-- ------------------------------------------------------------
-- CREATE DATABASE smartpark;

-- ------------------------------------------------------------
-- 2. Tables
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur  SERIAL PRIMARY KEY,
    nom             VARCHAR(80)  NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(80)  NOT NULL,
    role            VARCHAR(30)  NOT NULL DEFAULT 'ADMIN'
);

CREATE TABLE IF NOT EXISTS proprietaire (
    id_proprietaire SERIAL PRIMARY KEY,
    nom             VARCHAR(80) NOT NULL,
    prenom          VARCHAR(80) NOT NULL,
    telephone       VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicule (
    id_vehicule     SERIAL PRIMARY KEY,
    immatriculation VARCHAR(30) NOT NULL UNIQUE,
    marque          VARCHAR(60) NOT NULL,
    couleur         VARCHAR(40) NOT NULL,
    type_vehicule   VARCHAR(20) NOT NULL,
    id_proprietaire INTEGER NOT NULL REFERENCES proprietaire(id_proprietaire),
    CONSTRAINT ck_type_vehicule CHECK (type_vehicule IN ('Moto', 'Voiture', 'Camion'))
);

CREATE TABLE IF NOT EXISTS place (
    id_place     SERIAL PRIMARY KEY,
    numero_place VARCHAR(10) NOT NULL UNIQUE,
    etat         VARCHAR(20) NOT NULL DEFAULT 'LIBRE',
    CONSTRAINT ck_etat_place CHECK (etat IN ('LIBRE', 'OCCUPEE'))
);

CREATE TABLE IF NOT EXISTS tarif (
    id_tarif      SERIAL PRIMARY KEY,
    type_vehicule VARCHAR(20) NOT NULL UNIQUE,
    prix_heure    NUMERIC(10,2) NOT NULL,
    CONSTRAINT ck_type_tarif CHECK (type_vehicule IN ('Moto', 'Voiture', 'Camion'))
);

CREATE TABLE IF NOT EXISTS stationnement (
    id_stationnement SERIAL PRIMARY KEY,
    date_entree      TIMESTAMP   NOT NULL,
    date_sortie      TIMESTAMP,
    duree            NUMERIC(6,2),
    montant          NUMERIC(10,2),
    statut           VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
    id_vehicule      INTEGER NOT NULL REFERENCES vehicule(id_vehicule),
    id_place         INTEGER NOT NULL REFERENCES place(id_place),
    CONSTRAINT ck_statut_stationnement CHECK (statut IN ('EN_COURS', 'TERMINE'))
);

-- ------------------------------------------------------------
-- 3. Donnees initiales
-- ------------------------------------------------------------

-- Utilisateur administrateur de test
-- Le mot de passe est stocke en hachage SHA-256 hexadecimal (mot de passe
-- initial : 'admin123').
INSERT INTO utilisateur (nom, email, mot_de_passe, role)
VALUES ('Administrateur', 'admin@smartpark.com',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

-- Tarifs
INSERT INTO tarif (type_vehicule, prix_heure) VALUES
    ('Moto',    200.00),
    ('Voiture', 500.00),
    ('Camion',  1000.00)
ON CONFLICT (type_vehicule) DO NOTHING;

-- 20 places
INSERT INTO place (numero_place, etat)
SELECT 'P' || LPAD(i::text, 2, '0'), 'LIBRE'
FROM generate_series(1, 20) AS i
ON CONFLICT (numero_place) DO NOTHING;

-- Proprietaires de demonstration
INSERT INTO proprietaire (nom, prenom, telephone)
SELECT * FROM (VALUES
    ('Rakoto',   'Jean',   '034 11 22 333'),
    ('Rasoa',    'Marie',  '032 44 55 666'),
    ('Andria',   'Hery',   '033 77 88 999')
) AS v(nom, prenom, telephone)
WHERE NOT EXISTS (SELECT 1 FROM proprietaire);

-- Vehicules de demonstration
INSERT INTO vehicule (immatriculation, marque, couleur, type_vehicule, id_proprietaire)
SELECT * FROM (VALUES
    ('2145 TBA', 'Toyota',   'Blanc',  'Voiture', 1),
    ('4578 TCA', 'Honda',    'Rouge',  'Moto',    2),
    ('7896 TDZ', 'Renault',  'Bleu',   'Camion',  3),
    ('1122 TAV', 'Hyundai',  'Noir',   'Voiture', 3)
) AS v(immatriculation, marque, couleur, type_vehicule, id_proprietaire)
WHERE NOT EXISTS (SELECT 1 FROM vehicule);

-- Quelques stationnements termines (pour l'historique et les statistiques)
INSERT INTO stationnement (date_entree, date_sortie, duree, montant, statut, id_vehicule, id_place)
SELECT * FROM (VALUES
    (now() - interval '7 days', now() - interval '7 days' + interval '2 hours', 2, 1000, 'TERMINE', 1, 1),
    (now() - interval '6 days', now() - interval '6 days' + interval '1 hour',  1,  500, 'TERMINE', 2, 2),
    (now() - interval '5 days', now() - interval '5 days' + interval '3 hours', 3, 3000, 'TERMINE', 3, 3),
    (now() - interval '2 days', now() - interval '2 days' + interval '2 hours', 2, 1000, 'TERMINE', 4, 4),
    (now() - interval '1 day',  now() - interval '1 day'  + interval '4 hours', 4, 2000, 'TERMINE', 1, 5)
) AS v(date_entree, date_sortie, duree, montant, statut, id_vehicule, id_place)
WHERE NOT EXISTS (SELECT 1 FROM stationnement);

-- Un vehicule actuellement garant (pour le tableau de bord)
INSERT INTO stationnement (date_entree, statut, id_vehicule, id_place)
SELECT now() - interval '45 minutes', 'EN_COURS', 2, 6
WHERE NOT EXISTS (SELECT 1 FROM stationnement WHERE statut = 'EN_COURS');
UPDATE place SET etat = 'OCCUPEE' WHERE id_place IN (
    SELECT id_place FROM stationnement WHERE statut = 'EN_COURS'
);
