package com.krishna.kota.model;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Represents the metadata of a torrent file, commonly known as a .torrent file.
 * This is an immutable data carrier for all the information contained within the metainfo file.
 *
 * @param announce The URL of the tracker.
 * @param announceList An optional list of tracker URLs.
 * @param comment An optional comment by the author.
 * @param creationDate The optional creation date of the torrent.
 * @param createdBy The optional name and version of the program used to create the torrent.
 * @param encoding The optional string encoding format used for the pieces.
 * @param info A dictionary that contains information about the file(s) of the torrent.
 */
public record TorrentFileMetaInfo(
        String announce,
        Optional<List<String>> announceList,
        Optional<String> comment,
        Optional<Instant> creationDate,
        Optional<String> createdBy,
        Optional<String> encoding,
        InfoDictionary info
) {

    public record InfoDictionary(
            String name,
            long pieceLength,
            byte[][] pieces,
            Optional<Long> length,
            Optional<List<FileInfo>> files
    ) {
        public boolean isMultiFile() {
            return files.isPresent();
        }
    }

    public record FileInfo(
            long length,
            List<String> path
    ) {}

}
