
### 3.10. Full Software Upgrade

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

Let's say we want to *upgrade from Ubuntu 22.04 LTS to Ubuntu 24.04 LTS*. This should be done with care.
Some things to keep in mind:
* first make a complete backup (or at least back up the data)
* make sure there is enough disk space for the upgrade
* make sure there is enough time to fix any issues that might arise
* wait at least a few weeks after an Ubuntu release before doing the upgrade
* make sure that all used additional repositories also support the new release of Ubuntu
* have a bootable USB drive (or something similar) to boot from if anything goes wrong
* and, of course, evaluate whether we really want to upgrade!

Note that the current version of the (Ubuntu) distribution can be found with command
`lsb_release -a`.

Perform the following steps up to and including the upgrade:
1. `sudo apt update` or `sudo apt-get update`
2. `sudo apt full-upgrade` or `sudo apt-get dist-upgrade`
3. `sudo reboot`
4. `sudo do-release-upgrade`

The latter command can be installed by installing package `update-manager-core`.
Only one version can be upgraded at a time, either from LTS version to the next LTS version,
or from non-LTS version to the next (typically non-LTS) version.

Troubleshooting when the system no longer boots:
* stay calm, and try to methodically find out the root cause
* so, something fails during the boot process; that could be the bootloader or the kernel
* boot from the bootable USB drive (the one we had already prepared)
  * note that creating a bootable drive is not just copying an ISO image to the drive
  * instead, using special programs the drive must be "burned" with the ISO image, as bootable drive
  * and make sure that BIOS/UEFI lets us boot from that drive
* now, having booted from that drive, we can analyze and fix the broken OS
  * hopefully we can find some logging to pinpoint the moment booting started to fail
  * to fix the broken OS, we may need commands like `mount` and `chroot`

Common problems during the boot process:
* problems with the kernel
* problems with mounts
* problems caused by additional packages
* hardware problems

Common problems while the system is running:
* services do not start
* services might crash
* high CPU or memory load
* wrongly configured software (e.g. a firewall)

