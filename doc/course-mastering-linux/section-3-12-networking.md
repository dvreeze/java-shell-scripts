
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

The 7 layers of the OSI model, from bottom layer to top layer are:
1. *physical layer*; this layer handles transmission of raw data *bits* over physical media (such as cables, switches etc.)
2. *data link layer*; this layer provides error-free transfer of so-called *frames* between *adjacent* network nodes (each node having a *MAC address*)
3. *network layer*; this layer manages routing and forwarding of so-called *packets* between networks; *IP* belongs to this layer
4. *transport layer*; this layer ensures reliable transfer of so-called *segments* between host/application pairs; *UDP* and *TCP* belong to this layer
5. *session layer*; this layer manages *sessions* in network communication between applications
6. *presentation layer*; this layer translates, encrypts and compresses data for transmission between applications and the network; *TLS/SSL* belongs to this layer
7. *application layer*; this layer offers high-level *network protocols* for applications communicating over a network; e.g. `http`, `tfp`, `smtp`

Linux commands that are used extensively w.r.t. networking are:
* the `ip` command, which replaces commands like `ifconfig`, `route` and `netstat`
* `wireshark`; be **extremely careful** not to use this tool without permission outside our local home network!

As for troubleshooting networking issues, *each OSI layer depends on lower layers* to work properly. So this
layering helps pinpoint problems. For example, we cannot expect any IP packets to arrive at their destination
if communication in the data link layer (within a Wi-Fi/Ethernet local network) is not working properly.

As an alternative to the OSI model, there is also the (older) simpler *TCP/IP* model. It has 4 layers,
from bottom to top:
1. *network access layer*, combining *OSI physical and data link layers*
2. *internet layer*, similar to the *OSI network layer*; this layer is about exchange of *IP packets*
3. *transport layer*, similar to the *OSI transport layer*; this layer is about exchange of *TCP or UDP segments*
4. *application layer*, combining *OSI session, presentation and application layers*

Note that the OSI model is more generic than the TCP/IP model. The latter is based on specific standard
protocols. On the other hand, despite the fact that this section uses the generic OSI model as guideline,
the specifics fit in the TCP/IP model as well, since these protocols are more or less a de-facto standard.

#### 3.12.2. Physical layer

The *physical layer* handles transmission of raw data *bits* over physical media, such as:
* (copper or fiber-optic) cables
* wireless

Data transmission occurs at this layer by converting between digital data (bits) and signals.
Signaling involves voltage levels, modulation, synchronisation etc. Error detection might be done
using parity bits.

In summary, the physical layer provides a *physical connection, through which we can send bits*.

*Ethernet splitters* are devices operating in the physical layer. See
[Ethernet splitter versus switch](https://ascentoptics.com/blog/ethernet-splitter-vs-switch-understanding-the-key-differences/).

The versatile `ip` Linux command can also be used to disable (and again enable) network devices.

With `ip addr show` we get a lot of information (concerning multiple layers), including the name of our
network device. Disabling/enabling the device:
* disabling: e.g. `ip link set dev enp0s5 down` (for network device `enp0s5`)
  * be careful: it drops the connection if you are using this network device to connect to the remote machine
* enabling: e.g. `ip link set dev enp0s5 up` (for network device `enp0s5`)

#### 3.12.3. Data Link layer

The *data link layer* builds on the foundation of the physical layer underneath it. The data link layer
has the following characteristics:
* in this layer data is sent between nodes in the *same local network* (e.g. Wi-Fi/ethernet network)
  * think of nodes as *devices with a NIC* (*network interface controller*)
  * i.e. nodes are pieces of hardware that allow a computer to connect to a network
  * examples are: Ethernet and Wi-Fi adapters in computers, phone Wi-Fi adapters, networked printers etc.
* each node in such a network has a *globally unique MAC address* (*Media Access Control* address)
  * again, think of nodes as *devices with a NIC* (*network interface controller*)
  * MAC addresses are 6 bytes; e.g. (think bytes as pairs of hex numbers) `ac:19:8e:e0:6b:9a`
  * the first 3 bytes are the *OUI* (*Organizationally Unique Identifier*)
  * the last 3 bytes are device-specific
  * MAC addresses are static by default but can be spoofed (i.e. changed) in software
* the data sent between nodes has the form of *Ethernet or Wi-Fi* *frames* (as groups of *bits*, typically not exceeding approximately 1500 8-bit bytes)
  * Ethernet and Wi-Fi frames are quite similar; Ethernet (IEEE 802.3) and Wi-FI (IEEE 802.11) are highly interoperable
  * each frame has a *source MAC address* (i.e. MAC address of source network interface)
  * each frame has a *destination MAC address* (i.e. MAC address of destination network interface)
  * each frame has a *data payload*
  * typically, the payload is an *IP packet* (so typically IP packets are wrapped by Ethernet/Wi-Fi frames)

The data link layer has 2 sub-layers. They are, from bottom to top:
1. *Media Access Control* (*MAC*); see above; they define sender and receiver of a frame
2. *Logical Link Control* (*LLC*); the interface between data link and network layer, providing flow control and error detection

Again, command `ip addr show` shows our network interfaces. In the context of the data link layer,
this command shows the network interfaces and their MAC addresses etc.

Typical hardware operating at this layer includes:
* bridge
  * it makes multiple connected LANs logically appear as one LAN
  * see e.g. [network bridge](https://en.wikipedia.org/wiki/Network_bridge)
* Ethernet switch (although some switches also operate at the network layer)
  * a switch reduces unnecessary traffic of frames
  * without a switch, frames would reach all nodes in the network, with typically all but 1 of those nodes accepting the frame
  * a switch can intervene, and forward a frame only to the destination node of the frame
  * see e.g. [Ethernet splitter versus switch](https://ascentoptics.com/blog/ethernet-splitter-vs-switch-understanding-the-key-differences/)
* Wireless Access Point
  * device that enables wireless nodes to connect to a wired network
  * see e.g. [what is a WAP](https://www.cisco.com/site/us/en/learn/topics/small-business/what-is-an-access-point.html)

How can we find the other devices in a local network (Ethernet and/or Wi-Fi)? Some commands that can be
used in this regard are:
* `ip neighbor` (or `ip neighbor show`)
  * it shows the current neighbor table, i.e. *ARP table*
  * *ARP*, or *Address Resolution Protocol* is the protocol used to map IP addresses to MAC addresses within a LAN
* `sudo nmap -sn 192.168.1.0/24` (for local network `192.168.1.0/24`)
  * option `-sn` does a ping scan, to discover hosts, and does not do any port scan
  * `nmap` can use other protocols than just ARP alone
  * when using `nmap` in general, like when using `wireshark`, be **extremely careful** not to use this tool without permission outside our local home network!
* `avahi-browse -a -r` (discussed later)
* etc.

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
