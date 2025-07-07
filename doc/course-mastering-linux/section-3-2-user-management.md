
### 3.2. User management in Linux

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

This is about user management in Linux, not on a Mac.

There are 3 kinds of users:
* *root users*:
    * highest privileges
    * the root user has user ID 0
    * a system can have only 1 root user
* *regular users*:
    * limited privileges
    * they can temporarily get root access through `sudo`
* *service users*:
    * for specific tasks, in order to safely run a web server, database etc.
    * they have no home directory

All users have a *primary group*. And all users can be assigned to 0 or more *additional groups*.

User management data is stored in several files (which should certainly not be edited by us):
* `/etc/passwd`:
    * contains basic user account information (not passwords): username, an `x`, user ID, primary group ID, description, home dir, default shell
    * all users can read this file
* `/etc/shadow`:
    * contains encrypted user passwords, password aging information, but also date of last password change, expiry date etc.
    * only readable with root privileges
* `/etc/group`:
    * contains information about groups and group members: group name, password (or `x`), group ID, comma-separated list of group members
    * all users can read this file

Managing *users* through commands `useradd`, `userdel`, `usermod` and `passwd`:
* adding a user: `useradd`
    * options: `-m` (create home directory), `-d` (set custom home directory), `-s` (specify default shell)
    * group-related options: `-g` (specify primary group instead of using default), `-G` (add user to secondary groups)
    * example: `sudo useradd -m -s /bin/sh -G wheel new-user`
    * do not forget assign a password to the user (via command `passwd`), or else this user can not login
* setting a password, etc.: `passwd`
    * options: `-S` (show password status), `-d` (delete password), `-n`/`-x` (set min/max password age in days), `-l`/`-u` (lock/unlock user)
    * examples: `sudo passwd -x 90 new-user` (set max password age in days), `sudo passwd new-user` (set password for the user)
    * example to change our own password (without needing `sudo`): `passwd`
* changing user details: `usermod`
    * options: `-c` (change the user description, i.e. full name), `-s` (change the default shell)
    * options we should refrain from using: `-d` (change home dir, `-m` also moves current home dir to new one), `-l` (change user name)
    * group-related options: `-g` (should we want that?), `-G` (change secondary groups), `-aG` (add secondary group)
    * example: `sudo usermod -s /bin/bash some-user`
    * the user can change his/her own shell (to one in `/etc/shells`): `chsh -s /bin/bash`
* deleting a user: `userdel`
    * options: `-r` (removes home directory and mails), `-f` (like `-r`, but also forcefully deletes the user, even if logged in)
    * exampe: `sudo userdel -r some-user`

Depending on the system, there might also be commands like `adduser` and `deluser`, passing a user and group as parameters.

The concept of *groups* simplifies permission management and strengthens system security; user-based permissions would be
too fine-grained.

*Groups* have the following characteristics:
* each user has exactly one *primary group* (stored in `/etc/passwd`) and 0 or more *secondary groups*
* the primary is default owner of files created by the user; check that with `touch file.txt; ls -l file.txt`
* obviously, secondary groups can have multiple members (see `/etc/group`)
* list a user's groups with `groups the-user`
* some important groups are:
    * `root` (superuser group, having complete control over the system)
    * `sudo` or `wheel` (its members can do `sudo`)
    * `adm` (permission to read log files)
    * `lpadmin` or `lp` (managing printers and print queues (CUPS))
    * `www-data` (managing web servers)
    * `plugdev` (managing pluggable devices, such as USB sticks and external disks)

Managing *groups* through commands `groupadd`, `groupdel` and `groupmod`:
* adding a group: `groupadd`
    * results in the group info being stored in file `/etc/group`
    * options: `-g` (set a custom group ID instead of having it generated)
    * example: `sudo groupadd -g 1005 new-group`
* changing group details: `groupmod`
    * this command affects the contents of `/etc/group` and `/etc/passwd`
    * options: `-n` (change the name of the group), `-g` (change the group ID; is that wise?)
    * example: `sudo groupmod -n my-friends new-group`
* deleting a group: `groupdel`
    * this command affects the contents of `/etc/group` and `/etc/passwd`
    * example: `sudo groupdel new-group`
    * this command fails if the group is the primary group of at least one user
    * group-owned files are not deleted

*Switch user* with the `su` command:
* it will ask for the password (of that other user)
* example: `su other-user`
* `exit` returns to the previous user

Temporarily *gain superuser privileges* with command `sudo` ("superuser do"):
* by default, these extra privileges are the root user's privileges
* it will ask for the user's password
* it only works if the user is allowed to do `sudo` (see below)
* example (prefixing the command with `sudo`): `sudo apt update`
* a `sudo` session by default expires in 15 minutes
* options: `-k` (expire a `sudo` session), `-s` (shell with extra privileges)
* we can also `sudo` into another user; e.g. `sudo -u francis -g some-group bash`

We can configure `sudo` access as follows:
* add the user to group `sudo` (or `wheel` on some systems), with `sudo usermod -aG sudo some-user`, or:
* safely edit file `/etc/sudoers` with command `visudo`
    * format of line (for user): `<user> <host-group> = (<user-ownership> : <group-ownership>) <commands>`
    * format of line (for group): `<%group> <host-group> = (<user-ownership> : <group-ownership>) <commands>`
    * here <user-ownership> is the user the logged in user can `sudo` into; an analogous remark is true for the group and <group-ownership>
    * if we do not specify user and group, `sudo` into the root user is meant
    * example line (for user): `some-user ALL=(ALL:ALL) ALL`
    * same example, but requiring no password (not very secure): `some-user ALL=(ALL:ALL) NOPASSWD: ALL`
    * example line (for group): `%some-group ALL=(ALL:ALL) ALL`
    * note that in file `/etc/sudoers` we can see the root user and the `sudo` (or `wheel` group), both having all `sudo` privileges

