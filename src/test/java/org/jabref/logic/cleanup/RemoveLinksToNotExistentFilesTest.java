package org.jabref.logic.cleanup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jabref.logic.bibtex.FileFieldWriter;
import org.jabref.model.FieldChange;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.metadata.MetaData;
import org.jabref.preferences.FilePreferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jabref.logic.cleanup.RemoveLinksToNotExistentFiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoveLinksToNotExistentFilesTest {
    private Path defaultFileFolder;
    private Path fileBefore;
    private BibEntry entry;
    private FilePreferences filePreferences;
    private BibDatabaseContext databaseContext;
    private RemoveLinksToNotExistentFiles removeLinks;

    @BeforeEach
    void setUp(@TempDir Path bibFolder) throws IOException {
        // The folder where the files should be moved to
        defaultFileFolder = bibFolder.resolve("pdf");
        Files.createDirectory(defaultFileFolder);

        // The folder where the files are located originally
        Path fileFolder = bibFolder.resolve("files");
        Files.createDirectory(fileFolder);
        fileBefore = fileFolder.resolve("test.pdf");
        Files.createFile(fileBefore);

        MetaData metaData = new MetaData();
        metaData.setDefaultFileDirectory(defaultFileFolder.toAbsolutePath().toString());
        databaseContext = new BibDatabaseContext(new BibDatabase(), metaData);
        Files.createFile(bibFolder.resolve("test.bib"));
        databaseContext.setDatabasePath(bibFolder.resolve("test.bib"));

        entry = new BibEntry();
        entry.setCitationKey("Toot");
        entry.setField(StandardField.TITLE, "test title");
        entry.setField(StandardField.YEAR, "1989");
        LinkedFile fileField = new LinkedFile("", fileBefore.toAbsolutePath(), "");
        entry.setField(StandardField.FILE, FileFieldWriter.getStringRepresentation(fileField));

        filePreferences = mock(FilePreferences.class);
        when(filePreferences.shouldStoreFilesRelativeToBibFile()).thenReturn(false); 
        removeLinks = new RemoveLinksToNotExistentFiles(databaseContext, filePreferences);
    }

    /**
     * We deleted a linked file and called cleanup function from RemoveLinksToNotExistentFiles
     * There must be changes so the assertion is false.
     */
    @Test
    void deleteLinkedFileTest() {
        fileBefore.toFile().delete();
        List<FieldChange> changes = removeLinks.cleanup(entry);
        System.out.println(changes.toString());
        assertFalse(changes.isEmpty());
    }

    @Test
    void keepLinksToExistingFiles() {
        List<FieldChange> changes = removeLinks.cleanup(entry);
        assertTrue(changes.isEmpty());
    }
}
