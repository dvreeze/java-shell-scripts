
### 3.7. The System Boot Process and systemd

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

This chapter discusses the *bootloader* (GRUB 2), the *kernel* and *systemd*.
The older "init.d" / "sysvinit" system is not discussed.

This will lead to an understanding of:
* the boot process
* how the system ends up with a nice user interface
* how the server starts all services
* how we can start a program that runs automatically in the background

#### 3.7.1. Step 1. The bootloader: GRUB 2

After the BIOS / UEFI, the *bootloader* is the first software te run on startup.
The bootloader *loads the operating system*. That is, it loads the kernel into memory and
hands control to it. That's all the bootloader does, and therefore it runs for a very short time
during startup of the system.

We can change the configuration of GRUB as follows:
* edit file `/etc/default/grub` (edit carefully, of course)
* after that, update the GRUB configuration:
  * on Ubuntu, run `sudo update-grub`
  * on CentOS, run `sudo grub2-mkconfig -o /boot/grub2/grub.cfg`
  * this will update file `/boot/grub/grub.cfg`, which is a file we should never edit ourselves

#### 3.7.2. Step 2. The kernel

The *kernel* is the core of the operating system. Its main functions are:
* *process management*
  * scheduling and resource allocation
  * inter-process communication (IPC)
* *memory management*
  * physical and virtual memory
  * allocation and de-allocation of memory
* *file system management*
  * support for various file systems, e.g. "ext4", "XFS", "Btrfs"
  * handles file read and write actions
* *networking stack*
  * implements several network protocols, e.g. TCP/IP, Ethernet
  * routing, packet filtering and traffic control
* *hardware abstraction layer* (HAL)
  * enables applications to communicate with various devices (the abstract devices are under `/dev`)

*Kernel modules* are pieces of code that can be loaded into the kernel on demand.
They allow us to extend the functionality of the kernel. A few examples are:
* *device drivers*, such as nvidia drivers, Broadcom wireless drivers, etc.
* *VFIO* (virtual function I/O), allowing a guest OS to access the host GPU
* other kernel modules, such as VirtualBox Guest Addons, ZFS filesystem etc.

How do we *communicate with hardware*? There are *2 main layers* involved in that:
1. *User mode*
2. *Kernel mode*

The *user mode* layer contains the following sub-layers:
1. User applications, such as bash, firefox etc.
2. *System components*, such as `systemd`, X11, Wayland etc.
3. *C standard library*: calls to functions like `fopen`, `execv`, `malloc`, `localtime` etc.

Below the user mode layer, we have the *kernel mode* layer. The latter contains the following sub-layers:
1. *System calls* provided by the kernel, such as `read`, `open`, `write`, `close` etc.
2. *Linux kernel* to manage the hardware
3. *Physical hardware*, so CPU, memory, storage, network etc.

#### 3.7.3. Step 3. `systemd`

TODO
