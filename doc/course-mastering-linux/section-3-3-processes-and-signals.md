
### 3.3. Processes and Signals

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

A *process* is an *instance of a program*. Some characteristics of a process are:
* it is *managed by the kernel*
* it is an *independent execution unit* with its own *resources*, such as:
  * CPU
  * memory
  * opened files
  * network connections
  * etc.
* it has *attributes* such as:
  * a *PID* (unique process ID, at least unique at any moment in time)
  * a *PPID* (the parent process ID; processes are organized *in a hierarchy*!)
  * a *process state*, such as *running*, *waiting*, *stopped*, *zombie*
  * etc.

The course material (in this section) does not really talk about *threads*, but they are a property too.
A well-known example of a process (to Java developers) is a *running Java program* (JVM).
In a running Java program, a *kernel thread* is represented in Java as a *platform thread*
(as an instance of class `java.lang.Thread` for which method `isVirtual()` returns `false`).

We can query for information about running processes with the `ps` command ("process status").
Here the Linux `ps` command is described (not the same command on macOS). It can be used in *POSIX mode*
or *BSD mode*. POSIX stands for Portable Operating System Interface (IEEE 1003). BSD stands for
Berkeley Software Distribution. POSIX and BSD are different "branches" in the history of "Unix".

The `ps` command in POSIX mode, where many options can be *combined*:
* `ps` (without any arguments) shows all processes of the current terminal (TTY, see `tty` command)
* `ps -A` or `ps -e` shows all processes, from all users and all sessions (with or without terminal)
* `ps -f` shows extended information, such as user, terminal and PPID
  * so `ps -e -f` or `ps -ef` shows all processes, showing extended information in the output
* `ps -l` shows processes in a longer format, adding a few more columns, compared to option `-f`
* `ps --forest` shows processes as an ASCII tree
  * `ps -ef --forest` is like `ps -ef`, yet additionally making clear where each process occurs in the process hierarchy
  * knowing a process ID (say 1234), `ps -ef --forest | grep 1234` filters on that PID or PPID
* `ps -p 1234,1235` (or even `ps 1234 1235`) shows processes with the given PIDs

The `ps` command in BSD mode (using parameters without dash):
* `ps a` (all processes of all users)
* `ps u` (more user-oriented format, with additional columns)
* `ps x` (include processes without TTY (i.e. terminal))
  * `ps aux` (combination of the options above)

How does *multitasking* work (ignoring threads within a running process)?

For the sake of argument, assume we have only 1 CPU in our system. Then multiple programs can
*appear to execute at the same time*, by having the CPU switch between those programs (and doing so
fast enough). This *(process) context switching* is called *scheduling*.

*Context switches* can be found as follows (for imaginary process ID 1234):
* `cat /proc/1234/status | grep ctxt` (or `grep ctxt /proc/1234/status`)
* refreshing the output each half second: `watch -n 0.5 grep ctxt /proc/1234/status`

Knowing more about OS context switches for a process, we could influence that, by setting *niceness*.
Niceness ranges from -20 (lowest niceness, highest priority) to 19, inclusive (highest niceness, lowest priority).
The default for new processes is typically 0. Processes with lower niceness (so higher priority) receive
more time from the scheduler.

Increasing niceness for a process requires no root privilege, but decreasing niceness does. Examples:
* for a new process: `nice -n 19 gedit`
* for a new process: `sudo nice -n -20 gedit`
* changing priority for an existing process (with PID 1234): `renice -n 10 1234`
* changing priority for an existing process (with PID 1234): `sudo renice -n -10 1234`

If we just want to get a PID, based on process name, `ps -ef --forest | grep -F 'firefox'` is not that handy.
Instead, we could just do `pgrep firefox`. To get child processes as well, do `pgrep -f firefox`.
Then we could do things like: `renice -n 19 $(pgrep -f firefox)` (without double-quote pairs).

Another way of finding processes is via command `fuser` (see `fuser --help`). It is not mentioned in this
course (at least not in this section), I believe. Based on a given file, socket or filesystem, this
command can find processes using that file/socket/filesystem. This command can even send a signal
to the processes found (see below for a treaty of signals).

For example, a cumbersome way of finding IntelliJ processes is by using the `fuser` command, passing
the path to the IntelliJ `bin` directory as argument. Still, the `fuser` command can be quite useful
when troubleshooting.

#### 3.3.1. Signals

*Signals*  can be sent to processes, *interrupting* the process flow at a *convenient* time
("convenient" does NOT mean that it will take long before interrupting the process flow;
it does mean that the interruption does not take place at a time when it could potentially corrupt the OS).

Signals *asynchronously notify a process of an event*. The OS is responsible for delivering the
signal to the process. The OS maintains a *signal queue*, and it can even send a signal to a process
that is not active at the moment.

Some signals are:
* `SIGKILL` ("kill process"; e.g. the kernel could send this signal)
  * it forcefully terminates the program
  * it is the *kernel* handling this signal and unique in that sense; the program is not even informed!
  * obviously, this signal can lead to data loss; there is no cooperation from the program, after all
* `SIGILL` ("illegal instruction")
* `SIGINT` ("interrupt"; usually sent from a terminal)
  * it means: dear process, please try to come to an end, because we are in a shell and would like to regain control over the terminal
  * if we end a program with CTRL-C in a shell, the `SIGINT` signal is sent to that program
  * we could send this signal ourselves too: `kill -s SIGINT 12345` (for process 12345)
  * the program can ignore the signal
