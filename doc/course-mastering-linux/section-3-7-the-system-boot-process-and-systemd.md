
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

Programs in user mode are less privileged than programs in kernel mode. Also, programs in user mode
cannot directly communicate with hardware, but have to go via the kernel mode to do so. See below.

The *user mode* layer contains the following sub-layers:
1. User applications, such as bash, firefox etc.
2. *System components*, such as `systemd`, X11, Wayland etc.
3. *C standard library*: calls to functions like `fopen`, `execv`, `malloc`, `localtime` etc.

Below the user mode layer, we have the *kernel mode* layer. The latter contains the following sub-layers:
1. *System calls* provided by the kernel, such as `read`, `open`, `write`, `close` etc.
2. *Linux kernel* to manage the hardware
3. *Physical hardware*, so CPU, memory, storage, network etc.

#### 3.7.3. Step 3. `systemd`

Once the bootloader has handed control to the kernel, the latter will initialize and start the
process with PID 1. On most Linux systems, the process with PID 1 is `systemd`. See the bash session
below:

```bash
chris@host:~$ ps -fl 1
F S UID          PID    PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY        TIME CMD
4 S root           1       0  0  80   0 -  5895 -      00:12 ?          0:04 /sbin/init splash
chris@host:~$ ls -l /sbin/init
lrwxrwxrwx 1 root root 22 juin   4 14:24 /sbin/init -> ../lib/systemd/systemd
chris@host:~$ ls -l /lib/systemd/systemd
-rwxr-xr-x 1 root root 100816 juin   4 14:24 /lib/systemd/systemd
chris@host:~$ file /lib/systemd/systemd
/lib/systemd/systemd: ELF 64-bit LSB pie executable, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, BuildID[sha1]=bc7b9a1d0184534fd52cddf0c47e3ccb9398f86a, for GNU/Linux 3.2.0, stripped
```

This process initializes the rest. For example:
* starting the system
* mounting drives
* starting services
* configuring network connections
* starting additional applications

`systemd` is not just one tool, but an entire tool set. Part of this tool set is shown through
some sample problems:
* launching a web server in the background
* executing a command on every boot
* running a command every few minutes, even when we are not logged in

`systemd` may be controversial in the Linux community, but it is the norm nowadays in most Linux systems.

Critics of `systemd` say:
* it's quite complex, suffering from feature creep
* it violates the Unix philosophy:
  * each program should do just one thing, and do that thing well
  * to achieve meaningful results programs work together, using standard input and output to allow for combining them
* it runs only on Linux systems and not on Unix systems in general
* performance may suffer from doing many things in parallel (especially when booting from HDD or DVD)

Proponents of `systemd` say:
* systems can be highly dynamic, and configuration might change
  * e.g. a server can launch provided a certain port on our machine is accessible
* on somewhat modern drives (even slow SSDs) parallelizing the boot process makes sense
* it offers many unique features:
  * e.g. upgrade a service without losing any connections
  * e.g. having a backup script that requires a certain mount to be present
* like said above, it is ubiquitous in modern Linux systems

`systemd` in *system mode*:
* manages the boot process and starts all services required for this (running them in parallel)
* reads configuration files ("unit files"), creates the dependency graph, and then executes the necessary commands to get the result

`systemd` in *user mode*:
* does the same as in system mode, but for user services, after the user has logged in

Our focus is on *system mode*.

To illustrate `systemd` in system mode and (as child process) in user mode, see the following bash session:

```bash
chris@host:~$ ps -fl 1
F S UID          PID    PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY        TIME CMD
4 S root           1       0  0  80   0 -  5895 -      00:12 ?          0:04 /sbin/init splash
chris@host:~$ ps -fl 3248
F S UID          PID    PPID  C PRI  NI ADDR SZ WCHAN  STIME TTY        TIME CMD
4 S chris       3248       1  0  80   0 -  5381 -      00:12 ?          0:02 /usr/lib/systemd/systemd --user
```

##### 3.7.3.1. `systemd` units, and service units in particular

`systemd` uses so-called *units* as basic building blocks. Units can declare *dependencies on other
units*. Some quite important types of unit are:
* *service*
  * represents a service that should run on our system
  * can be enabled, disabled, started, stopped, restarted, reloaded etc.
  * are managed by ".service" unit files (just like units in general are managed by "unit files")
* *target*
* *timer*
* etc.

Finding paths of units:

```bash
systemd-analyze --system unit-paths
```

