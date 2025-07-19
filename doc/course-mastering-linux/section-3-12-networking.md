
### 3.12. Networking

For the overview page for part 3, see [Part 3. Linux, Core Concepts](./part-3-linux-core-concepts.md).

This section explains *networking* in general and *Linux commands concerning networking* in particular.
In a sense, besides a discussion of some Linux networking tools, this section is about *how the internet
works*.

The material in this section will also help with troubleshooting networking issues, whether in a work
environment or at home.

#### 3.12.1. OSI model

Networking is discussed in this section using the *OSI model* (*Open Systems Interconnection*)
as guideline. This model defines *7 layers*. This layered model helps improve interoperability between
different network devices and network technologies, but it also helps our understanding of this
vast and complex topic.

The 7 layers, from bottom layer to top layer are:
1. *physical layer*; this layer handles transmission of raw data *bits* over physical media (such as cables, switches etc.)
2. *data link layer*; this layer provides error-free transfer of so-called *frames* between *adjacent* network nodes (each node having a *MAC address*)
3. *network layer*; this layer manages routing and forwarding of so-called *packets* between networks; *IP* belongs to this layer
4. *transport layer*; this layer ensures reliable transfer of so-called *segments* between host/application pairs; *UDP* and *TCP* belong to this layer
5. *session layer*; this layer manages *sessions* between applications
6. *presentation layer*; translates, encrypts and compresses data for transmission between applications and the network; *TLS/SSL* belongs to this layer
7. *application layer*; high-level *network protocols* for applications communicating over a network; e.g. `http`, `tfp, `smtp`

Linux commands that are used extensively w.r.t. networking are:
* the `ip` command, which replaces commands like `ifconfig`, `route` and `netstat`
* `wireshark`; be **extremely careful** not to use this tool without permission outside our local home network!

As for troubleshooting networking issues, each OSI layer depends on lower layers to work properly. So this
layering helps pinpoint problems. For example, we cannot expect any IP packets to arrive at their destination
if communication in the data link layer (within a WIFI/Ethernet local network) is not working properly.

#### 3.12.2. Physical layer

TODO

#### 3.12.3. Data Link layer

TODO

#### 3.12.4. Network layer

TODO

#### 3.12.5. Transport layer

TODO

#### 3.12.6. Session layer

TODO

#### 3.12.7. Presentation layer

TODO

#### 3.12.8. Application layer

TODO