* `SIGHUP` ("hangup"; sent from a shell)
  * for usual programs, `SIGHUP` communicates that the terminal has been closed; then the program usually exits
  * for *daemon processes* (background processes) it may signal that its configuration should be reloaded
  * we could send this signal ourselves too: `kill -s SIGHUP 12345` (for process 12345)
* `SIGWINCH` ("window change"; sent by a window manager)
* `SIGTERM` ("terminate process"; sent from other processes)
  * it means: dear process, please try to come to an end, if possible
  * the program can ignore the signal
* `SIGSTOP` and `SIGCONT`
  * `SIGSTOP` puts the program in a stopped state, i.e. a "paused" state
  * the program cannot ignore this signal; it cannot even catch it
  * we can send the signal ourselves
  * with `SIGCONT` the program can be resumed again
  * these signals can be tested well with programs like `cmatrix` and `wget`

The command to *send signals* is called `kill`. Despite the name, the command can send any signal.
Syntax (assuming the process to which the signal is sent is 12345, and the signal is `SIGINT`):
* `kill -s SIGINT 12345`
* or: `kill -SIGINT 12345`
* or: `kill -s 2 12345` (`SIGINT` has number 2)
* or: `kill -2 12345`

Command `kill 12345` is identical to `kill -s SIGTERM 12345`, so `SIGTERM` is the default signal sent by
that command.

We can also select the process(es) by name, using the `killall` command (again, this command is for sending any signal):
* e.g. `killall -s SIGTERM firefox`, or: `killall -SIGTERM firefox`, or: `killall firefox` (`SIGTERM` is the default signal)
* this matches and sends the signal to all processes with "firefox" in the name, matching only the first 15 characters of the program name
* for programs with very long names, use `killall -e` (for "exact")
* interactive mode: `killall -i firefox`

Command `kill -l` outputs a list of signals with their numbers (e.g. 15 for `SIGTERM`, and 9 for `SIGKILL`).

Many commands, such as `kill`, exist twice:
* once as *shell built-in*
  * command `type kill` says: "kill is a shell builtin" (on my machine)
* once as *executable file*, typically under `/usr/bin`
  * command `which kill` returns path `/usr/bin/kill` to the executable file (on my machine)
  * in the `zsh` shell, use command `where kill` instead of `which kill`
  * command `cat` is not a shell built-in; `type cat` and `which cat` both point to the `/usr/bin/cat` executable

Note the different output of `kill -l` and `usr/bin/kill -l`, so they are indeed quite different.

When a process exits:
1. most of the resources of the process are made available again to other processes
2. the kernel sends a `SIGCHLD` signal to notify the parent of the terminated child process
3. the parent "reaps" the terminated child process; i.e. it collects the child's *exit status* through a system call (`wait()` or `waitpid()`)

The last terminated child's exit code can be queried with `$?`.

Of course, it is possible that the parent terminates before the child. In that case, the child
becomes an *orphan*, and is adopted by the `init` process (or a child of the `init` process).

A *zombie process* is a terminated process that still has an entry in the *process table*.
Usually this occurs when the parent has not (yet) read the child's exit status.

Zombies are marked as "Z" in the output of `ps -l`. If there are too many of them, this may lead
to process table overflow.

Removal of zombie processes:
* when the parent process ends, they are usually removed automatically
* otherwise, sending a `SIGCHLD` signal to the parent hopefully helps
* if we kill the parent process, the `init` process will adopt the zombie process and reap it

*Process states*:
* *Running* (*R*): this means what it says, namely "currently running"
* *Sleeping (interruptible)* (*S*): it is waiting for some event, and on receipt of that event returns a signal (about receipt of the event)
* *Uninterruptible sleep* (*D*): typically during a *system call* (usually I/O)
* *Traced or stopped* (*T*): "stopped" means "temporarily stopped" or "paused"
* *Zombie* (*Z*): see above

Process state changes:
* From *R* to *S*: waiting for some event
* From *S* to *R*: event ready, or signal received
* From *R* to *D*: invoking a system call
* From *D* to *R*: system call finished
* From *R* to *T*: `SIGSTOP` (see above), `SIGTSTP` (typically CTRL-Z from a terminal), `ptrace()` system call
* From *T* to *R*: `SIGCONT` (see above)
* From *R* to *Z*: `exit()` call (see above)

To display all processes in our system, we can use `top` (`top` or `sudo top`). The most important
parameters of the `top` command are:
* `-u user-name` (show only processes owned by the given user)
* `-d 3` (setting the delay between display updates, in seconds; the default is 3 seconds)
* `-i` (hide "idle" processes, so showing only processes currently using CPU resources)
* `-c` (display the full command line used to start each process, instead of just the command name)

The output of `top` can be changed interactively:
* the "f key" is used to further customize the output
* with the "k key" ("kill") we can send signals to processes
* with the "r key" we can change the niceness of processes
* with the "z key" we can change to color mode, and with the "Z key" (uppercase) we can configure it
* with the "W key" (uppercase) we can save the current configuration, so it is used the next time `top` is started

An easier alternative to `top` is `htop`, but typically it must be installed. Compared to `top`
it is quite self-explanatory.