Some typical paths returned by the command above are:
* `/lib/systemd/system`; sometimes also `/usr/lib/systemd/system` (on my machine `/lib` is a symbolic link to `/usr/lib`)
  * system configuration
  * "default" place for configuration from the maintainer (of Linux distribution / packages)
* `/run/systemd/system`
  * non-persistent runtime configuration
* `/etc/systemd/system`
  * configuration for the administrator
  * overrides files from `/lib/systemd/system`
  * we should store our custom configuration here

A *unit* can be managed with the `systemctl` command:
* querying the status of a unit: e.g. `systemctl status apache2.service`
* starting/stopping/restarting:
  * e.g. `systemctl start apache2.service`, `systemctl stop apache2.service`, `systemctl restart apache2.service`
* reloading the unit's configuration (not to be confused with the `systemd` configuration of the unit):
  * e.g. `systemctl reload apache2.service`

##### 3.7.3.2. cgroups

*Control groups*, or *cgroups* in short, allow for control over distribution of system resources to
groups of processes. That is:
* *cgroups* organizes processes hierarchically
  * non-leaf nodes in this hierarchy/tree are other cgroups
  * leaf nodes in this hierarchy/tree are individual OS processes
  * if we start a sub-process, it will automatically be in the same cgroup as the parent processes
* using cgroups, we can more evenly distribute system resources
* cgroups are a *Linux kernel feature*
* `systemd` relies heavily on cgroups to manage processes
* use cases of cgroups (within the same server):
  * resource limiting; e.g. setting memory limits on a cgroup, or limiting the percentage of CPU resources
  * measuring resource consumption per cgroup
  * freezing a group of processes
