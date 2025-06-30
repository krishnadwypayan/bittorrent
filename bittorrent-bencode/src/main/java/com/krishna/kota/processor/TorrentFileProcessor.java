package com.krishna.kota.processor;

import com.krishna.kota.bencode.decode.IBdecoder;
import com.krishna.kota.bencode.type.*;
import com.krishna.kota.model.TorrentFileMetaInfo;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.krishna.kota.model.TorrentFileConstants.*;

@Singleton
public class TorrentFileProcessor {

    private final IBdecoder bdecoder;

    @Inject
    public TorrentFileProcessor(IBdecoder bdecoder) {
        this.bdecoder = bdecoder;
    }

    public TorrentFileMetaInfo decodeTorrentFile(final String path) {
        try (InputStream is = Files.newInputStream(Path.of(path))) {

            // .torrent file is decoded into BencodeMap
            BencodeElement metaInfoDictionary = bdecoder.decode(is);
            if (!(metaInfoDictionary instanceof BencodeMap)) {
                throw new RuntimeException("Invalid .torrent file");
            }

            // BencodeMap is converted to TorrentFileMetaInfo
            return fromBencodeMap((BencodeMap) metaInfoDictionary);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TorrentFileMetaInfo fromBencodeMap(final BencodeMap bencodeMap) {

        // announce url
        String announceUrl = null;
        BencodeElement announceBencodeElement = bencodeMap.get(ANNOUNCE_BENC_STRING);
        if (announceBencodeElement instanceof BencodeString) {
            announceUrl = ((BencodeString) announceBencodeElement).asString(StandardCharsets.UTF_8);
        }

        // announce url: list
        List<String> announceUrlList = null;
        BencodeElement announceListBencodeElement = bencodeMap.get(ANNOUNCE_LIST_BENC_STRING);
        if (announceListBencodeElement instanceof BencodeList) {
            announceUrlList = ((BencodeList) announceListBencodeElement).stream()
                    .filter(element -> element instanceof BencodeString)
                    .map(element -> ((BencodeString) element).asString(StandardCharsets.UTF_8))
                    .toList();
        }

        // comment: string
        String comment = null;
        BencodeElement commentBencodeElement = bencodeMap.get(COMMENT_BENC_STRING);
        if (commentBencodeElement instanceof BencodeString) {
            comment = ((BencodeString) commentBencodeElement).asString(StandardCharsets.UTF_8);
        }

        // creation date: integer
        Instant creationDate = null;
        BencodeElement creationDateBencodeElement = bencodeMap.get(CREATION_DATE_BENC_STRING);
        if (creationDateBencodeElement instanceof BencodeInteger) {
            creationDate = Instant.ofEpochSecond(((BencodeInteger) creationDateBencodeElement).longValue());
        }

        // created by: string
        String createdBy = null;
        BencodeElement createdByBencodeElement = bencodeMap.get(CREATED_BY_BENC_STRING);
        if (createdByBencodeElement instanceof BencodeString) {
            createdBy = ((BencodeString) createdByBencodeElement).asString(StandardCharsets.UTF_8);
        }

        // encoding: string
        String encoding = null;
        BencodeElement encodingBencodeElement = bencodeMap.get(ENCODING_BENC_STRING);
        if (encodingBencodeElement instanceof BencodeString) {
            encoding = ((BencodeString) encodingBencodeElement).asString(StandardCharsets.UTF_8);
        }

        // info: map
        BencodeMap infoMap = getAndMap(bencodeMap, INFO_BENC_STRING, BencodeMap.class, Function.identity()).orElse(null);
        if (infoMap == null) {
            throw new RuntimeException("Invalid .torrent file. 'info' not found");
        }

        String name = getAndMap(infoMap, NAME_BENC_STRING, BencodeString.class, v -> v.asString(StandardCharsets.UTF_8))
                .orElse(null);

        long pieceLength = getAndMap(infoMap, PIECE_LENGTH_BENC_STRING, BencodeInteger.class, BencodeInteger::longValue).orElse(0L);

        byte[][] pieces = getAndMap(infoMap, PIECES_BENC_STRING, BencodeString.class, this::getPieces).orElse(null);

        Optional<Long> length = getAndMap(infoMap, LENGTH_BENC_STRING, BencodeInteger.class, BencodeInteger::longValue);

        Optional<List<TorrentFileMetaInfo.FileInfo>> files = getAndMap(infoMap, FILES_BENC_STRING, BencodeList.class, this::parseFilesList);

        return new TorrentFileMetaInfo(
                announceUrl,
                Optional.ofNullable(announceUrlList),
                Optional.ofNullable(comment),
                Optional.ofNullable(creationDate),
                Optional.ofNullable(createdBy),
                Optional.ofNullable(encoding),
                new TorrentFileMetaInfo.InfoDictionary(name, pieceLength, pieces, length, files)
        );
    }

    private <T, R> Optional<R> getAndMap(BencodeMap map, BencodeString key, Class<T> expectedType, Function<T, R> mapper) {
        return Optional.ofNullable(map.get(key))
                .filter(expectedType::isInstance)
                .map(expectedType::cast)
                .map(mapper);
    }

    private byte[][] getPieces(BencodeString piecesString) {
        byte[] piecesBytes = piecesString.getValue();

        if (piecesBytes.length % PIECE_HASH_LENGTH != 0) {
            throw new RuntimeException("Pieces is not valid");
        }

        int numPieces = piecesBytes.length / PIECE_HASH_LENGTH;
        byte[][] pieces = new byte[piecesBytes.length / PIECE_HASH_LENGTH][PIECE_HASH_LENGTH];
        for (int i = 0; i < numPieces; i++) {
            System.arraycopy(piecesBytes, i * PIECE_HASH_LENGTH, pieces[i], 0, PIECE_HASH_LENGTH);
        }
        return pieces;
    }

    private List<TorrentFileMetaInfo.FileInfo> parseFilesList(BencodeList list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        // The files list is the value files maps to, and is a list of dictionaries
        return list.stream().map(element -> {
            if (!(element instanceof BencodeMap fileMap)) {
                throw new RuntimeException("Invalid .torrent file");
            }

            long fileLength = getAndMap(fileMap, LENGTH_BENC_STRING, BencodeInteger.class, BencodeInteger::longValue).orElse(0L);
            List<String> filePath = getAndMap(fileMap, PATH_BENC_STRING, BencodeList.class, list1 -> list1.stream()
                    .map(element1 -> ((BencodeString) element1).asString(StandardCharsets.UTF_8))
                    .toList()).orElse(Collections.emptyList());
            return new TorrentFileMetaInfo.FileInfo(fileLength, filePath);
        }).toList();
    }

}
