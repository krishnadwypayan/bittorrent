# Java BitTorrent Client

This project is a work-in-progress implementation of a BitTorrent client built using Java and the [Micronaut Framework](https://micronaut.io/). The goal is to create a modern, efficient, and modular client by leveraging Micronaut's features like dependency injection, AOT compilation, and a reactive programming model.

## Core Components

The project is being built module by module, with a focus on creating a clean, testable, and robust implementation of the BitTorrent protocol. The core components include:

1.  **Bencode Encoder/Decoder:** A library for serializing and deserializing data according to the Bencode specification.
2.  **Metainfo Parser:** Logic to parse and interpret `.torrent` files.
3.  **Tracker Communication:** An HTTP client to communicate with BitTorrent trackers to get peer lists.
4.  **Peer Wire Protocol:** The core networking logic for communicating with other peers to download and upload file pieces.

---

## Bencode

Bencoding is a way to specify and organize data in a terse format. It supports the following types: byte strings, integers, lists, and dictionaries.

## Resources
1. BitTorrent unofficial [spec](https://wiki.theory.org/BitTorrentSpecification)
2. Awesome [blog](https://blog.jse.li/posts/torrent/)
3. Another awesome [blog](http://www.kristenwidman.com/blog/33/how-to-write-a-bittorrent-client-part-1/)