* also see [namespaces and cgroups](https://blog.nginx.org/blog/what-are-namespaces-cgroups-how-do-they-work)
* or see [resource management on Redhat Linux](https://docs.redhat.com/en/documentation/red_hat_enterprise_linux/6/html/resource_management_guide/index)

We can inspect *cgroups* as follows:
* show all processes on the system with `systemctl status`
* inspecting cgroups with `systemd-cgtop`
  * by default, this displays at most 3 levels of cgroups
  * but that level can be changed, e.g. with `--depth=5`

As an aside, *Docker containers* (and alternatives) also rely heavily on *cgroups*, as well as on
*namespaces*:
* *namespaces* are also a *Linux kernel feature*
* we can even emulate containers by directly working with namespaces (and cgroups)
* we could even simulate some of a container's isolation:
  * e.g. `sudo unshare --user --pid --map-root-user --mount-proc --fork bash` (exit with `exit`)
  * note how this changes the pid namespace, for example; `ps -f` and `ps -ef` return quite different output, reusing PIDs like 1 etc.
* also see [namespaces and cgroups](https://blog.nginx.org/blog/what-are-namespaces-cgroups-how-do-they-work)
* or see [containers behind the scenes](https://blog.kubesimplify.com/understanding-how-containers-work-behind-the-scenes)

*Cgroup example: limiting Firefox to a mere 100 MB of RAM*. For this, we create a *systemd slice* in our
user account. See `man systemd.slice`. The slice (let's call it "browser") is created as file
`~/.config/systemd/user/browser.slice`, with the following content:

```
[Slice]
MemoryHigh=100MB
```

After that, assuming `/usr/bin/firefox` is the path to the real Firefox executable (check with `file`
command) and not the path to a Firefox script (which can change the cgroup configuration), then Firefox
can be run as follows:

```bash
systemd-run --user --slice=browser.slice /usr/bin/firefox
```

##### 3.7.3.3. `systemd` target units

So far, we have seen *service units* and *slice units* in `systemd`. There are also so-called
*target units* (among many other types of units), which group units logically to a goal. They are
managed by ".target" unit files.

Examples of *target unit* querying/manipulation:

```bash
# Getting the current default target
sudo systemctl get-default

# Switching target without rebooting. PLEASE DO NOT DO THIS!
sudo systemctl isolate multi-user.target

# Listing available targets
sudo systemctl list-units --type target --all

# Changing the current default target. PLEASE DO NOT DO THIS!
sudo systemctl set-default multi-user.target
```

##### 3.7.3.4. enabling and disabling a `systemd` unit

*Enabling* a `systemd` *unit*:
* e.g. `sudo systemctl enable apache2.service`
  * this will enable the unit on boot, if so specified in the unit configuration file
* e.g. `sudo systemctl enable --now apache2.service`
  * like above, but also loading the unit immediately

*Disabling* a `systemd` *unit*:
* e.g. `sudo systemctl stop apache2.service` followed by `sudo systemctl disable apache2.service`
  * the unit will then no longer be loaded on boot

##### 3.7.3.5. configuring `systemd` units

The *format* of *unit configuration files* in general, and of *service unit configuration files* in
particular, is described in manual pages such as `man systemd.unit` and `man systemd.service`,
respectively.

The sections in a *service unit configuration file* are as follows:

```
[Unit]

[Service]

[Install]

```

The `Unit` section contains *configuration options* such as:
* `Description`
  * brief explanation, to help users understand the purpose of the (service) unit
* `Documentation`
  * links to relevant documentation
* `Requires`
  * ensures that other units are activated before this unit
  * if a required unit fails to start, this unit will not be started
* `Wants`
  * like `Requires`, but this unit will start even if a wanted unit fails to start
  * this is useful for optional dependencies
* `After`
  * ensures this unit will start after the specified units, if they occur
  * this helps define a unit activation order
* `Before`
  * ensure this unit will start before the specified units, if they occur
  * like for `After`, this helps define a unit activation order

The `Service` section of a service unit configuration file contains *configuration options* such as:
* `Type`
  * defines the process type and startup behavior; e.g. `simple`, `exec`, `forking`, `oneshot`
* `ExecStart`
  * the command to start the service
  * it can include arguments and options
  * but it is NOT a full Bash command! so, do not rely on command rewriting!
* `ExecStop`
  * optional command to stop the service
* `Restart`
  * defines when the service should be automatically restarted
  * e.g. `no`, `on-success`, `on-failure`, `on-abnormal`, `on-abort`, `always`
* `User`
  * user under which the command should run
* `Environment`
  * defines environment variables for the service

The `Install` section contains *configuration options* such as:
* `WantedBy`
  * specified the target(s) that should include this unit as dependency
  * common targets include `multi-user.target` and `graphical.target`
  * enables the unit to be started automatically at boot when `systemctl enable` is used

*Editing* a unit configuration file can be done *manually* or using `systemctl edit`. Manual editing
can be done as follows:
1. copy the unit file from `/lib/systemd/system` to `/etc/systemd/system` (the latter takes precedence)
2. edit the file
3. inform `systemd`, using command `sudo systemctl daemon-reload`

*Editing* a unit configuration file with `systemctl edit`:
* printing the current configuration: e.g. `sudo systemctl cat apache2.service`
* easy editing: `sudo systemctl edit apache2.service`
  * internally, this creates directory `/etc/systemd/system/apache2.service.d`
  * from that directory, override files will be loaded, changing certain parts of the initial configuration

*Example: logging current time and pinging a server on booting*. File `my-network-log.service` could be
created with the following contents:

```
[Unit]
Description=Ping a server and log it
Requires=network.target

[Service]
Type=oneshot
StandardOutput=append:/network-log/log.txt
ExecStart=date '+%%T'
ExecStart=ping -c 4 google.com

[Install]
WantedBy=multi-user.target
```

*Next example: `systemd` timer units*. First disable the previous service:
`sudo systemctl disable my-network-log.service`. Now create a timer unit with
configuration file `/etc/systemd/system/my-network-log.timer`, having the following contents:

```
[Unit]
Description=Run the network logging service on boot

[Timer]
OnActiveSec=5min
Unit=my-network-log.service

[Install]
WantedBy=timers.target
```

With command `systemd-analyze` we can see the date format (`systemd-analyze timestamp now`) or
analyze placeholders in the calendar format (`systemd-analyze calendar '*-*-* *:0,15,30,45'`).

##### 3.7.3.6. `systemd-journald`

`systemd-journald` is part of the `systemd` stack, managing system logs. It replaces the traditional
`syslog`. Key features:
* binary format for efficient storage
* centralized logging solution
* automatic log rotation and retention
* indexing and querying capabilities
* logs messages during boot as well

Basic usage examples of command `journalctl` (and `systemd-cat`):
* `journalctl` shows all logs
* `journalctl -b` only shows the current boot
* `journalctl --list-boots` shows all available boots
* `journalctl -u unit-name` filters by systemd unit
* `journalctl --since "2025-05-20" --until "2025-05-23"` shows an example of filtering by time range
* `journalctl -r` reverses the output
* `journalctl -f` follows the log in real time, analogous to `tail -f`
* sending a message into the `journalctl` log: `echo 'message' | systemd-cat`