#### 3.2.1. File permissions

*File permissions* control access to files and directories.

Permission levels:
* *Owner* (`u`)
* *Group* (`g`)
* *Others* (`o`)

Permission types:
* *Read* (`r`), with number 4, or `0b100` as a Java binary int literal, or `04` as an octal digit
* *Write* (`w`), with number 2, or `0b010` as a Java binary int literal, or `02` as an octal digit
* *Execute* (`x`), with number 1, or `0b001` as a Java binary int literal, or `01` as an octal digit

We can assign *permissions to a file* with command `chmod`. For example:
* `chmod u+x file.txt` (the user himself or herself gets execution rights for this file)
* `chmod g-w file.txt` (the user's group loses write rights for this file)
* `chmod o+r file.txt` (others get read rights for this file)
* `chmod +x file.txt` (veryone gets execution rights for this file)

Command `chmod` can also be used with *octal* numbers. For example:
* `chmod 754 file.txt` (i.e. 7 or `0b111` for the user, 5 or `0b101` for the group, 4 or `0b100` for others, so `0754` (octal))
    * so, all rights for the user, read/execute but no write rights for the group, only read rights for others

*Permissions* also apply to *directories*. The semantics are as follows for directories:
* *Read* means: access to the directory contents
* *Write* means: add or remove files (but we need execute rights for this as well)
* *Execute* means: enter and traverse the directory

The *owner* and *group* of a file is changed with command `chown`. For example:
* `chown some-user:some-group file.txt`

Both the commands `chmod` and `chown` accept an `-R` option in order to change permissions or ownership for a *whole
directory structure*.

An *umask* sets default permissions for new files and directories. The current umask can be queried with command `umask`.

We can think of applying a umask starting with base permissions as "subtracting" the umask from the base permissions,
except that the result will always be 0 (`0b000` for user, group and others) or more. In Java terms, applying a umask
works as follows:

```java
// Bitwise AND, and bitwise complement (or 1-complement)
int permissions = basePermissions & (~umask);

// For example, if basePermissions is (octal) 0777 (0b111111111), and the umask is 022 (0b000010010),
// the result is 0755 (0b111101101), according to this formula.
// That happens to coincide with subtraction, but only because there are no negative numbers involved.
 
// Note that it helps to think in octal numbers (3 digits, for user, group and others), or binary numbers.
// It does not help to think in decimal numbers here. With "777" we mean octal 0777, which is 511 as a decimal number.
```

Base permissions are usually `0777` (octal) for directories and `0666` for regular files. So if we set the umask to
`022`, directories will have permissions `0755` (group/others can not write) and regular files will have permissions `0644`
(owner can read and write, group/others can only read). Think in octal numbers and octal digits here (or binary numbers),
not in decimal numbers!

We can also change the umask value with command `umask`, either explicitly (with a leading 0) or implicitly setting the new
umask value as an octal number (again, forget about decimal numbers in this context). To permanently change it for all
programs, edit file `/etc/login.defs` (rather than `~/.bashrc`).

There is an extra bit, namely the *sticky bit*. For regular files it has no meaning (anymore), but for *directories*
it is set to only allow the *directory owner* (or root) to *rename or delete* a file. It is especially useful for the
`/tmp` folder.

Setting the sticky bit:
* `chmod +t folder`
* or: `chmod 1777 folder`
* unsetting the sticky bit: `chmod 0777 folder` (the 0 unsets the sticky bit; it is not syntax for octal "literals" here)
* querying the sticky bit: `ls -l`:
    * if the sticky bit is set, and the file/directory has execution rights for *others*, the "x" is replaced by a "t"
    * if the sticky bit is set, and the file/directory has no excution rights for *others*, the last character is a "T"

How can programs like `sudo`, `su`, `mount` have additional privileges? This has to do with the *SUID* (set user ID) and
*SGID* (set group ID). Setting the SUID gives the executable the rights of the owner. Setting the SGID gives the executable
the rights of the group.

By all means, do *not change the SUID ourselves*!
* inspecting the SUID: `ls -l file`, with output like "-rw*s*rwxrwx":
    * if the SUID bit is set, and the file/directory has execution rights for the *user*, the "x" is replaced by a "s"
    * if the SUID bit is set, and the file/directory has no excution rights for the *user*, the "-" is replace by an "S"
* setting it (*do not do this!*): `chmod u+s file`

Also, by all means, do *not change the SGID ourselves*!
* inspecting the SGID: `ls -l file`, with output like "-rwxrw*s*rwx":
    * if the SGID bit is set, and the file/directory has execution rights for the *group*, the "x" is replaced by a "s"
    * if the SGID bit is set, and the file/directory has no excution rights for the *group*, the "-" is replace by an "S"
* setting it (*do not do this!*): `chmod g+s file`

*Best practices* for user management:
* prefer groups for managing permissions
* use meaningful group names
* follow the principle of *least privilege*:
    * assign users only necessary permissions and group memberships
    * avoid overly permissive access rights
    * avoid giving everyone write access
    * keep the number of users with elevated privileges low
* keep group memberships up-to-date
* review group permissions on a regular basis
