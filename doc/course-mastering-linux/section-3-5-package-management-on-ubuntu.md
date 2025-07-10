
### 3.5. Package Management on Ubuntu

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

*Package management* is the process of *installing, configuring and removing software*.

It is essential for easy distribution and installation of software, handling dependencies and
software compatibility, and therefore maintaining system stability and security.

Personal note: I do not use Ubuntu's package management for every program. For example, for managing
different versions of Java and for installing Java tooling (such as Maven) [SDKMAN!](https://sdkman.io/)
is a great SDK manager. As for [snap](https://snapcraft.io/), I tend to not use it for "core" packages,
but I do use it for programs like the Firefox browser. So, dependencies of the OS and dependencies
of Firefox etc. do not interfere with each other, since Firefox (when installed using `snap`) ships
with its own (isolated) dependencies.

Package management differs quite a lot in the details between different Linux distributions.
This section is about package management for Ubuntu.

It is based on Debian package management. It uses `apt` or `apt-get` (choose whichever one you like best),
and at a lower level it uses `dpkg`. As an alternative to `apt`/`apt-get` we can use `snap`, but it does
matter for which software we choose to use `snap` instead of `apt`/`apt-get`.

At a low level, `dpkg` is used to install software, distributed as `*.deb` files. It does not manage
dependencies. A `*.deb` file is a compressed archive, in the "ar" file format, with all the files
needed for the program and for its installation on the system. Installation would be done as follows:

```bash
# Do not do this. Use apt or apt-get instead, to manage dependencies as well.
sudo dpkg -i package.deb
```

Nice to know (but we can always do `dpkg --help`, of course) is that the "architecture" can be found as
follows, in case of confusion about which architecture to select for packages to install:

```bash
# Prints amd64 in my case (for Intel or AMD processors, that does not matter)
dpkg --print-architecture
```

Packages can be found at [Ubuntu packages](https://packages.ubuntu.com/).

Unlike `dpkg`, programs `apt` and `apt-get` do manage dependencies of installed software. While it does
not really matter which one of those 2 tools is used, in scripts it is better to use `apt-get` because
of its API stability.

Program `apt`/`apt-get` knows about packages (and their versions) that are available, and about where to locate
them, through a list of *repositories*. These repositories are found by `apt`/`apt-get` in:
* file `/etc/apt/sources.list` for repositories from the system
* files `/etc/apt/sources.list.d/*` for additional (3rd party) repositories

Important: only repositories you *trust* should be listed, because they can even cause existing software
to be replaced on our system. The less we need to rely on 3rd party repositories, the better it is in that
regard.

Before installing software with `apt`/`apt-get`, we need to update the "package definitions", i.e.
fetch the latest list of available packages from the repositories:
* `sudo apt update` or `sudo apt-get update`

Only after that we should install needed software, and their dependencies:
* `sudo apt install postgresql` or `sudo apt-get install postgresql`

`apt`/`apt-get` will remember which packages have been installed manually and which ones have been
installed as dependencies.

Keeping the system up-to-date, by installing available updates:
1. `sudo apt update` or `sudo apt-get update`
2. `sudo apt upgrade` or `sudo apt-get upgrade --with-new-pkgs`

This will install available and possible updates, even install additional dependencies if needed,
but it will never remove any dependencies from our system.

Bigger upgrade, allowing "uninstalling" dependencies as well if that is needed to resolve version conflicts:
1. `sudo apt update` or `sudo apt-get update`
2. `sudo apt full-upgrade` or `sudo apt-get dist-upgrade`

This is more risky, potentially removing software we rely on. So, read what the command will do
(before confirming the upgrade action). Also, have a backup. And make sure to have time to fix
any problems that may arise.

Removing unneeded dependencies (which have not been removed by `apt full-upgrade`/`apt-get dist-upgrade`):
1. `sudo apt update` or `sudo apt-get update`
2. `sudo apt autoremove` or `sudo apt-get autoremove`

Example of the syntax of (lines in) `/etc/apt/sources.list`:

```bash
deb http://ports.ubuntu.com/ubuntu-ports/ jammy main restricted
```

In this example:
* `deb` is the *type*
  * `deb` means: this repository contains binary packages
  * `deb-src` means: this repository contains source packages
* the *URL* is the *address* of the repository
* `jammy` is the *distribution*
  * other distributions are e.g. `noble` and `oracular`
* after the distribution, the *domains* follow:
  * officially supported by Canonical: `main` (for free software) and `restricted` (for non-free software)
  * community supported/third party: `universe` (for free software) and `multiverse` (for non-free software)

We can add additional *repositories* (e.g. "https://wiki.winehq.org/Ubuntu"), by adding a file to
`/etc/apt/sources.list.d`. Usually the GPG key is added as well, to let our system have more trust
in the 3rd party repository. But be careful before deciding to trust a 3rd party repository.

At [Personal Package Archives](https://launchpad.net/ubuntu/+ppas) users can easily provide repositories
for others. For example, we could add a PPA for the latest PHP version:
* adding the repository: `sudo add-apt-repository ppa:ondrej/php` (followed by `sudo apt update`)
* removing the repository: `sudo add-apt-repository --remove ppa:ondrej/php`
* again, be careful before deciding to trust the repository, now and in the future

We can show dependencies of packages as follows:
* e.g. `sudo apt show bash` or `sudo apt-cache show bash` (note: `apt-cache`, not `apt-get`)

What can we do when encountering *conflicting dependencies*? E.g. "bash" wants "libc6" <= 2.35 and
"zsh" wants "libc6" >= 2.36. We could try:
* `sudo apt install -f` or `sudo apt-get install -f` (where `-f` is `--fix-broken`)
* or: `sudo apt full-upgrade` or `sudo apt-get dist-upgrade`, especially before trying to install additional software
* or else: `sudo apt autoremove` or `sudo apt-get autoremove`

Best practices:
* avoid 3rd party repositories to the extent possible/feasible
* avoid installing software with `*.deb` files
* before upgrading to a new Ubuntu version:
  * plan some extra time for fixing dependency issues
  * do not upgrade immediately after the release of a new Ubuntu release, but wait a few months
  * create a complete backup of the system
* use LTS (Long Term Support) Ubuntu versions (for servers)
* consider using Docker etc. to containerize some/many of the used software

Sometimes when installing packages the installation process asks us questions, or it runs
some configuration. For example:
* a bootloader such as `grub` may want to install itself as the main bootloader
* a webserver may create a user/group and automatically start with our system using this configuration
* a display manager (managing the login screen) may ask us for the default display manager

Sometimes we would like to rerun those scripts. For this we can use command
`sudo dpkg-reconfigure` taking the package name as argument.

Another example is the first installation of the `locales` package. Recall that locales define how
numbers and dates are displayed. Note that Java (among many other programming languages) rely on the
OS to provide "locale" functionality.

Use `sudo dpkg-reconfigure locales` if we want to reconfigure a package w.r.t. locales.

Although "md5" is no longer considered secure, many packages ship with "md5" checksums. With
command `debsums` (taking a package or `*.deb` file as argument) we can verify all packages shipped
with an "md5" checksum:
* option `-a` lists all files, including configuration files
* option `-l` lists packages shipped without "md5" checksums
* option `-s` means: silent; so only show errors

With `snap` we have an alternative to `apt`/`apt-get` for package management:
* `snap` bundles an installed application with all its dependencies, in isolation from "global dependencies"
* the command is `snap install` (taking the package as argument)
* this typically increases the size of the download
* yet each application installed with `snap` can have its own dependencies, not affecting any other `snap`-installed software
* in particular, each application installed with `snap` can be independent from the Linux distribution
* packages are updates automatically in the background (by daemon process `snapd`)
* we can trigger updates ourselves with `snap refresh`
* "snap" is a centralized repository for applications (with their dependencies included):
  * available packages can be found at [snapcraft.io](https://snapcraft.io/)
  * anyone can publish their applications to this repository
  * as always, be careful before trusting the author of a package
* in case an `apt`/`apt-get` installed dependency is extremely problematic, consider using `snap` for that dependency, if feasible
