
### 3.8. Volumes, Partitions and Mounts

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

Note: this lecture mainly applies to HDDs, SSDs and USB drives (and not to optical disks, tape storage,
cloud storage, etc.).

#### 3.8.1. Basics of partitions and file systems

For the *structure* of the storage device, see the *partition table*:
* the legacy format is *MBR* (*Master Boot Record*):
  * limited to 4 primary partitions
  * limited to 2 TiB disk size
* the modern format is *GPT* (*GUID Partition Table*);
  * supports up to 128 partitions
  * supports larger disks

With command `gparted` we can visualize this. We can get the same information from CLI tool `parted`.

Important when organizing partitions: mind the difference between *KiB* (kibibyte) and *kB* (kilobyte),
etc. `1 KiB` is 1024 or `2 ** 10` bytes, whereas `1 KB` is 1000 or `10 ** 3` bytes. Analogous remarks
apply to MiB (mebibyte, or `2 ** 20` bytes) versus MB (megabyte, or `10 * 6` bytes), GiB (gibibyte)
versus GB (gigabyte), TiB (tebibyte) versus TB (terabyte), etc.

To make things more confusing, the JEDEC standard defined 1 megabyte as `2 ** 20` bytes (like mebibyte)
and 1 gigabyte as `2 ** 30` bytes (like gibibyte). This notion is followed by early macOS versions
(until 10.6 Snow Leopard), Windows, CPU cache and memory (RAM).

A *partition* usually uses and needs a *file system* on top of it, to store data. A file system is
responsible for:
* organizing data, by storing data in *folders and files*
* *space allocation*: managing free and used space, and releasing space when files are deleted
* management of *metadata*: permissions, ownership, timestamps (created at, last updated at, etc.)
* *data integrity*:
  * implementing error detection mechanisms
  * making sure the file system is always in a consistent state (if possible even after unexpected power loss)
  * journaling: storing new changes in a temporary file, before committing them permanently

Common file systems on *Linux* are:
* `ext3`: 3rd version of extended file system
  * older but still commonly used
* `ext4`: 4th version of extended file system
  * improved performance, support for larger disks, and journaling improvements (improved recovery after shutdown)
* `xfs`:
  * especially fit for managing large files and file systems
  * optimized for parallel I/O
  * snapshot support, and files can share the same data blocks
* `btrfs`:
  * supports easy creation of snapshots
  * enhanced RAID support

Common file systems on *Windows* are:
* `FAT32`: older file system; usually can only store files up to 4 GB
* `NTFS`: proprietary, sometimes only readable and not writeable from Linux
* `ReFS`: successor of `NTFS`
* `exFAT`: specification now published (but was proprietary until 2019); ideal for external drives

Common file systems on *macOS* are:
* `APFS`: for iOS and macOS devices; supports full disk encryption

With command `parted` (with root privileges) we can manage partitions on the CLI. This should
hardly ever be needed in practice, and must be done carefully, in order not to lose any data!
Some skills regarding the use of command `parted` may be needed after a server crash.

After creating a partition, we still need to create the file system, using command (family) `mkfs`
("make file system"). For example:

```bash
mkfs.ext4 /dev/sdb1
```

#### 3.8.2. Mounts

Again, separate the notions of *partition* (low level) and *volume* (higher level than partitions).
A *partition* is a part of a physical drive, separated from other parts of the drive.

A *volume*:
* is a *logical* storage unit on our computer
* usually appears as an accessible drive or partition
* can span multiple partitions:
  * multiple partitions can be combined into one *LVM* (logical volume)
  * or: a volume can be stored on another computer, and accessed through the network

Now we can start speaking about *mounting a volume*.

The idea of a *mount* is that we can *connect a file system to our directory tree*. This makes
the file system accessible to our programs.

Devices we can mount include:
* external removable media (usually mounted into a subdirectory of `/media`)
* internal permanent volumes (usually mounted into a subdirectory of `/mnt`)
* external storage servers, e.g. FTP
* folders into other folders ("bind mount")

