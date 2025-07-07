
### 3.1. Files on Unix, and the "everything is a file" concept

A *file* is a container for storing, accessing and/or managing data. Typically a file has a unique identifier or
*filename*. This name, in combination with its *path* provides a unique location of the file in the filesystem.

*Filenames* point to an *inode*, which stores the *metadata* of the file. So, the filename is NOT part of the inode.
The metadata provided by the inode includes file size, ownership, permissions, timestamps, file type (e.g. a directory
is a file too), the location on disk (usually), etc.

A *folder* is a file too, and the folder filename therefore has an inode too. The inode of the directory points to
filenames (and locations) of the files in the directory.

With `ls -l` we can see the *file type* as the first character on each line. For example:
* `-` means: regular file
* `d` means: directory
* `l` means: symbolic link
* `c` means: character device
* `b` means: block device
* `p` means: named pipe
* `s` means: socket

As we can see, *"everything is a file"* on Unix systems.

A *symbolic link* (*symlink*) contains a *reference* to another file/directory, through its filename. Symlinks are
automatically resolved at runtime each time they are used. To see the symlink itself instead of the file it refers to,
do an `ls -l` (see above, `l` means symlink, and the referred file is also shown on that `ls -l` output line).
It is possible that a symlink refers to a non-existing file, in which case it is broken.

A symlink is created with `ln -s targetfile link`. The target can be a directory too.

Personal note: the `sdkman` tool that is much loved by Java developers is a perfect use case for symlinks under the hood.
E.g., the current Java distribution is referred to using symlinks. This makes it so easy to switch Java versions.

A *hardlink* is another filename (or the same filename in another directory) for the *same inode*. Remember: the inode
is the (metadata of the) "real file". The filename only refers to that file. Strictly speaking, the (first assigned) filename
is a hardlink too. When removing a file, only the hardlink is removed, until it is the last hardlink, in which case the file
data is removed.

A hardlink is created with `ln targetfile link`. The target can only be a regular file, not a directory.

An entire folder structure can be copied with hardlinks, so without needing any more storage. The needed command is
`cp -al source dest`. The `-l` option indicates that only hardlinks must be introduced, instead of copying any data.
The `-a` option ("archive") indicates several things, including recursive copying, and preservation of all file attributes.

There is a limit to the *number of inodes*. This limit can be reached even without filling up the hard disk! To show
how many inodes (instead of blocks) are used, enter command `df -ih` (`i` for inode, `h` for human-readable). File removal
and/or archiving of files (as "tar" archive, for example) can be used as quick fixes. By the way, `df` is the tool to
report file system disk space usage (or, in this case, inode usage).

Data streams between an I/O device (e.g. mouse, keyboard, etc. etc.) and program can be *unbuffered* or *buffered*.
Unbuffered I/O gives immediate access to data, and gives precise control over data flow and timing.
Buffered I/O utilizes a so-called *buffer* (a temporary storage area) to hold data before it is being sent to or received
from the I/O device. Buffered I/O is efficient, and ideal for large sequential data transfers. Example: disk I/O.

A *device* is represented by Linux as a *file* too. Remember: *"everything is a file"*, or *"everything is a stream
of bytes"* (Linus Torvalds). The file abstraction enables access to underlying hardware without knowing its technical
details.

Kinds of devices:
* *character devices*:
    * they give us *unbuffered* access to the device
* *block devices*:
    * they give us *buffered* access to the device, through *blocks* of data (each of size 512 bytes or 1KB, typically)
* *pseudo-devices* (potentially not referring to any physical device, but still presenting themselves as `c` or `b` devices):
    * `/dev/null` discards data written to it (and returns EOF when reading from it)
    * `/dev/random` produces a stream of random "data", given there is enough "environmental noise" (or entropy)
    * `/dev/urandom` always produces random data, potentially reusing already used "environmental noise"
    * `/dev/stdin`, `/dev/stdout` and `/dev/stderr` facilitates I/O for normal Unix programs

To show the "everything is a file" concept, we could explicitly write output to `/dev/stdout`.

The `/proc` folder provides *runtime data*:
* `/proc/cpuinfo` shows runtime information about the syatem's CPU(s)
* `/proc/meminfo` shows current memory usage
* `/proc/version` shows information about kernel, gcc and the OS
* `/proc/uptime` shows the time the system has been up and the time it has been idle (consider using the `uptime` command)
* `/proc/loadavg` shows the average load the past 1, 5 and 15 minutes, the current process and thread counts, and the last PID

#### 3.1.1. File System Hierarchy Standard

The *directory structure*, according the FSH standard, is as follows:
* `/bin` ("binaries") contains essential command binaries required for users
    * the "usrmerge" initiative merges it to `/usr/bin` (currently typically using a symlink from `/bin` to `/usr/bin`)
* `/boot` contains files for the boot loader
* `/dev` ("devices") contains devices as files (see above)
    * e.g. `/dev/null`, `/dev/sda` ("hard drive"), `/dev/tty` (terminal)
* `/etc` contains system-wide *configuration* files/directories, typically as editable text files
* `/home` contains user home directories as direct subdirectories
* `/lib` ("libraries") contains library files supporting binaries under `/bin` and `/sbin`
    * `/lib32` and `/lib64` contain libraries for additional architectures
    * the "usrmerge" initiative causes this `/lib` directory to be merged to `/usr/lib`, etc.
* `/media` contains mount points for *removable storage media*
* `/mnt` ("mount") contains mount points for additional filesystems (typically not shown in the file explorer GUI)
* `/opt` ("optional") contains optional software packages
* `/proc` ("process") is a virtual filesystem (usually procfs) providing runtime information (see above)
* `/root` is like the home directory for the root user
* `/run` ("runtime") provides runtime data, and its files are emptied/removed on booting, or on shutdown
* `/sbin` contains essential command binaries required for the root user
    * the "usrmerge" initiative merges it to `/usr/sbin` (currently typically using a symlink from `/sbin` to `/usr/sbin`)
* `/srv` ("services") contains files for services, if not stored under `/var`; quite often, it contains data from FTP servers
* `/sys` ("system") provides information about devices, drivers and kernel features
* `/tmp` ("temporary") contains temporary files created by system or users, which are typically removed on reboot
* `/usr` ("user") contains *shareable read-only* data
    * e.g. system binaries, libraries, documentation, source code
    * so, subdirectories like `/usr/bin`, `/usr/lib`, `/usr/sbin`, `/usr/src`
    * usually, files in this folder can be shared between multiple computers
    * the latter is not the case for `/usr/local` (subdirs: e.g. `usr/local/bin`, `usr/local/lib`, `/usr/local/sbin` etc.)
    * again, note the "usrmerge" initiative, which makes the `/usr` directory even more important than it already was
* `/var`("variable") contains variable data such as logs, databases, websites, email etc.
    * the directory's contents changes as the system runs

For the "usrmerge" project/initiative, see above. Why this project and this simplification? In the distant past, we
might have wanted to store `/usr` on a different drive, and have `/bin` etc. contain only essential files to fix a broken
system. This is no longer needed. Hence the simplification.
