
## Part 2. Bash CLI

For the overview page, see [Overview](./overview.md).

The `bash` shell is used, not `sh` or `zsh`.

Small syntactic differences in shell commands can lead to quite different output.

Note that many commands depend on "*implicit context*", such as the working directory, current user, etc. etc.
(which tend to be "environment variables"). From a programmer perspective, think "(hidden) global variables".

Commands can take a *path*. An *absolute path* starts with a slash. A *relative path* does not, and it is resolved
against the current working directory.

Getting help on a command:
* e.g. `ls --help`
* e.g. `man ls`

Commands can also include *globs*. See a separate section on that, and about how globs are interpreted.

### 2.1. Small introduction to user management and `sudo`

TODO

### 2.2. Some basic commands

Some basic commands are:
* `echo`
    * `echo 'Hello world'`
    * `echo -e 'Hello\nWorld'`
    * `echo -n 'Hello '; echo 'World'` (chaining commands with a semicolon)
* `pwd` (print working directory)
* `cd` (change directory)
    * `cd ~/Desktop` (absolute path)
    * `cd ..` (relative path, resolved against current directory)
* `ls` (list contents)
    * `ls` (current directory)
    * `ls ./Documents` (relative path)
    * `ls ~/Desktop` (absolute path)
    * `ls -a` (list hidden files, starting with dot, as well)
    * `ls -a -l`, or equivalently: `ls -al`
    * `ls -al -h`, or equivalently: `ls -alh` (human readable)
    * `ls -r` (reverse order)
    * `ls -t` (sort by modification time, newest first)
    * `ls -ta --color=never ~/Documents` (color: always, never, or auto)
* `touch`
    * creates a new empty file if not yet existing; otherwise just updates the timestamp
* `mkdir` (make directory)
* `rmdir` (remove empty directory; safer than `rm -R`)
* `mv` (move, and/or rename)
    * `mv myfile.txt my_file.txt` (rename file)
    * `mv ./my_file.txt ~/Documents` (move file to target directory)
    * `mv my_file.txt ~/Documents/some_file.txt` (move and rename at the same time)
* `cp` (copy)
    * `cp my_file.txt other_file.txt` (make a copy of a file, with a different file name)
    * `cp ./my_file.txt ~/Documents` (make a copy of a file, in the given target directory)
    * `cp my_file.txt ~/Documents/some_file.txt` (copy and rename the copy at the same time)
    * `cp -R directory1 directory2` (copy an entire directory tree to another directory tree)
* `rm` (remove a file or directory; potentially very unsafe)
    * `rm my_file.txt`
    * `rm -r obsolete_directory` (dangerous!)
* `tree`
* `diff`
* `find`
    * `find . -type d` (find directories, from the current directory)
    * `find . -type f` (find regular files, from the current directory)
    * `find . -type f -size +100M`

Some basic text file management commands are:
* `cat`
* `head`
    * `head -n 20 data.txt`
* `tail` (similar to `head`, but at the end of the text file rather than the beginning)
* `less`
    * TODO
* `wc` (word count)
    * `wc -l -w -c file.txt`, or `wc -lwc file.txt`, or `wc file.txt`
* `du` (disk usage; not only for text files, by the way)
    * `du -k my_file.txt` (file size in kilobytes)
    * `du -h my_file.txt` (file size in human-readable format)
    * `du -s my_directory` (in the given directory, file sizes as summaries)

### 2.3. Globbing

Wildcards: `*`, `?`, `[a-z]` etc. Even `**` to match slash(es) as well.

