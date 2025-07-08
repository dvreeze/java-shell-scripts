
### 3.4. Bash: Job Control

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

A *job* is a *command that is being executed*. It can contain *multiple (running) programs*. For example,
a command consisting of a Linux pipeline consists of multiple programs that make up the pipeline.
While a job is running, the programs in that job/command are running as individual but typically related
*processes*.

If the job is a Linux pipeline, the individual programs in the pipeline are each running as a
process, and these processes are linked to each other through output and input streams of data.

For example: command `cat file.txt | wc` is one command, consisting of 2 simultaneously running
programs, or processes. The running  `cat file.txt | wc` command is 1 *job*.

Important note:
* we know that the notion of a *process* in Unix systems is quite fundamental in the *core kernel*
* yet, the notion of a *job* exists only in the *shell* on top of the kernel, in this case Bash
  * so, the notion of a job is not a kernel notion

Jobs can be *foreground jobs* (what we have seen so far) or *background jobs*.

A *foreground job* occupies the shell until the job has finished. Only after completion of the job
a new command can be accepted by the shell.

A *background job*:
* can be started with a `&` at the end; e.g. `ping -c 10 www.google.com &`
* the output is still displayed in the shell, unless redirected, of course
* the background job can take no keyboard input from the shell
  * that includes signals, such as `SIGINT` (CTRL-C)
  * so even signals triggered by the keyboard will NOT notify background jobs
* on starting of the job, job ID and process ID are displayed

Background jobs can be found with command `jobs`. The result is a list of job IDs.

A background job can be turned into a foreground job by command `fg %some-job-id`.
Note the `%` sign, which is used to refer to jobs, as opposed to processes.
When using command `fg` without arguments, the current job (marked with a plus sign in the "jobs table")
is brought back to the foreground.

*Suspending* jobs:
* a foreground job can be *suspended*, i.e. sent to the background, by entering the suspend character (typically CTRL-Z)
* this sends a `SIGTSTP` signal to the job
  * `SIGTSTP` is a nicer version of `SIGSTOP`; it asks the program to pause execution and return control to the terminal
  * the program can ignore it

*Resuming* a suspended job:
* resuming in the foreground: `fg %job-id`
* resuming in the background: `bg %job-id`
* when continuing, a `SIGCONT` signal is sent to the program (in that sense similar to `SIGCONT` after `SIGSTOP`)

Killing a job is analogous to killing a process:
* `kill %job-id` is equivalent to `kill -s SIGTERM %job-id`
* `kill -s SIGKILL %job-id` if the former does not work

If we want to see the whole output of a background job, but do not want it to directly write to the console,
we can use command `stty tostop`:
* `stty` changes or prints terminal line settings
* the `tostop` option tells the terminal to suspend the job if when creates any output
* so, after `stty tostop`, when we start a background job, it will only run until it creates any output
* i.e., once it writes any output, it will be suspended immediately
* then we can disable this feature with `stty -tostop`

Suppose we have 3 big downloads, each of them running as a background job. We can wait for them
and execute another command once all of them have finished, using the `wait` command:
* `wait` without arguments waits until all currently running background jobs have changed their state
* `wait 12345` waits for *process* 12345
* `wait %1` waits for background *job* 1
* `wait -n` waits for any job to be completed

We can keep a program running even after closing a terminal with the `nohup` command:
* e.g. `nohup ping -c 100 www.google.com &`
  * even after closing the terminal or logging out the program keeps running in the background
  * then standard output will be redirected, e.g. to "nohup.out" (in current or else home directory)
* `nohup ping -c 100 www.google.com` (note the absence of `&`) is a foreground process
  * but `nohup` disconnects the program from the `SIGHUP` signal
  * so it will keep running after the terminal has closed
* `ping -c 100 www.google.com &` (without `nohup`) is a normal background process
  * it will terminate when the terminal closes, due to the `SIGHUP` signal it will receive
