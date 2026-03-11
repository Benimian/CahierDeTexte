CREATE TABLE IF NOT EXISTS utilisateur (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom TEXT NOT NULL,
    prenom TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    mot_de_passe TEXT NOT NULL,
    role TEXT NOT NULL,
    valide INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cours (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    intitule TEXT NOT NULL,
    volume_horaire INTEGER,
    enseignant_id INTEGER,
    classe TEXT,
    FOREIGN KEY (enseignant_id) REFERENCES utilisateur(id)
);

CREATE TABLE IF NOT EXISTS seance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cours_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    heure TEXT NOT NULL,
    duree INTEGER NOT NULL,
    contenu TEXT,
    observations TEXT,
    statut TEXT DEFAULT 'EN_ATTENTE',
    commentaire_rejet TEXT,
    FOREIGN KEY (cours_id) REFERENCES cours(id)
);

INSERT OR IGNORE INTO utilisateur (nom, prenom, email, mot_de_passe, role, valide)
VALUES ('Admin', 'Chef', 'chef@esitec.sn', 'admin123', 'CHEF', 1);
