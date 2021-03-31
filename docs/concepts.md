# Concepts

## Use cases

* Read and write information on users and organization
* Validate user secrets for authentication (password or X.509 client certificate)
* Check users permissions on an application, add and remove permissions
* Manage groups of users for applications

Users, organizations and applications can come from multiple storages and can be functionnaly isolated with realms.

## Terminology

**Realm** : base unit for sugoi, each realm is isolated from one another by permissions. Realm represents a set of sugoi objects (users, organizations, applications) managed together. *Users* can have read or write rights scoped to a realm.

**UserStorage** : storage for users and organizations in a *realm*. A *realm* can have multiple user storages. Each user storage can be at a different location and have its own user storage type (ldap, file, broker) corresponding to a *store provider*.

**Store Provider** : the way to access data. For now there are 3 store providers avalaible : local file, ldap and broker.

**User** : Represents an individual. It bears profile data (name, address, username...). It may also bear roles and authentification secrets and will then represent an account. A user can belong to *groups*.

**Organization** : Represents an organizational unit. An organization can have an address, secrets, suborganization... Users can belong to organizations.

**Application** : Represents an information system with which users and organizations can interact. Applications belong to a realm. Application is used to define roles for users on the corresponding information system with the *group* concept.

**Group** : A group is a unit of an *application* which contains *users*. The information system requesting sugoi uses groups to determine the users' status. Permissions on the information system can be managed via groups.

## Access control and permissions

Users can authenticate on sugoi with basic authentication, bearer authentication or ldap authentication. For now users will have rights on the application only by using ldap authentication. The permissions are checked from groups defined in security.ldap-account-managment-url property (see [configuration](configuration.md)).

There are five profiles on sugoi permission model : admin, read and write, password and application manager. A user have a right when it belong to the corresonding ldap group. All profiles are scoped to realm except the admin group. Sugoi management groups are entirely configurable with regex.

### Administrator

The admin group is defined via the regexp.role.admin property.

An administrator can :

* read, create, modify or delete any realms.
* read, create, modify or delete userstorages.
* do whatever the writer can do on all realms.

### Writer

The writer group is defined via the regexp.role.writer property. It is scoped to one realm.

A writer can :

* modify and create users on the realms they manage
* modify and create organizations on the realms they manage
* modify and create application and groups on the realms they manage
* do whatever a reader can do on the realms they manage.

### Reader

The reader group is defined via the regexp.role.reader property. It is scoped to one realm.

A reader can :

* read users, organizations, applications and groups on the realm they are reader on.

### Password manager

The password manager is defined via the regexp.role.password.manager property. It is scoped to one realm.

Password manager can initialize passwords, update password (with a given password or not) or validate a password on its realm.

### Application manager
