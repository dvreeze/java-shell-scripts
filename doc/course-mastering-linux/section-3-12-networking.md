
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

The *network layer* builds on the foundation of the data link layer underneath it. Let's first start with
some theory. The data sent over the wire in this network layer are *IP packets*, i.e. *internet protocol
packets*.

*IP packets* in the ubiquitous IPv4 standard (see [IPv4](https://en.wikipedia.org/wiki/IPv4)) use *addresses*
for source and destination that consist of 4 bytes, i.e. 32 bits. These IPv4 addresses are typically not
written in binary, octal or hexadecimal notation, but in decimal notation. For example, `192.168.1.2` is
an IPv4 address.

Which part of an IPv4 address identifies a network, and which part identifies a device in that network?
For that we use *subnet masks*, consisting of multiple "1-bits" followed by 0 or more "0-bits". For example:
* the subnet mask for an address such as `192.168.1.2` is typically `255.255.255.0` (also in decimal notation)
* we get the network by the "logical AND" of the IPv4 address and the subnet mask
* above, the IP address in binary form is: `11000000.10101000.00000001.00000010`
* and the subnet mask is: `11111111.11111111.11111111.00000000`
* so the "network" (as the "logical AND" of IP address and subnet mask) is: `11000000.10101000.00000001.00000000`
* given this IP address and subnet mask, clearly address `192.168.1.212` belongs to the same network
* yet address `172.16.4.12` clearly does not belong to the same network

We can represent a subnet mask by the number of (leading) binary "ones", e.g. 24 in the example above.
The IPv4 address with subnet mask then becomes the following address:
`192.168.1.2/24`, which is IP address `192.168.1.2` in a network with subnet mask `255.255.255.0`.
Note that this network is `192.168.1.0/24`, in so-called *CIDR notation*.

Some IPv4 address ranges are "private", so they are not globally unique and cannot be used for publicly
known IP addresses on the internet. They can be used for private networks, though. Well-known such
reserved address ranges include:
* `10.0.0.0/8`
* `172.16.0.0/12`
* `192.168.0.0/16`

Let's now describe the *network layer*:
* the network layer supports communication with devices in *other networks*, and not just within one local area network (using Ethernet/Wi-Fi)
* the data in this layer is sent in *IP packets* (typically IPv4 packets)
* the nodes in network layer communication have an *IP address* (see above for IPv4 addresses)
  * it would be nice if these addresses were globally unique, but the situation is more complicated (see below)
* the network layer builds on top of the foundation of the data link layer (for communication within a LAN)
  * specifically, the payload of data link layer Ethernet/Wi-Fi frames is typically IP packets
  * so IP packets are typically wrapped in Ethernet/Wi-Fi frames within a local area network
* each *IP packet* has:
  * a *source IP address*
  * a *destination IP address* (typically outside the current local area network)
  * a *payload*, which is typically a *UDP or TCP segment* belonging to the layer above the network layer

Note that with IPv4 addresses consisting of only 4 bytes, the number of supported IPv4 addresses on the
internet is quite low (less than 2 to the power of 32, so less than 4 GiB unique addresses).
A solution is the use of IPv6, which offers a far larger address space, but IPv6 is still not as widely
supported as IPv4.

A typical workaround for the low number of IPv4 addresses is to use publicly available IPv4 addresses
on the internet, and reserved addresses such as the ones in address range `192.168.0.0/16` inside the
LAN. Let's consider typical network layer communication in a home network, and starting from a device
in that home network. The router or gateway in the LAN has both an `192.168.0.0/16` address as well as
in IPv4 address "publicly known on the internet".

There are 2 scenarios to be acquainted with in this context:
* the destination is in the same LAN
  * the data link layer makes sure the device with the destination MAC address can be found
  * at the network layer, source and destination also belong to the same network
  * i.e., source and destination typically have IPv4 addresses in the same reserved address range, such as `192.168.1.0/24`
  * in this case, the frame (containing the IP packet) stays within the LAN, and the router/gateway knows that the IP packet must not leave the LAN
* the destination is outside the LAN, and somewhere else on the internet
  * the data link layer makes sure the frame containing the IP packet reaches the router/gateway
  * the router/gateway knows that the IP packet in the frame must leave the LAN, and find its way to other routers until the destination is reached
  * the router/gateway must perform *NAT* (*network address translation*), by rewriting request and response IP packet IP addresses
  * after all, the reserved IP addresses used inside the LAN are not known on the internet
  * so, to make this work, the router/gateway must keep track of IP packet rewritings in order to do the reverse rewriting on response IP packets

But how does the router/gateway know about how to route IP packets? It has this knowledge from a so-called
*routing table* (see below).

To show our machine's network interfaces with corresponding IP addresses, see command `ip addr show` again.
So this command not only shows the network interfaces at the data link layer (with their MAC addresses),
but also the assigned IP addresses (and subnet masks).

To show the *routing table*, use command `ip route show`. The output contains lines that convey the
following information: network X (say, `192.168.1.0/24`) can be reached via network interface Y
(say, `wlp0s20f3`) from own device IP address Z (say, `192.168.1.104`). There is also a line for
the "default" network, so any network not mentioned in the other lines (typically for internet access).

Adding/removing an IP address to a network interface:
* adding: e.g. `sudo ip addr add 192.168.1.10/24 dev enp0s5` (where `enp0s5` is a network interface)
* removing: e.g. `sudo ip addr del 192.168.1.10/24 dev enp0s5`

Managing the *routing table*:
* querying for one address: e.g. `ip route get 8.8.8.8` (consults the routing table, but does not guarantee the destination can be reached)
* adding a route: e.g. `sudo ip route add 10.0.0.0/24 via 192.168.1.1 dev enp0s5` (where `192.168.1.1` is the gateway)
* deleting a route: e.g. `sudo ip route del 10.0.0.0/24 via 192.168.1.1 dev enp0s5`

How does a client get an IP address? Especially in home networks, this is typically achieved through
*DHCP* (*Dynamic Host Configuration Protocol*). DHCP is a protocol on top of transport layer protocol
UDP, but conceptually it "belongs" to the network layer, because it helps manage the network layer.

DHCP consists of:
* the DHCP server
  * it stores an IP address pool, manages IP address leases, and assigns/reclaims addresses
  * in a home network, the router is typically the DHCP server (for private IP addresses in the LAN)
* the DHCP client
  * it requests an IP address and configuration
  * it renews or releases IP address leases
* possibly also a DHCP relay agent (forwarding requests between subnets)

The DHCP protocol works as follows:
1. *Discover*: the client broadcasts a DHCP discover message, which is handled by the DHCP server (and ignored by other nodes)
2. *Offer*: the DHCP server responds, offering an IP address and lease information
3. *Request*: the client sends a request message, accepting the IP address and lease terms
4. *Acknowledge*: the server acknowledges the IP address lease to the client

DHCP can be inspected. First check with `systemctl status` whether `systemd-networkd` or `NetworkManager`
is used. Then DHCP-related issues can be inspected either with `journalctl -u systemd-networkd` or
`journalctl -u NetworkManager`, depending on the stack used.

The well-known `ping` tool can be used to check reachability of nodes in the network layer.
E.g. `ping -c 5 8.8.8.8`. It uses the *ICMP* protocol (*Internet Control Message Protocol*).
ICMP can be considered a network layer protocol.
See [ICMP](https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol).

Command `traceroute` can be used to find out the path taken by IP packets to the destination.
It shows where latency occurs and exposes potential routing issues. Under the hood, `traceroute`
starts out by sending an IP packet with TTL 1. Our router will decrement the TTL by 1. It therefore
becomes 0, so our router will discard the package, and reply with ICMP "Time Exceeded" message. So we
have the address of the router (the first hop). Next an IP package with TTL 2 is sent. This will reach
the next router after our router, which will reply with the ICMP "Time Exceeded" message, so which will
reveal the IP address of that next router after our router. And so on, until the destination is found,
if it is reached by `traceroute`.

#### 3.12.5. Transport layer

TODO

#### 3.12.6. Session layer

TODO

#### 3.12.7. Presentation layer

TODO

#### 3.12.8. Application layer

TODO
