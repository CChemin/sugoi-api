## Concepts 

### Use cases

* Read and write information on users and organization
* Check permissions on an application, add and remove permissions

### Terminology

**Realm** : base unit for sugoi, each realm is isolated from one another by permissions. This represents a set of sugoi objects (users, organizations, applications) managed together.

**UserStorage** : storage for users and organizations in a *realm*. A *realm* can have multiple user storages. Each user storage can be at a different location and have its own user storage type (ldap, file, broker) corresponding to a *store provider*.

**Store Provider** : 

**User** : Represents an account. A user bears profile data (name, address, username...), roles and authentification secrets. A user can belong to *groups*.

**Organization** : Represents an organizational unit. An organization can have an address, secrets, suborganization... Users can belong to organizations.

**Application** : Represents an information system with which users and organizations can interact. Application belongs to a realm. Application is used to define roles on the corresponding information system with the *group* concept.

**Group** : A group is a unit of an *application* which contains *users*. The information system requesting sugoi uses groups to determine the users' status. Permissions on the information system can be managed via groups.

### Access control and permissions

Each users with authentification information in a realm can authenticate on sugoi.
There are three profiles on sugoi permission model : admin, read and write. The permission is defined by the fact of belonging to one of the corresponding sugoi *group* as defined in configuration.

#### Administration

The admin group is defined via the regexp.role.admin property.

An administrator can :

* read, create, modify or delete realms.
* read, create, modify or delete userstorages.
* do whatever the writer can do on all realms.

#### Writer

The writer group is defined via the regexp.role.writer property. It is scoped to one or several realms.

A writer can :

* modify and create users on the realms they manage
* modify and create organizations on the realms they manage
* modify and create application and groups on the realms they manage
* do whatever a reader can do on the realms they manage.

#### Reader

The reader group is defined via the regexp.role.reader property. It is scoped to one or several realms.

A reader can :

* read users, organizations, applications and groups on the realm they are reader on.

FRENCH :

Realm: Premier niveau d’isolation de sugoi. Un ensemble d’objet sugoi ayant vocation a étre géré par une meme entité. Pour l’insee, représente un domaine applicatif cohérent. (=domaine)

UserStorage: Chaque realm peut posséder un ou plusieurs UserStorage, permettant d’isoler un peu les comptes au sein de chaque realm (par exemple entre agents Insee et agents SSM)

User: Utilisateur enregistré dans sugoi. Ils possèdent des informations de profil (nom, prénom, nom d’utilisateur, adresse…), des rôles (selon différentes formes), et des secrets d’authentification (pour s’identifier sur des applications)

Organization: Une organisation a laquelle appartient un utilisateur. Une organisation peut posséder une adresse, et des secrets (par exemple une clef de chiffrement)

Application: Une application représente un système d’information surlequel les users ou organization peuvent agir. L’application appartient à un realm.

Groupe: Un groupe contient un ensemble d’utilisateur du realm et appartient à une application. Il peut permettre de gérer les droits sur cette application