After mounting a drive, it becomes part of our directory tree, and we can `cd` into that folder.

To mount a drive that is not mounted automatically, we first need to get its name. For that we use
program `lsblk -f` ("list information about block devices"), where option `-f` causes `lsblk`
to also show file system information.

On my machine, I see that block devices named `nvme0n1`, `nvme0n1p1` etc. indeed correspond to block
device "files" `/dev/nvme0n1`, `/dev/nvme0n1p1` etc., when looking at those devices with `ls -l`.
Remember: "everything is a file in Unix".

We can mount a drive into a directory tree with command `mount <mount source> <target directory>`.
First make sure the target directory is created. For example, after creating directory
`/mnt/windows-data` on my machine, I could (safely) mount a Windows volume `/dev/nvme0n1p3` into
that target directory:

```bash
# Mounting the Windows drive. For the options, see below.
sudo mount -t ntfs -o ro,noexec,nosuid,noatime /dev/nvme0n1p3 /mnt/windows-data/

# Checking the mount with "mount"
mount

# Checking the mount with "df -h"
df -h

# "ls" the target directory of the mount
ls /mnt/windows-data/

# Unmounting, after making sure the mount is no longer used
sudo umount /mnt/windows-data/
# Alternatively, we could have unmounted with "sudo umount /dev/nvme0n1p3"

# Check the mount is indeed no longer there
mount
```

Parameters of the `mount` command include:
* `-t` to explicitly mention the filesystem (be careful here!); e.g. `-t ntfs`
* `-o` to add comma-separated options:
  * `ro` is read-only; `rw` (read-write) is the default
  * `noexec` disables execution of executable files on the mounted filesystem
  * `nosuid` disables "set-user-identifier" and "set-group-identifier" on the mounted filesystem, for extra security
  * `noatime` disables updating of access time when a file on the mounted filesystem is read

Mount options *depend on the filesystem*! For example, `exFAT` does not support users and groups.
For `exFAT` we could set them as mount options:
* e.g. `gid=1001` means: all files should be owned by the group with ID 1001
* e.g. `uid=1001` means: all files should be owned by the user with ID 1001
* e.g. `umask=0027` (starting with a zero to mark this as an octal number):
  * so `0777` becomes `0750` after setting this umask

To create and mount `exFAT` partitions, packages `exfat-fuse` and `exfatprogs` must be installed.

#### 3.8.3. Mounting on boot: `/etc/fstab`

File `/etc/fstab` is a system configuration file in Linux:
* it defines how storage devices and partitions should be mounted
* it is being read during boot and can be used to automatically mount volumes

File `/etc/fstab` contains one line per filesystem to mount. Each line contains the following
whitespace-separated fields:
1. device identifier (UUID or device path), e.g. `UUID=8882d72d-948c-4d68-b9b3-81237304ca5c`
2. mount point, e.g. `/mnt/backups`
3. filesystem type, e.g. `ext4`
4. mount options (see below)
5. dump option (`dump` is a backup utility; 0 means: no backup)
6. filesystem check order; i.e. `fsck` priority: 1 is root filesystem, and 2 is non-root filesystem

Sample `/etc/fstab` line:

```
UUID=8882d72d-948c-4d68-b9b3-81237304ca5c /               ext4    errors=remount-ro 0       1
```

In `/etc/fstab` the default options are `rw`, `suid`, `dev`, `exec`, `auto`, `nouser` and `async`.
Explanation of some of these options:
* `auto` means: should be mounted automatically
* `nouser` means: can only be mounted with root privileges
* `async` means: reads and writes should be "async" (good for performance, bad for data integrity on a power loss)

After editing `/etc/fstab`, refresh our mounts with command `sudo mount -a`. This mounts all filesystems
mentioned in `/etc/fstab`.

#### 3.8.4. Mounting an FTP server

#### 3.8.5. Checking for errors

#### 3.8.6. Repartitioning
