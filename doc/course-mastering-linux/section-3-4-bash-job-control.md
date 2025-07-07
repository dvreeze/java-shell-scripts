
### 3.4. Bash: Job Control

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

A *job* is a *command that can be executed*. It can contain *multiple programs*. For example,
a command consisting of a Linux pipeline consists of multiple programs that make up the pipeline.
When the command is running, the programs in that command are running as individual but
typically related *processes*.

If the command is a Linux pipeline, the individual programs in the pipeline run as (typically
simultaneously) running processes, which are linked to each other through output and input streams of data.

For example: command `cat file.txt | wc` is one command, consisting of 2 simultaneously running
programs, or processes. The running  `cat file.txt | wc` command is 1 *job*.

Important note:
* we know that the notion of a *process* in Unix systems is quite fundamental in the *core kernel*
* yet, the notion of a *job* exists only in the *shell* on top of the kernel, in this case Bash
  * so, the notion of a job is not a kernel notion

TODO
