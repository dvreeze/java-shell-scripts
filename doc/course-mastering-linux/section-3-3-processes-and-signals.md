
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

#### 3.3.1. Signals

*Signals*  can be sent to processes, *interrupting* the process flow at a *convenient* time
("convenient" does NOT mean that it will take long before interrupting the process flow).

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
  * it means: dear process, please try to come to an end, because we are in a shell and would like to regain control
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

If we end a program with CTRL-C in a shell, the `SIGINT` signal is sent to that program.
It is sent from the terminal in order to regain control over the terminal. The program can
listen to the signal and perform custom actions, including ignoring the signal!

The command to *send signals* is called `kill`. Despite the name, the command can send any signal.
Syntax (assuming the process to which the signal is sent is 12345, and the signal is `SIGINT`):
* `kill -s SIGINT 12345`
* or: `kill -SIGINT 12345`
* or: `kill -s 2 12345` (`SIGINT` has number 2)
* or: `kill -2 12345`

Command `kill 12345` is identical to `kill -s SIGTERM 12345`, so `SIGTERM` is the default signal sent by
that command.

We can also select the process(es) by name, using the `killall` command (again, this command is for sending any signal):
* `killall -s SIGINT firefox`
* or: `killall -SIGINT firefox`

Command `kill -l` outputs a list of signals with their numbers (e.g. 15 for `SIGTERM`, and 9 for `SIGKILL`).