*Important*:
* Globbing is interpreted *before* running the command; it basically first resolves the glob and then rewrites the command!
    * This can be quite surprising in combination with commands such as `find` (see [find command and globbing](https://blog.robertelder.org/find-command-wildcard-globbing/))
    * We can surround the "glob" with single-quote-pairs to switch off globbing "pre-processing"
* It is often safer to start a glob with `./`

### 2.4. Standard streams and redirection

The *standard data streams* are *stdin* (number 0), *stdout* (number 1) and *stderr* (number 2).
By default, stdin takes input from the keyboard, and stdout as well as stderr write output to the console.
These streams can be redirected, though.

Note that commands such as `wc` and `cat` (`cat - `) can take input from stdin (ending the input with CTRL-D).
There are many other such commands, like `sort`, `uniq` etc.

Examples of *redirecting standard streams*:
* redirecting stdout and overwriting: `echo 'Hello World' > welcome.txt`
    * equivalently (being more explicit): `echo 'Hello World' 1> welcome.txt`
* redirecting stdout and appending: `echo 'Hello World' >> welcome.txt`
    * equivalently (being more explicit): `echo 'Hello World' 1>> welcome.txt`
* redirecting stdout and stderr: `du -h existing_file.txt non_existing_file.txt > output.txt 2> error.txt`
* redirecting stderr to *current* stdout: `du -h existing_file.txt non_existing_file.txt > output.txt 2>&1`
* redirecting stderr to *current* stdout: `du -h existing_file.txt non_existing_file.txt 2>&1 > output.txt`
* redirecting stdin: `wc -l < file.txt` (not very useful; prefer `wc -l file.txt`)

### 2.5. Pipes

a *pipe* (`|`) passes the (stdout) output of one command as (stdin) input to another command. Pipes are quite often chained
in so-called pipelines.

This is typically preferable to creating intermediate files using stream redirection (as shown above).

The second command of a pipe must accept input from stdin. Some such commands are `tee`, `sort`, `uniq`, `grep`, `tr`, `rev`,
`cut` and `sed`. Indeed, just try out using these commands directly on stdin (from the terminal, ending input with CTRL-D),
without pipes. Of course, in practice these commands are used in "chains of pipes", i.e.
[pipelines](https://www.gnu.org/software/bash/manual/html_node/Pipelines.html).

Remark: if a pipe starts with (plain) command `ls`, the output contains multiple lines. It is just the terminal that
replaces newlines by spaces. So, in a terminal, `ls | cat` behaves like `ls` behaves in a pipeline.

Examples of using the above-mentioned commands with pipes:
* `tee`:
    * `echo 'Hello' | tee hello.txt` (write to the output file, but also write the same output to the console)
    * `echo 'Hello' | tee -a hello.txt` (appending)
* `sort`:
    * `ls | sort` (sort in alphabetical order)
    * `ls | sort -r` (sort in reverse alphabetical order)
    * `echo -e '3\n2\n1\n4\n2' | sort -n` (sort in numerical order)
    * `ls -l | sort -k 9` (sorting on column, 1-based; in this case sorting on the file name)
    * `ls -l | sort -k 9 -r` (reverse sorting on column, 1-based; in this case reverse sorting on the file name)
* `uniq`:
    * `sort names.txt | uniq` (`uniq` works only on consecutive lines; hence the sorting before that)
    * equivalent (without using `uniq`): `sort -u names.txt`
    * finding duplicates: `sort names.txt | uniq -d`
    * getting the number of occurrences as well: `sort names.txt | uniq -c`
* `grep`, without using regular expressions:
    * `cat file.txt | grep -F 'hello'` (we could have just written: `grep -F 'hello' file.txt`)
    * please use `grep` only for text files (for more reliable results, for performance and for avoiding non-printable chars)
    * note that non-printable characters can change the behavior of the terminal
* `tr` (translate):
    * `echo 'bash' | tr 'b' 'd'`
    * `echo 'hi' | tr [a-z] [A-Z]` (converting to upper-case)
    * `echo 'this is great' | tr -d ' '` (removing characters, in this case removing the spaces)
* `rev` (reverse):
    * `echo 'not readable' | rev` (outputs: "elbadaer ton")
* `cut`
    * `uptime | cut -c 1-9` (outputs the first 9 characters; here it may or may not start with a space)
    * worse: `uptime | cut -b 1-9` (outputs first 9 bytes instead of characters; this ignores character encodings, though)
    * common use case of extracting "fields": `cat access.log | cut -d ' ' -f 7` (getting file name; separator space, not tab)
* `sed` (stream editor):
    * can delete, insert or substitute lines; substitution is the most common use case of `sed`
    * command pattern: `sed 'command1; command2; ...'`
    * uses regular expressions
    * example: `cat file.txt | sed 's/hello/HELLO/g'` (global substitution, replacing "hello" by "HELLO" in the line)

#### 2.5.1 Detour: Analogy with streams in Java, and writing shell scripts in Java

As an aside (not in the course material), in the Java programming language we can write programs to invoke commands
and create and run command pipelines.
See [ProcessBuilder](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ProcessBuilder.html) and
[Process](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Process.html).

From this Java API we can see that the command's stdin is exposed as a `java.io.OutputStream` (so not as an input stream).
This makes sense, because the Java program invokes a Linux command, whose stdin receives bytes from the output stream
that the Java program writes to. Likewise, stdout and stderr are exposed as `java.io.InputStream` instances, in order
to have the Java program read bytes from the invoked command's stdout and stderr, respectively.

When invoking "text processing" commands from a Java program, we can work at a slightly higher abstraction level, using
a `java.io.BufferedWriter` connected to the command's stdin, and `java.io.BufferedReader` instances connected to the command's
stdout and stderr, respectively.

We could also write new Linux commands *as Java programs*. Such programs run *as Linux processes*. The stdin of such a program
would be a `java.io.InputStream` (namely `System.in`), and the stdout and stderr would
be `java.io.OutputStream` instances (namely `System.out` and `System.err`, respectively). If the program/command
is a "text processing" command, the stdin could be a `java.io.BufferedReader` (backed by `System.in`) and stdout and
stderr could be `java.io.BufferedWriter` instances (backed by `System.out` and `System.err`, respectively). This makes
perfect sense when looking at the above-mentioned
[Process](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Process.html) API, which "connects" to these
streams.

Note that `BufferedReader` has a
[lines](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/BufferedReader.html#lines()) method,
turning the `BufferedReader` into a `Stream<String>` *Java Stream* of lines. The *Java Stream pipeline* could then
end with a
[forEach](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html#forEach(java.util.function.Consumer))
method call that writes output lines to the stdout/stderr `java.io.BufferedWriter`. This is (potentially) quite
memory-efficient, and in principle allows for text processing of very large files.

Hence, Java programmers can understand and even implement Linux commands using *Java I/O streams* for the standard streams
(stdin, stdout and stderr), and even to a large extent implement the commands as *Java Stream pipelines*.

For more information on how to use Java to write shell scripts, see
[Revisiting Java for Shell Scripting](https://belief-driven-design.com/revisiting-java-for-shell-scripting-7ff3e/) and
[JEP 445 (shorter scripts)](https://softwaregarden.dev/en/posts/new-java/scripts/shorter-scrips-jep-445/).

A trivial "Java shell script" named `today` could look like this (don't forget to give it execution rights through `chmod +x`):

```java
#!/usr/bin/env -S java --source 17

import java.time.LocalDate;

// Using JEP 330 (launch single-file source-code programs)
// No use of JEP 445 (unnamed classes and instance main methods)
// That JEP is in preview in Java 21

public final class Today {

    public static void main(String... args) {
        System.out.println("Today is: " + LocalDate.now());
    }
}
```

Note that the *Java Stream API*, when used for element type `String`, can help understand *Linux pipelines* from a Java
developer point of view. For example:
* the Java `Stream.filter` higher order function helps understand the Linux `grep` command in a Linux pipeline
* the Java `Stream.map` higher order function helps understand Linux commands transforming streams of lines of text
    * e.g. `cat access.log | cut -d ' ' -f 7`, where the "mapping function" returns the 7th column of each line of text
    * e.g. `cat file.txt | sed 's/hello/HELLO/g'`, where the "mapping function" replaces `hello` by `HELLO` in each line of text
* etc.

*Java Streams* also offer a good mental model for *Linux pipelines* in that intermediate data is not stored but efficiently
"streamed" through the Linux pipeline. Like mentioned earlier, both Java Stream pipelines and Linux pipelines can be quite
memory-efficient even when processing very large files.

*Java Streams* als offer a good mental model for JSON processing using the Linux `jq` command. Suppose we want to analyse
Apache server access logs, not at the low abstraction level of individual lines of text, but at the abstraction level of
JSON objects, one JSON object per line in the access log. We would need a command to turn each line in the access log into
a JSON object. Let's call that command `convert_access_log_to_json`. This script could well be implemented in Java, using
a regex to recognise an access log entry, turning it into a JSON object.

Now consider the following Linux pipeline:

```bash
cat access.log | convert_access_log_to_json | jq '.[] | .request.requestUrl'
```

Assume `convert_access_log_to_json` turns the access log into a JSON array (in a streaming fashion, keeping only individual
log entries in memory). Then the `jq` command above processes that JSON array, using its own "programming language",
to manipulate "JSON pipelines". The "inner JSON pipeline" could then be understood "in Java terms" as a `Stream<JsonValue>`
pipeline, for some imaginary `JsonValue` element type.

Assuming we also have a command `ltrim` that does what it says ("left trim each line"), we could find the 10 most frequently
occurring request URLs as follows:

```bash
cat access.log | convert_access_log_to_json | jq '.[] | .request.requestUrl' |
    sort | uniq -c | ltrim | sort -k 1 -n -r | head
```

### 2.6. Managing the shell configuration, and environment variables

In general, a *shell* is an outer layer of the OS, translating commands from the user to "kernel calls", displaying the
output to the user. Besides the CLI shell, this includes GUI shells etc. The term *Linux shell* refers to the CLI
(Command Line Interface) shell, i.e., the "terminal" or "console". The CLI is efficient and requires no GUI.

*Environment variables* have the following characteristics:
* they store configuration data and settings, e.g. the current working directory, current user, current "path"
* they can be regarded as "*implicit context*" to commands (or "*global variables*" used by commands)
* they *influence* shell and program behavior; e.g. the current working directory is used to resolve relative paths
* they are not a feature of the (bash) shell, but they are a feature provided by the *operating system*
* they are available to programs in other programming languages, e.g. Python and Java (using language-specific syntax)
    * consider using a *shebang* in such Python or Java programs (or programs in other programming languages)
* they have names that are *case-sensitive*
* yet, by convention, they should have names in *uppercase*, and use *underscores* instead of *dashes*
* they can be listed with command `env`
* they can be shown like this: `"${PWD}"`
    * the `${ ... }` syntax refers to the variable
    * the double quote pairs suppresses "command rewriting"
    * example: `echo "The current working directory is: ${PWD}"`
    * using single quotes does not expand the variable: `echo 'The current working directory is: ${PWD}'` (not ok)
* some important environment variables are:
    * `HOME`: the current user's home directory
    * `PWD` (related to `pwd` command): the current working directory (`OLDPWD` refers to the previous working directory)
    * `USER` (related to `whoami` command): the current user's user name (not to be confused with a label for the user)
* setting/unsetting/updating environment variables:
    * create: `export VAR=value` (important: no space around `=`)
    * delete: `unset VAR`
    * update existing variable: `VAR=new-value` (important: no space around `=`)
* a subshell (see output of `ps -f`, for shell and parent shell) can create own environment variables
    * this is a "layered system"; "ancestor shells" keep their environment, no matter what is updated in subshells in this regard
    * yet make sure that new environment variables are introduced with `export`, or else they easily "get lost"
    * leave the subshell with `exit`, thus returning to the parent shell of the subshell
    * to run a script in the current shell instead of its own subshell, run: `source myscript.sh` or `. myscript.sh`
* the `PATH` environment variable stores a colon-separated list of directories
    * it is used to search for directories containing executable programs
    * order matters: directories are searched from left to right, and the first match is used
    * command `which` shows how the path of a command is resolved; e.g. `which cat`
    * of course, the command can also be called using its full path; e.g. `/usr/bin/cat`
* modifying the `PATH` variable:
    * for example, this is needed if we need a separate directory to store specific executables in
    * pattern: `PATH="${PATH}:/new/path"`
    * common issue: "command not found"; troubleshoot with `echo "${PATH}"` or `which` command
* `PATH` best practices:
    * keep system directories at the beginning, and try to avoid changing the `PATH` for system-wide changes
    * try not to duplicate directories, and clean up the `PATH` on a regular basis, keeping it lean and mean
* `SHELL` contains the path to the user's *default* shell (inherited by subshells)
    * changing the shell, to take effect after the next login: `chsh -s "/bin/bash"` (change shell)
    * this must be a shell in `/etc/shells`

*Single-user mode* is used to launch Linux if it is broken and needs fixing, with access to essential tools.

The *Filesystem Hierarchy Standard* standardizes where to place files in the filesystem. Yet why multiple locations for
commands?

Different paths of binaries:
* Essential binaries, always available: `/bin`
* Essential binaries, always available, usually requiring root access: `/sbin`
* Non-essential binaries (for all users), can be shared with other computers: `/usr/bin`
* Non-essential binaries, usually requiring root access, can be shared with other computers: `/usr/sbin`
* Non-essential binaries (for all users), specific to this host: `/usr/local/bin`
* Non-essential binaries, usually requiring root access, specific to this host: `/usr/local/sbin`

When creating own commands, do not forget to make them executable: `chmod +x my_command`. See output of `ls -l`.

### 2.7. Persistently storing the user's own configuration

Bash has many startup files, such as `~/.bash_profile`, `~/.bash_login`, `~/.profile`, and `~/.bashrc`.
So where should we store *(our) persistent configuration*?

There are different *startup modes*:
* *interactive login shell*
    * we need to login; example: (one-liner) `ssh` command
* *interactive non-login shell*:
    * e.g. opening a terminal on our desktop environment (we had already logged in before)
    * e.g. using `bash` command within an already existing terminal
* *non-interactive non-login shell*:
    * running a script, such as `bash script.sh` or `./script.sh`
* *non-interactive login shell* (quite rare):
    * e.g. sending a command with `ssh` to a remote server, but without starting a shell

Bootstrapping for different startup modes:
* *interactive login shell*:
    * first `/etc/profile` is loaded
    * then the first existing file of `~/.bash_profile`, `~/.bash_login` and `~/.profile` is loaded
* *interactive non-login shell*:
    * `~/.bashrc` is loaded
    * note that the interactive login shell startup files may ultimately refer to `~/.bashrc`
* *non-interactive shell* (login or non-login):
    * bash looks for environment variable `BASH_ENV`
    * if found, it is tried to execute this file (but without looking at `PATH` variable)

### 2.8. Aliasing

Aliasing can be used to provide shortcuts to commands:
* e.g. `alias gohome='cd ~'`, followed by 0 or more invocations of command `gohome`
* or even `alias ls='ls --color=auto'`; note the "recursion" (which is not really recursion, but more like an "update")
* listing aliases with `alias`
* removing an alias: `unalias gohome`
* an alias is not persistently stored; for that we would have to add it to a startup file
* aliases are often used to shorten `git` commands (e.g. `gitc` for `git checkout`)

### 2.9. Configuring the shell with `set` and `shopt`

With `set` we can change the behavior of the shell using pre-defined configuration options:
* pattern for enabling: `set -feature` (note the dash or minus sign; it is still enabling, not disabling)
    * e.g. `set -x` to troubleshoot command execution
* pattern for disabling: `set +feature` (note the plus sign; it is still disabling, not enabling)
* see [builtin set](https://www.gnu.org/software/bash/manual/html_node/The-Set-Builtin.html)

Similar to `set` is `shopt` ("shell options"):
* pattern for enabling: `shopt -s optname`
* pattern for disabling: `shopt -u optname`
* see [builtin shopt](https://www.gnu.org/software/bash/manual/html_node/The-Shopt-Builtin.html)
* example: `shopt -s autocd` (enabling changing the current directory without `cd`; not recommended!)
* example: `shopt -s cdspell` (enabling `cd` to directory with minor spelling errors; not recommended!)

Why both `set` and `shopt`? The former command is inherited from `sh`. The latter command is specific to bash.

### 2.10. Command substitution (introduction)

We have already seen (environment) variable substitution, and we used double quote pairs to see that work (without any
further processing, like command rewriting).

Similarly, we can use *command substitution*, also preferably within double quote pairs. For example:

```bash
echo -e "This is the output of the ls command:\n$(ls)"
```

The syntax is similar to variable substitution, but instead of braces command substitution uses *parentheses*.

### 2.11. Colorful bash prompts

The *prompt* is *shell variable* `PS1` (Prompt String 1):
* it defines the appearance of the primary shell prompt
* it can be customized with escape sequences and special characters
* it is a *shell variable*, not an environment variable, and it is not exported to child processes
    * hence, `env` does not show it

Example: `PS1='\u@\h:\w$ '`. Here:
* `\u` is the user name
* `\h` is the short host name (the first part of the dot-separated host name)
    * `\H` would be the full host name
* `\w` is the current working directory as full path
    * `\W` is the last directory of the current working directory
* `\t` is the time in 24-hour format
    * `\@` is the time in 12-hour format (with am / pm)

See [PS1](https://www.gnu.org/software/bash/manual/html_node/Controlling-the-Prompt.html).

We can use colors in the prompt too. Colors are part of the text to output to the terminal, as *escape sequences*.
This is essentially the same protocol that was used by VT100 terminals in the distant past, to show colors on the screen.

Terminals have different abilities in this respect (e.g. no colors, 16 different colors, 256 different colors).
With environment variable `TERM` we can tell bash which capabilities should be used. A common value of `TERM`
is `xterm-256color` (meaning: 256 different colors).

The available modes are returned by command `toe` or `toe -a` (table of terminfo entries). Environment variable `TERM`
should choose one of those available modes, such as `xterm-256color`.

To set (foreground and background) colors, we send *escape sequences* inside the text. For example:

```bash
echo -e "\e[30;40m"
```

The escape sequence is recognized as such by prefix `\e[`. The foreground color is 30, which stands for a black foreground.
The background color is 40, which stands for a black background. The escape sequence (in this case) ends with `m`.
From that point on, the escape sequence should be applied to the text, until overridden by another escape sequence.

See [escape sequences](https://en.wikipedia.org/wiki/ANSI_escape_code#3-bit_and_4-bit).

Which escape sequences can the terminal handle? For that, see the output of command `infocmp`.

With command `tput` we can avoid having to use escape sequences directly. Examples of using this command:
* `tput clear` (clears the terminal, just like `clear`)
* `tput cup 4 25` (moves the cursor to position 5 (vertical) an 20 (horizontal))
* `tput bold` (start bold font)
* `tput sgr0` (resets our font/terminal configuration)
* `tput smul` and `tput rmul` (start and end underlined text, respectively)
* `tput setaf 2` (set the foreground color to green)
* `tput setbf 1` (set the background color to red)
* `tput colors` (query for tne number of different colors)
* `tput cols` (query for the number of columns in the terminal)
* `tput lines` (query for the number of lines in the terminal)

See [tput](https://linuxcommand.org/lc3_adv_tput.php) for more on `tput`, including the color numbers.

We can use command substitution for `tput` repeatedly in the value of the `PS1` shell variable.
Yet there is a potential issue: escape sequences count as characters, but it would confuse the terminal, when using arrow keys.
Solution: wrap all use of `tput` in the `PS1` variable value inside `\[ ... \]`. Then they won't count as normal characters,
and bash knows how long the `PS1` prompt is.

Tips:
* The assigned `PS1` value should be closed in *pairs of double quotes*, when using variable/command substitution
* `PS1` is evaluated *only once* during assignment, so in `PS1="$(ls)"` the `ls` command is evaluated only once
* When needed, *escape characters*, such as the dollar sign if it can be mistaken for its use for a variable
* In principle, all *Unicode* characters can be used, to the extent the terminal supports them
* At the end of the `PS1`, *reset* the terminal configuration, with `$(tput sgr0)`

Personal note: it is common to show the current user, host and working directory in the prompt. In any case, whether or not
this information is in the prompt, we can always query for that. When we hop from machine to machine, it is often needed
to know who and where we are. So the following commands come in handy in that regard:
* the "who": `whoami`
* the "where": `uname -n` (host name), and `pwd`

### 2.12. Shell Expansions

Before running a command, the shell performs all kinds of *shell expansions* before running the command.
This is described in detail in [Shell Expansions](https://www.gnu.org/software/bash/manual/html_node/Shell-Expansions.html).

We can prevent many kinds of shell expansions with *double quote pairs*, while allowing for variable substitution and
command substitution. With *single quote pairs* we prevent pretty much all kinds of shell expansions.

The *mental model* for understanding quotes and shell expansions in Linux is *quite different from traditional programming*.
In programming languages, *syntax* often carries *semantics* related to *data types*, and different syntax is used for literals
of different types, for example. In Java, single-quote pairs are used for single characters, double-quote pairs are used
for strings of characters, etc. Not so for the Linux shell: single-quote pairs and double-quote pairs in Linux
*carry no ("data-type-related") semantics*, but influence *shell expansions* and *word splitting*. Besides defining how
bash expands and splits a command, *quotes do nothing else*. They are not even seen by the command once it runs after all
substitutions, word splitting and quote removal have been done!

For example:

```bash
echo 'Hello world'
# this is equal to:
echo 'Hello'" "'world'
# but also to:
echo 'Hello'" "world

echo hello
# this is equal to:
echo 'hello'
# this is also equal to:
'echo' 'hello'
# and, given that there is nothing to expand here, it is equal to:
"echo" "hello"
"echo" "hel"'lo'
```

Mind the different processing of:
* `cat '$PWD/*.txt'` (interpreting the literal command argument string (within quotes) as a file name, which would not exist)
* `cat "$PWD/*.txt"` (interpreting the command argument after variable expansion as a file name, which likely does not exist)
* `cat $PWD/*.txt` (both variable expansion and filename expansion, which finds the text files in the directory, if any)

A *command line is processed* as follows:
1. the command line is *tokenized*; i.e. it is parsed into a list of tokens, so of words and operators
2. the *shell expansions* are performed, in a strict order
3. unnecessary pairs of (single or double) *quotes are removed*
4. the command is *executed*

The *shell expansions* are as follows, in the given order:
1. *brace expansion* (can increase the number of words of the expansion)
2. *tilde expansion*
3. *shell parameter and variable expansion*
4. *command substitution* (and *arithmetic expansion*)
5. *word splitting* (can increase the number of words of the expansion)
6. *filename expansion* (can increase the number of words of the expansion)

Pairs of *single quotes* disable all these expansions and word splitting within their scope.
Pairs of *double quotes* disable all these expansions and word splitting, with the exception of *parameter/variable
expansion* and *command substitution*.

Also note that pairs of single quotes or pairs of double quotes *cannot be nested*. After all, they are not like braces or
parentheses, where we have a difference between starting brace/parenthesis and ending brace/parenthesis.

*Brace expansion* is rather rare, but it does occur sometimes:
* `echo data.{csv,txt}` resolves to `echo data.csv data.txt`
* `echo {A..D}` resolves to `echo A B C D`
* brace expansion does not work within (single/double) quotes (but this works: `echo 'test'{.txt,.csv}`)

*Tilde expansion* lists the `HOME` folder, so `ls ~` is equivalent to `ls "${HOME}"`.
Similarly, `ls ~+` is equivalent to `ls "${PWD}"`, or `ls "$(pwd)"`.

We have seen *variable expansion* before. Recall the best practice w.r.t. use of (double) quotes. E.g. `echo "${USER}"`.
Indeed, suppose the current directory contains spaces in its directory name. Then:

```bash
# Does not work (if the current directory contains spaces in its directory name)
cat $PWD/file.txt

# Instead use a double-quote pair (around variable expansion)
cat "${PWD}/file.txt"
# or, equivalently:
cat "${PWD}"/file.txt
```

Given a variable, we can use it in *shell parameter expansion*. For example:

```bash
# Query for the string length of the HOME variable
echo "${#HOME}"

# Substring extraction, with 0-based indexing, starting at position 0, using length 3
echo "${HOME:0:3}"

# Replacing a substring one time
echo "${HOME/pattern/replacement}"

# Replacing a substring, all occurrences
echo "${HOME//pattern/replacement}"
```

We have seen *command substitution* before. Some characteristics of command substitution are:
* syntactically it looks like variable expansion, except for the use of *parentheses* instead of braces:
    * e.g. `echo "$(cat file.txt)"` (also here using double-quote pairs, just like we do for variable expansion)
    * an alternative syntax uses backticks for command substitution, but that is less readable
* the "inner command" is executed in a subshell, so in its own OS process

There is also a concept called *process substitution*:
* using the output of a process as a temporary file: e.g. `echo <(ls)` or `wc -l <  <(ls)`
* using a temporary file as input to a command: e.g. `echo "test" >  >(cat)`

*Word splitting* is done after all these expansions (but before filename expansion). Word splitting:
* is rather straightforward: `touch a.txt b.txt` is split into 3 words, the first one being the command, followed by arguments
* occurs at characters in variable `IFS`, which are whitespace characters (do `echo "${IFS}" | hexdump` to obtain them)
* treats sequences of `IFS` characters (spaces, tabs, newlines) as one single delimiter
* can be partially disabled by wrapping parts of the command into quotes; e.g. `touch 'a file.txt'` (2 words)

We have seen *filename expansion* before, in a section about *globbing*.

*Best practices* w.r.t. expansions and quotes:
* Try to refer to files in the current directory as follows: `./file.txt`
* Prefer the *quoting style* that is as *restrictive* as possible
    * Prefer *single quotes*
    * Otherwise, use *double quotes* if possible (e.g. `echo "hello ${USER}"` or `echo 'hello '"${USER}"`)
* Use *no quoting* if there is *no ambiguity* or if *all expansions should be applied*:
    * no ambiguity: `ls -al` (`'ls' '-al'` is defensive, but not really necessary here)
    * all expansions needed: `echo ./*.txt`

Sometimes we want to disable bash from performing *default actions*. For example, we autocomplete a filename that
contains a space. The autocompletion may look like `cat a\ file.txt`. Here the *backslash* disables the default action,
which would be word splitting. So the `cat` command has just one argument (with a space), instead of 2 arguments.

Note that this *escaping* also "rewrites" the command. Single-quote pairs disable all expansions and "rewrites", so they
also disable the baskslash. So this does not work: `'\''`. Yet this does: `"\""` (although this is better: `'"'`).
