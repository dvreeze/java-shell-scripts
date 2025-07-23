
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
  * *ARP*, or *Address Resolution Protocol* is the protocol used to map (local) IP addresses to MAC addresses within a LAN
* `sudo nmap -sn 192.168.1.0/24` (for local network `192.168.1.0/24`)
  * option `-sn` does a ping scan, to discover hosts, and does not do any port scan
  * `nmap` can use other protocols than just ARP alone
  * when using `nmap` in general, like when using `wireshark`, be **extremely careful** not to use this tool without permission outside our local home network!
* `avahi-browse -a -r` (discussed later)
* `sudo arp-scan --interface=wlp0s20f3 --localnet` (for network interface `wlp0s20f3`)
* etc.

The *OUI* (i.e. first 3 bytes) of a MAC address can also help make sense of what device has which MAC
address in a LAN. To that end, many OUIs can be looked up in [OUI lookup](https://www.wireshark.org/tools/oui-lookup.html).

Note that *ARP* is *technically* a data link layer protocol that *assists* the network layer.
Technically it is a data link layer protocol, because ARP data is the payload of data link layer frames,
and, unlike IP packets, these frame payloads do not leave the LAN (and are not "routable"). Yet ARP does
assist the network layer by mapping (local) IP addresses to MAC addresses (that's why ARP is sometimes called
a "layer 2.5" protocol).

ARP helps reduce unnecessary traffic within the LAN, because network devices in the LAN cache ARP
data, thus preventing continuous ARP traffic in the LAN to find out the destination MAC address, given
an IP address (typically of the router).

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
supported as IPv4 (well, at least not from the perspective of end users). That said, technically IPv6
is quite robust, and requires far fewer "workarounds" than IPv4. A separate subsection of this section
briefly introduces IPv6, although clearly IPv6, like IPv4, belongs to the network layer that is discussed
here.

A typical workaround for the low number of IPv4 addresses is to use publicly available IPv4 addresses
on the internet, and reserved addresses such as the ones in address range `192.168.0.0/16` inside the
LAN. Let's consider typical network layer communication in a home network, and starting from a device
in that home network. The router or gateway in the LAN has both an `192.168.0.0/16` address as well as
an IPv4 address "publicly known on the internet".

There are 2 scenarios to be acquainted with in this context:
* the destination is in the same LAN
  * source and destination typically have IPv4 addresses in the same reserved address range, such as `192.168.1.0/24`
* the destination is outside the LAN, and somewhere else on the internet

If the *destination is in the same LAN*:
1. consulting the *routing table* (see below), the destination IP address is found in the same LAN
2. this destination IP address maps to a MAC address (in the same LAN, of course) by consulting the ARP cache
3. a data link frame containing the IP packet is created and sent to the destination MAC address
4. and the IP packet (payload of the frame) is extracted from the frame

In a request/response scenario (this mainly depends on the application layer in the OSI model), the same
happens for the response, but switching source and destination address.

If the *destination is outside the LAN*, and somewhere else on the internet:
1. consulting the *routing table* (see below), the router/gateway IP address is found for the "first hop"
2. this router/gateway IP address maps to a MAC address (in the same LAN, of course) by consulting the ARP cache
3. a data link frame containing the IP packet is created and sent to the router's MAC address
4. the router extracts the IP packet from the frame, and performs *NAT* (*Network Address Translation*), setting the source IP to its own "internet IP address"
5. the router forwards this updated IP packet (wrapped in some WAN technology frame) to the next router, and so on, until the destination IP address is reached

Typically, the hardware device that acts as our router also acts as a modem (so this device is a "modem-router"):
* the *modem* functionality turns Ethernet/Wi-Fi frames into "WAN technology frames", and vice versa
  * so the modem functionality is *data link layer* functionality
* the *router* functionality forwards IP packets, from nodes in our local network to the internet and vice versa
  * that is, unless the IP packet is meant for another node within our LAN
  * so the router functionality is *network layer* functionality

In any case, in a home network setup we obviously need both (modem and router) functionalities, whether
provided as one hardware device or 2 separate ones.

In a request/response scenario (this mainly depends on the application layer in the OSI model), the reverse
happens for the response, switching source and destination address. Note that for the response IP packet,
the destination address is the "internet IP address" of our router. This router then rewrites the destination
IP address to the originator of the request (so the router must keep track of NAT-related rewritings),
and sends the adapted response IP packet, wrapped in a frame, to the originator of this request/response
interaction.

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

The *transport layer* builds on the foundation of the network layer underneath it. The transport layer
has the following characteristics:
* in this layer data is sent between *applications* running on *hosts* across networks
  * more precisely, data is sent from a *pair of source IP address and source port* to a *pair of destination IP address and destination port*
  * a sending application (on the source host) should *send* data from the (typically randomly assigned) *source port*
  * a receiving application (on the destination host) should *listen* for data received on the (typically standardized) *destination port*
* the transport layer offers the following functionality (partly depending on the kinds of "segments" sent):
  * multiple connections between remote hosts, and dedicated connections for applications communicating across networks
  * handling out-of-order packets, and retransmissions if data is lost
  * flow control (what the receiver can handle) and congestion control (what the connection can handle)
* the data sent is either a *UDP segment* or a *TCP segment*
  * the UDP or TCP segment is the *payload of an IP packet*, so the UDP/TCP segment is wrapped in an IP packet
  * UDP and TCP segments contain a *source port* and *destination port*, besides other metadata and the payload
  * the *source IP address* and *destination IP address* are of course found in the IP packet that encapsulates the UDP/TCP segment
* *TCP* offers the above-mentioned functionality (out-of-order handling, retransmissions, flow/congestion control)
  * so the application does not have to deal with that
  * TCP can therefore be considered a *connection-oriented* protocol
  * and TCP traffic can conceptually be understood in terms of (correctly sorted and complete) input and output byte streams, from the perspective of the application
  * in Java programs, TCP-level communication can be achieved with `java.net.Socket` (client-side) and `java.net.ServerSocket` (server-side)
* *UDP* does not offer all the above-mentioned functionality
  * out-of-order handling and retransmissions are left to the application, and not solved by UDP
  * UDP can be considered a *connection-less* protocol, where each UDP segment is sent independently of other UDP segments
  * UDP clearly has its use cases; e.g. video calls
  * in Java programs, UDP-level communication can be achieved with `java.net.DatagramSocket` (client-side and server-side)

UDP and TCP *ports* are 16-bit (or 2-byte) numbers ranging from 0 to 65535. Note that 65535 is one less than 2 to the
power 16, which makes sense. UDP/TCP ports are written in decimal notation.

The following types of *TCP ports* exist (that server-side applications listen on):
* *well-known* ports, ranging from 0 to 1023
  * these TCP ports are reserved for standard services and protocols
  * e.g. HTTP (80), HTTPS (443), FTP (20, 21), SSH (22), Telnet (23), SMTP (25), IMAP (143), POP3 (110)
  * FTP uses port 20 for actual data transfer, and port 21 for the control connection
* *registered* ports, ranging from 1024 to 49151
  * they are assigned to specific applications by *IANA* (Internet Assigned Numbers Authority)
  * e.g. MySQL (3306), PostgreSQL (5432), VNC (5900)
* *dynamic or private* ports, ranging from 49152 to 65535
  * they are not controlled by IANA, and available for any application to use on an as-needed basis
  * note that 49152 equals `3 * (2 ** 14)`; so 3 times 2 to the power of 14

In TCP traffic, the *source port* is randomly assigned from the dynamic/private port range, and the
*destination port* is typically a well-known or registered port.

Note that *TCP ports* differentiate between multiple connections on a single device. More precisely, a unique
combination of source IP address, source port, destination IP address and destination port differentiates
between multiple TCP connections between the same 2 devices, allowing for multiple TCP connections to
coexist without any conflicts.

The most commonly used *UDP ports* are:
* DNS (53); this is the Domain Name System
* DHCP (67 server-side, 68 client-side); this is the Dynamic Host Configuration Protocol
* SNMP (161, 162); this is the Simple Network Management Protocol
* TFTP (69); this the Trivial File Transfer Protocol
* NTP (123); this is the Network Time Protocol
* RTP (5004, 5005); this is the Real-time Transport Protocol, used for audio/video streaming

TCP connections start with a *3-way TCP handshake*:
* goal: both computers need to know that the other side responds
* and both computers will later need to know how much data has already been received by the other side
* so, our "sequence numbers" need to be exchanged

The TCP handshake works as follows:
1. our computer sends a `SYN` packet to the other side
2. the other side sends a `SYN-ACK` packet back to our computer
3. our computer replies with an `ACK` packet; the connection has been successfully established

For more details, see for example [TCP 3-way handshaking](https://wiki.wireshark.org/TCP_3_way_handshaking).

We have seen tool `nmap` before, but that was its use for discovering neighbor nodes in the LAN.
Foremost, `nmap` is a *port scanner* (therefore operating at the transport OSI layer), which is something
*we are not supposed to do without permission*. Port scanning on our own network should be ok, though.

The idea is that `nmap` tries to connect through all possible ports. If the port is open, the server will
send back a message. This allows us to identify open ports and available service on the target system.

Example usage of `nmap`:
* scanning a specific host: e.g. `nmap 192.168.1.254` or `nmap a-host-name`
  * this will scan the most common 1000 TCP ports
* scanning specific ports on a specific host:
  * e.g. `nmap -p 21 192.168.1.254`
  * e.g. `nmap -p 20,21 192.168.1.254`
  * e.g. `nmap -p 0-1024 192.168.1.254`
* scanning all ports on a specific host: e.g. `nmap -p - 192.168.1.254`
* scanning a range of IP addresses: e.g. `nmap 192.168.1.1-100`

We can use different *kinds of scans*:
* `-sS` (TCP-SYN-Scan):
  * the default scan type, if available
  * it is relatively fast
  * it sends a `SYN` packet to establish a TCP connection, but does not follow through with the full connection
  * it might require root privileges
  * if we receive a `SYN-ACK`, the port is open
  * if we receive an `RST` (reset) message, the port is closed
  * if we receive no message, the port is probably blocked or filtered
* `-sT` (TCP-Connect-Scan):
  * it is only the default if a TCP-Syn-Scan is not possible (possibly when we are trying to scan an IPv6 network)
  * in this case `nmap` uses OS functionality to create a connection
  * it does the full 3-way handshake with the remote server, closing the connection afterward
  * yet it is slow, may cause crashes on the other side, and might cause logs at the other side
* `-sU` (UDP-Scan):
  * this scans for open UDP ports
  * the idea for UDP scanning: send a UDP packet to the other side
  * and if we get a reply, the port will be marked as "open"
  * yet if we get back an error, the port will be marked as "filtered"
  * and if we don't get any reply, the port will be marked as "open|filtered"
  * UDP port scanning is extremely slow, and we may have to test ports multiple times as our request may have got lost

We briefly talked about *NAT* (*Network Translation Protocol*) before. Indeed, NAT is mainly a network
layer feature. Yet NAT can also "rewrite" ports, and not just IP addresses, so in that case NAT becomes
a transport layer feature.

This port forwarding functionality may be needed if we have a service running within the LAN, listening
on, say, port 8080, and we want to make this service available to the internet, via port 58080 for example.
Then we need to tell our router to forward port 58080 to port 8080 on the node in the LAN running the service.
It would then also be needed to reserve the IP-to-MAC-address combination for the host running the
service, thus preventing DHCP in the router from assigning this IP address. If home (internet) IP addresses
change, we may need something like "dyndns" to make this IP+port forwarding work.

#### 3.12.6. Session layer

The *session layer* builds on top of the foundation of the transport layer underneath it. In the session layer
and the higher layers (presentation layer and application layer) we can think in terms of *streams of
bytes* or "client-server dialogs" (high level protocols) involving byte streams.

The *session layer* has the following characteristics:
* like the higher layers, the session layer is often completely handled by the application
  * this makes the distinction between session layer, presentation layer and application layer less clear
* the session layer establishes, maintains and terminates *connections*, and supports communication between (remote) applications
* examples of session layer protocols are:
  * *NFS* (*Network File System*), to access remote files on a network (built on an RPC foundation)
  * *RPC* protocols (*Remote Procedure Calls*), calling *remote functions* as if they were local functions
  * *SCP* protocols (*Session Control Protocol*), managing sessions between devices
* RPC-like protocols are well-known territory for many programmers, e.g. for Java programmers
  * these protocols involve serialization/deserialization of data, data format versioning strategies etc.
  * so, clearly these protocols are at a higher abstraction level than segments, packets and frames

#### 3.12.7. Presentation layer

The *presentation layer* builds on the foundation of the session layer underneath it. It is about
*representation of data*, to ensure data compatibility and security. Functionality in this layer
includes:
* data conversion, such as character encoding conversions (EBCDIC, UTF-8 etc.)
* securing data transmission; e.g. SSL/TLS
* data compression (deflate, brotli compression etc.)

Example protocols in the presentation layer are:
* *SSL* / *TLS* (Secure Sockets Layer, Transport Layer Security)
  * provides secure communication over a network
  * originally designed for securing HTTP communication
  * we could also consider this as operating both at levels 6 (presentation) and 7 (application) in the OSI model
* *MIME* (Multipurpose Internet Mail Extensions)
  * defines how e-mails support extensions, through so-called *MIME types* (or *content types*)
  * but also widely used outside mail functionality, e.g. in application layer protocols like HTTP)
  * character encodings are also part of MIME

#### 3.12.8. Application layer

At the highest level in the OSI model resides the *application layer*. It contains the *protocols that
applications can use*. For example:
* *HTTP* / *HTTPS* (for web traffic)
* *IMAP* (for accessing e-mails on a remote server)
* *SSH* (to access a remote shell, do a "remote copy" through `scp`, etc.)
* *POP3* (for downloading e-mails)
* proprietary protocols, such as custom VOIP implementations

The *HTTP* protocol is very familiar to programmers. A good explanation of HTTP can be found in
[MDN page on HTTP](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview).

We could even "speak" HTTP (client-side) using command `telnet`, passing host (IP or host name) and
port (typically 80 for HTTP) as parameters. The `telnet` command could also be used to just check
a TCP connection, regardless of the application level protocol.

Of course, "speaking" HTTP (client-side) is much more practical with commands like `curl` or `wget`.

If a connection uses SSL/TLS, we cannot use `telnet`. In that case we could use command `openssl`,
which handles the SSL/TLS handshake and encryption for us, provided we have set up the needed certificates.
For `openssl s_client -connect` command examples, see
[testing SSL connectivity with openssl](https://docs.pingidentity.com/solution-guides/standards_and_protocols_use_cases/htg_use_openssl_to_test_ssl_connectivity.html).
For `openssl` command "cryptography" examples, see [openssl examples](https://www.baeldung.com/linux/openssl-command-examples).

#### 3.12.9. Domain Name Service (DNS)

The *DNS* (*Domain Name Service*) protocol translates domain names to IP addresses. This facilitates
human-readable access to websites and services. In particular, the URL we type in the address bar
of the browser can contain a host name (like `google.com`) instead of an IP address, and the computer
does the rest, using DNS under the hood.

How does the browser know about the IP address of the domain in the URL? There are 2 steps involved
(in the context of internet access from a home computer):
1. finding the IP address of the domain in a cache
2. if not found, the *DNS resolver* looks up the IP address

Step 1 in more detail:
1. the *browser* consults its *local cache* that maps domain names to IP addresses
2. if not found, the *OS* consults its local cache of domain names mapping to IP addresses
3. if found, we are done; if not, proceed with step 2

Step 2 in more detail (in typical scenarios):
1. the *DNS resolver* of the *ISP* (Internet Service Provider) checks its *resolver cache*
2. if not found, the ISP reaches out to one of 13 *root nameservers* (labelled with letters from A to M)
3. the root nameserver responds with the location (domain and IP address) of a *TLD nameserver* (*Top Level Domain* nameserver)
4. the TLD nameserver is consulted
5. the TLD nameserver typically responds with the location (domain and IP address) of the *authoritative nameserver* for the domain
6. the authoritative nameserver for the domain is consulted
7. the authoritative nameserver responds with the IP address of the domain
8. so, now our ISP's DNS resolver has the IP address of the domain, and sends it to our OS
9. the OS gives this IP address to the browser
10. and the browser now finally sends the request to the IP address corresponding to the domain

DNS distinguishes between different *DNS record types* for DNS records:
* `A` maps a domain name to an IPv4 address
* `AAAA` maps a domain name to an IPv6 address
* `CNAME` provides an alias for another domain name
* `MX` specifies mail servers for a domain
* `NS` lists authoritative nameservers for a domain

Received DNS entries can be listed with the `host` command. For example:
* `host google.com` (returns IPv4 address, IPv6 address, and mail server domain name)
* `host -v google.com` (returns the same, but verbosely, listing entire DNS entries)
* `host -v -t AAAA google.com` (listing entire DNS entries for record type `AAAA`, so for IPv6 addresses)
* `host -v -t ANY google.com`, or, equivalently: `host -a google.com`
* `host 10.0.2.15` (reverse lookup, from IP address to domain name; not as reliable as domain-to-IP lookups)
* `host -t NS google.com` (returns authoritative nameservers for the domain)

To aid our understanding of DNS, we could manually send out DNS queries using the `dig` command.
Sample (manual) `dig` session:
1. pick a *root nameserver*: `dig @a.root-servers.net com NS`
2. query a *TLD nameserver*: `dig @f.gtld-servers.net google.com NS`
3. query an *authoritative nameserver*: `dig @ns2.google.com google.com ANY`

DNS can not be considered very secure nowadays. For example:
* *DNS spoofing*: an attacker redirects traffic by altering DNS records
* *cache poisoning*: inserting malicious DNS entries into a DNS resolver cache
* *man-in-the-middle-attacks*: DNS queries are intercepted by an attacker, and false responses are returned

To an extent, this can be mitigated by transferring DNS query results via HTTPS, and refusing invalid
certificates. We could also use *DNSSEC* (DNS Security Extensions), which is not treated in this course.

An IP address (with corresponding domain) can also be manually defined in file `/etc/hosts`.

Sometimes the local DNS server must be refreshed. Yet do we use service `systemd-resolved` or do we
use `dnsmasq`? With commands like `systemctl status` and `pgrep` we can find the answer to that
question. Alternatively, we could enter the following command to find the answer:

```bash
# lsof ("list open files") for port 53, which is the UDP/TCP port for DNS
sudo lsof -i :53
```

If we use `systemd-resolved`, the following DNS-related `resolvectl` commands can be used:
* flushing the caches: `sudo resolvectl flush-caches`
* querying the "DNS status": `sudo resolvectl status`
* getting "DNS statistics": `suod resolvectl statistics`

If `dnsmasq` is used, just restart the service:
* `sudo systemctl restart dnsmasq`

*Within our local network*, the host name can be used to easily access other computers (in the same LAN).
It is also used during DHCP negotiation. The host name can be found with command `hostname`.

To change the host name, the following steps are needed:
1. edit file `/etc/hostname`
2. also edit file `/etc/hosts` to keep it consistent with the host name change in `/etc/hostname`
3. reboot

Suppose another node within our LAN has host name `ubuntu`. Then we can access it with the
suffix `.local`. For example:
* `ping ubuntu.local`

What is this suffix `.local` about? This is called *mDNS* (*Multicast DNS Standard*):
* it is quite different from normal DNS
* the idea is to designate a special mDNS domain `.local`, just for local networks
* then an mDNS query is sent to our entire local network, and the target computer will respond with its IP address
* but it requires both computers to be configured correctly (details can be found on the internet)
* on Linux, the `avahi` daemon process must be configured and running
* on CentOS, `nss-mdns` must be installed, followed by a reboot

#### 3.12.10. IPv6 and the network layer

An opinionated article about IPv6 can be found
[here](https://www.sidn.nl/en/modern-internet-standards/ipv6). It has a good technical explanation of
IPv6 too.
