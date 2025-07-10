
### 3.5. Package Management on Ubuntu

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

Personal note: I do not use Ubuntu's package management for every dependency. For example, for managing
different versions of Java and for installing Java tooling (such as Maven) [SDKMAN!](https://sdkman.io/)
is a great SDK manager. As for [snap](https://snapcraft.io/), I tend not to use it for "core" packages,
but I do use it for programs like the Firefox browser. So, dependencies of the OS and dependencies
of Firefox etc. do not interfere with each other, since Firefox (when installed using `snap`) ships
with its own (isolated) dependencies.

*Package management* is the process of *installing, configuring and removing software*.

It is essential for easy distribution and installation of software, handling dependencies and
software compatibility, and therefore maintaining system stability and security.

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
