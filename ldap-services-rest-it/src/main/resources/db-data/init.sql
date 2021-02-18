CREATE TABLE contacts
(
  identifiant varchar NOT NULL,
  domainegestion varchar,
  ldapurl varchar,
  CONSTRAINT contacts_pkey PRIMARY KEY (identifiant)
);



  
CREATE TABLE courriers
(
  identifiantcontact varchar NOT NULL,
  modelecourrier varchar NOT NULL,
  adressepostaleligneune varchar,
  adressepostalelignedeux varchar,
  adressepostalelignetrois varchar,
  adressepostalelignequatre varchar,
  adressepostalelignecinq varchar,
  adressepostalelignesix varchar,
  adressepostalelignesept varchar,
  date varchar NOT NULL,
  nomdirection varchar,
  nomdepartement varchar,
  nomservice varchar,
  nomapplicationlettre varchar,
  chefsignataire varchar,
  nomsignataire varchar,
  urlsite varchar,
  hotlinetel varchar,
  hotlinefax varchar,
  hotlinemail varchar,
  ueidentifiant varchar,
  nomcommun varchar,
  password varchar,
  logo varchar,
  civilite varchar,
  identifiantent varchar,
  mail varchar,
  CONSTRAINT courriers_pkey PRIMARY KEY (identifiantcontact)
);


  
CREATE TABLE organisations
(
  identifiant varchar NOT NULL,
  domainegestion varchar,
  ldapurl varchar,
  CONSTRAINT organisations_pkey PRIMARY KEY (identifiant)
);

INSERT into contacts(identifiant, domainegestion) values ('testc','domaine1');
INSERT into contacts(identifiant, domainegestion) values ('sloopy1', 'testbatch');
INSERT into contacts(identifiant, domainegestion) values ('intru', 'testbatch');
INSERT into contacts(identifiant, domainegestion) values ('intru2', 'domaineintru');
INSERT into contacts(identifiant, domainegestion) values ('sloopyunique', 'nouniqueid');
INSERT into contacts(identifiant, domainegestion) values ('sloopyunique2', 'nouniqueid');

INSERT into organisations(identifiant, domainegestion) values ('testo','domaine1');
INSERT into organisations(identifiant, domainegestion) values ('orgintru', 'testbatch');
INSERT into organisations(identifiant, domainegestion) values ('orga2', 'testbatch');
INSERT into organisations(identifiant, domainegestion) values ('orga3', 'domainpasdunicite');
INSERT into organisations(identifiant, domainegestion) values ('intru3', 'domaineintru');
INSERT into organisations(identifiant, domainegestion) values ('orgaunique', 'nouniqueid');
