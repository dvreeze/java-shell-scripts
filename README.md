# Shell scripts written in Java

This project contains bash shell scripts written in Java.

The scripts benefit from JEP 330 (launch single-file source-code programs).
JEP 445 (unnamed classes and instance main methods) has not been used.
The latter JEP is still in preview in Java 21.

The following script `today` is a very simple Java shell script:

```java
#!/usr/bin/env -S java --source 17

import java.time.LocalDate;

public final class Today {

    public static void main(String... args) {
        System.out.println("Today is: " + LocalDate.now());
    }
}
```

Do not forget to make the scripts executable (via `chmod +x`).

While making progress on the Udemy course "Mastering Linux: The Comprehensive Guide"
by Jannis Seemann these scripts will improve.
