
### 3.9. LVM (Logical Volume Manager)

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

With *LVM* (*Logical Volume Manager*) we can *dynamically handle storage*. In other words, LVM introduces
an extra abstraction layer between "physical devices" and actual hardware. That is, we do not place
our volume directly on disk, but instead place a *device mapper* in between the volumes and disks.

Indeed, with LVM volumes can span multiple drives, or a disk can support multiple volumes.

At the highest level in LVM, we have *logical volumes*, which are just "low level" *partitions* to the
Linux kernel. So, these logical volumes in LVM are partitions, visible as `/dev/mapper/*` device files.
As such, we still need to install a file system on them, using a command like `mkfs` (or `mkfs.ext4`).

The layers in LVM, bottom-up, are:
1. Regular *physical volumes*, e.g. `/dev/sdb2`
2. On top of that, a *volume group*, combining those physical volumes, and acting as the above-mentioned *device mapper*
3. On top of that, *logical volumes*, mapped indirectly to the low level physical volumes via the "device mapper"

To install LVM, install package `lvm2`.

Each of the 3 layers mentioned above has a set of commands pertaining to that layer:
1. commands for the *physical volume* layer: e.g. `pvcreate`, `pvs`, `pvdisplay`, `pvscan`, `pvremove`, `pvmove`, `pvremove`, etc.
2. commands for the *volume group* layer: e.g. `vgcreate`, `vgs`, `vgdisplay`, `vgscan`, `vgremove`, `vgextend`, `vgreduce`, etc.
3. commands for the *logical volume* layer: e.g. `lvcreate`, `lvs`, `lvdisplay`, `lvscan`, `lvremove`, `lvextend`, `lvreduce`, etc.

Example session, where we create a logical volume mapped to 2 drives, `/dev/sdb` and `/dev/sdc`:

```bash
# 1. initialize the physical volumes

# First use "parted" on "/dev/sdb" and "/dev/sdc", creating a partition, marking it as LVM
# The "parted" shell session is not shown here

sudo pvcreate /dev/sdb

sudo pvcreate /dev/sdc

# Show the results with "pvs" or "pvdisplay"
# If needed, execute "pvscan" to cause the volumes to be shown

# 2. Add the volume group

# We call the volume group "vgroup"
sudo vgcreate vgroup /dev/sdb /dev/sdc

# Show the results with "vgs" or "vgdisplay"
# If needed, execute "vgscan" to cause the volume groups to be shown

# 3. Add the logical volume(s)

sudo lvcreate -l 100%FREE -n backups vgroup

# or, e.g. "sudo lvcreate -L 10G -n backups vgroup"

# Show the results with "lvs" or "lvdisplay"
# If needed, execute "lvscan" to cause the logical volumes to be shown

# 4. Turn the logical volume(s) into "ext4" filesystem(s)

# Assuming the "lvcreate" created device "/dev/mapper/vgroup-backups"
sudo mkfs.ext4 /dev/mapper/vgroup-backups

# Now we can mount this filesystem, and therefore use it as a normal directory tree
```

This section of the course also went into resizing volumes in an LVM context. The idea is to do things
in the right order, and to use commands mentioned above (per layer in LVM), for which we can always
consult the manual pages (or just the shorter help pages, by passing the `--help` parameter).

There is much more to learn about LVM, but that is beyond the scope of this course.
