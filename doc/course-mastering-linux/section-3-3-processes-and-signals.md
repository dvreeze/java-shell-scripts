
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
  * so `ps -e -f` or ```ps -ef` shows all processes, showing extended information in the output
* `ps -l` shows processes in a longer format, adding a few more columns, compared to option `-f`
* `ps --forest` shows processes as an ASCII tree
  * `ps -ef --forest` is like `ps -ef`, yet additionally making clear where they occur in the process hierarchy
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
Then we could do things like: `renice -n 19 $(pgrep firefox)` (without double-quote pairs).
