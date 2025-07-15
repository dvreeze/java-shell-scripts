
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
Some skills using `parted` may be needed after a server crash.

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
