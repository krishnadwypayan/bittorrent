package com.krishna.kota.processor;

import com.krishna.kota.bencode.decode.Bdecoder;
import com.krishna.kota.bencode.decode.IBdecoder;
import com.krishna.kota.bencode.type.*;
import com.krishna.kota.bencode.type.impl.BencInteger;
import com.krishna.kota.bencode.type.impl.BencList;
import com.krishna.kota.bencode.type.impl.BencMap;
import com.krishna.kota.bencode.type.impl.BencString;
import com.krishna.kota.model.TorrentFileMetaInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static com.krishna.kota.model.TorrentFileConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@MicronautTest
public class TorrentFileProcessorTest {

    private static final String DUMMY_PATH = "test.torrent";

    @Inject
    private IBdecoder bdecoder;

    @Inject
    private TorrentFileProcessor torrentFileProcessor;

    @MockBean(Bdecoder.class)
    public IBdecoder bdecoder() {
        return Mockito.mock(IBdecoder.class);
    }

    @Test
    public void decodeTorrentFile_ValidSingleFileTorrent_ReturnsCorrectMetaInfoTest() throws IOException {
        // Arrange
        BencodeMap infoMap = new BencMap(Map.of(
                NAME_BENC_STRING, new BencString("single-file.txt"),
                PIECE_LENGTH_BENC_STRING, new BencInteger(262144),
                PIECES_BENC_STRING, new BencString(new byte[20]),
                LENGTH_BENC_STRING, new BencInteger(12345)
        ));
        BencodeMap rootMap = new BencMap(Map.of(
                ANNOUNCE_BENC_STRING, new BencString("http://tracker.com/announce"),
                CREATION_DATE_BENC_STRING, new BencInteger(1672531200L),
                COMMENT_BENC_STRING, new BencString("Test Comment"),
                CREATED_BY_BENC_STRING, new BencString("TestClient/1.0"),
                ENCODING_BENC_STRING, new BencString("UTF-8"),
                INFO_BENC_STRING, infoMap
        ));
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(rootMap);

            // Act
            TorrentFileMetaInfo result = torrentFileProcessor.decodeTorrentFile(DUMMY_PATH);

            // Assert
            assertNotNull(result);
            assertEquals("http://tracker.com/announce", result.announce());
            assertEquals("Test Comment", result.comment().orElse(null));
            assertEquals("TestClient/1.0", result.createdBy().orElse(null));
            assertEquals("UTF-8", result.encoding().orElse(null));
            assertEquals(Instant.ofEpochSecond(1672531200L), result.creationDate().orElse(null));
            assertEquals("single-file.txt", result.info().name());
            assertEquals(12345L, result.info().length().orElse(0L));
            assertNotNull(result.info().pieces());
        }
    }

    @Test
    public void decodeTorrentFile_ValidMultiFileTorrent_ReturnsCorrectMetaInfoTest() throws IOException {
        // Arrange
        BencodeMap file1 = new BencMap(Map.of(
                LENGTH_BENC_STRING, new BencInteger(1024),
                PATH_BENC_STRING, new BencList(List.of(new BencString("dir1"), new BencString("file1.txt")))
        ));
        BencodeList filesList = new BencList(List.of(file1));
        BencodeMap infoMap = new BencMap(Map.of(
                NAME_BENC_STRING, new BencString("multi-file-dir"),
                PIECE_LENGTH_BENC_STRING, new BencInteger(262144),
                PIECES_BENC_STRING, new BencString(new byte[20]),
                FILES_BENC_STRING, filesList
        ));
        BencodeMap rootMap = new BencMap(Map.of(ANNOUNCE_BENC_STRING, new BencString("http://tracker.com/announce"), INFO_BENC_STRING, infoMap));
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(rootMap);

            // Act
            TorrentFileMetaInfo result = torrentFileProcessor.decodeTorrentFile(DUMMY_PATH);

            // Assert
            assertNotNull(result);
            assertEquals("multi-file-dir", result.info().name());
            assertTrue(result.info().length().isEmpty());
            assertTrue(result.info().files().isPresent());
            assertEquals(1, result.info().files().get().size());
        }
    }

    @Test
    public void decodeTorrentFile_FileReadThrowsIOException_ThrowsRuntimeExceptionTest() {
        // Arrange
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(any(Path.class))).thenThrow(new IOException("Disk read error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> torrentFileProcessor.decodeTorrentFile(DUMMY_PATH));
            assertTrue(exception.getCause() instanceof IOException);
            assertEquals("Disk read error", exception.getCause().getMessage());
        }
    }

    @Test
    public void decodeTorrentFile_BdecoderReturnsNonMapType_ThrowsRuntimeExceptionTest() throws IOException {
        // Arrange
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(new BencString("not a map"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> torrentFileProcessor.decodeTorrentFile(DUMMY_PATH));
            assertEquals("Invalid .torrent file", exception.getMessage());
        }
    }

    @Test
    public void decodeTorrentFile_MapMissingInfoDictionary_ThrowsRuntimeExceptionTest() throws IOException {
        // Arrange
        BencodeMap rootMapWithoutInfo = new BencMap(Map.of(ANNOUNCE_BENC_STRING, new BencString("http://tracker.com/announce")));
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(rootMapWithoutInfo);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> torrentFileProcessor.decodeTorrentFile(DUMMY_PATH));
            assertEquals("Invalid .torrent file. 'info' not found", exception.getMessage());
        }
    }

    @Test
    public void decodeTorrentFile_WithAnnounceList_ReturnsCorrectListTest() throws IOException {
        // Arrange
        BencodeList announceList = new BencList(List.of(new BencString("http://tracker1.com"), new BencString("http://tracker2.com")));
        BencodeMap infoMap = new BencMap(Map.of(NAME_BENC_STRING, new BencString("file.dat"), PIECE_LENGTH_BENC_STRING, new BencInteger(16384), PIECES_BENC_STRING, new BencString(new byte[20]), LENGTH_BENC_STRING, new BencInteger(100)));
        BencodeMap rootMap = new BencMap(Map.of(ANNOUNCE_LIST_BENC_STRING, announceList, INFO_BENC_STRING, infoMap));
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(rootMap);

            // Act
            TorrentFileMetaInfo result = torrentFileProcessor.decodeTorrentFile(DUMMY_PATH);

            // Assert
            assertNotNull(result);
            assertTrue(result.announceList().isPresent());
            assertEquals(2, result.announceList().get().size());
            assertEquals("http://tracker1.com", result.announceList().get().get(0));
        }
    }

    @Test
    public void decodeTorrentFile_MinimalValidFile_ReturnsNullAndEmptyOptionalsTest() throws IOException {
        // Arrange
        BencodeMap infoMap = new BencMap(Map.of(
                NAME_BENC_STRING, new BencString("minimal.file"),
                PIECE_LENGTH_BENC_STRING, new BencInteger(16384),
                PIECES_BENC_STRING, new BencString(new byte[20]),
                LENGTH_BENC_STRING, new BencInteger(99)
        ));
        BencodeMap rootMap = new BencMap(Map.of(INFO_BENC_STRING, infoMap));
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(rootMap);

            // Act
            TorrentFileMetaInfo result = torrentFileProcessor.decodeTorrentFile(DUMMY_PATH);

            // Assert
            assertNotNull(result);
            assertNull(result.announce());
            assertTrue(result.comment().isEmpty());
            assertTrue(result.creationDate().isEmpty());
            assertTrue(result.createdBy().isEmpty());
            assertTrue(result.encoding().isEmpty());
            assertEquals("minimal.file", result.info().name());
        }
    }

    @Test
    public void decodeTorrentFile_AnnounceListWithInvalidTypes_IgnoresInvalidEntriesTest() throws IOException {
        // Arrange
        BencodeList announceList = new BencList(List.of(new BencString("http://tracker1.com"), new BencInteger(123), new BencString("http://tracker2.com")));
        BencodeMap infoMap = new BencMap(Map.of(NAME_BENC_STRING, new BencString("file.dat"), PIECE_LENGTH_BENC_STRING, new BencInteger(16384), PIECES_BENC_STRING, new BencString(new byte[20]), LENGTH_BENC_STRING, new BencInteger(100)));
        BencodeMap rootMap = new BencMap(Map.of(ANNOUNCE_LIST_BENC_STRING, announceList, INFO_BENC_STRING, infoMap));
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.newInputStream(Path.of(DUMMY_PATH))).thenReturn(mockInputStream);
            when(bdecoder.decode(mockInputStream)).thenReturn(rootMap);

            // Act
            TorrentFileMetaInfo result = torrentFileProcessor.decodeTorrentFile(DUMMY_PATH);

            // Assert
            assertNotNull(result);
            assertTrue(result.announceList().isPresent());
            List<String> urls = result.announceList().get();
            assertEquals(2, urls.size());
            assertTrue(urls.contains("http://tracker1.com"));
            assertTrue(urls.contains("http://tracker2.com"));
        }
    }

}
