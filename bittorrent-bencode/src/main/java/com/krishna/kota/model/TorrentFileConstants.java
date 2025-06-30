package com.krishna.kota.model;

import com.krishna.kota.bencode.type.impl.BencString;

public interface TorrentFileConstants {

    String ANNOUNCE = "announce";
    String ANNOUNCE_LIST = "announce-list";
    String COMMENT = "comment";
    String CREATED_BY = "created by";
    String CREATION_DATE = "creation date";
    String ENCODING = "encoding";
    String INFO = "info";
    String NAME = "name";
    String PIECE_LENGTH = "piece length";
    String PIECES = "pieces";
    String LENGTH = "length";
    String FILES = "files";
    String PATH = "path";
    int PIECE_HASH_LENGTH = 20;

    BencString ANNOUNCE_BENC_STRING = new BencString(ANNOUNCE);
    BencString ANNOUNCE_LIST_BENC_STRING = new BencString(ANNOUNCE_LIST);
    BencString COMMENT_BENC_STRING = new BencString(COMMENT);
    BencString CREATED_BY_BENC_STRING = new BencString(CREATED_BY);
    BencString CREATION_DATE_BENC_STRING = new BencString(CREATION_DATE);
    BencString ENCODING_BENC_STRING = new BencString(ENCODING);
    BencString INFO_BENC_STRING = new BencString(INFO);
    BencString NAME_BENC_STRING = new BencString(NAME);
    BencString PIECE_LENGTH_BENC_STRING = new BencString(PIECE_LENGTH);
    BencString PIECES_BENC_STRING = new BencString(PIECES);
    BencString LENGTH_BENC_STRING = new BencString(LENGTH);
    BencString FILES_BENC_STRING = new BencString(FILES);
    BencString PATH_BENC_STRING = new BencString(PATH);

}
