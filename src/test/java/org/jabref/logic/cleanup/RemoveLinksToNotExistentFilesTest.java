package org.jabref.logic.cleanup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jabref.logic.bibtex.FileFieldWriter;
import org.jabref.model.FieldChange;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.metadata.MetaData;
import org.jabref.preferences.FilePreferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        LinkedFile fileField = new LinkedFile("", fileBefore.toAbsolutePath(), "");
        
        
        //Entry with one online and one normal linked file
        entry = new BibEntry(StandardEntryType.Article)
                .withField(StandardField.AUTHOR, "Shatakshi Sharma and Bhim Singh and Sukumar Mishra")
                .withField(StandardField.DATE, "April 2020")
                .withField(StandardField.YEAR, "2020")
                .withField(StandardField.DOI, "10.1109/TII.2019.2935531")
                .withField(StandardField.FILE, FileFieldWriter.getStringRepresentation(Arrays.asList(
                    new LinkedFile("", "https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8801912:PDF", ""),
                    fileField)))
                .withField(StandardField.ISSUE, "4")
                .withField(StandardField.ISSN, "1941-0050")
                .withField(StandardField.JOURNALTITLE, "IEEE Transactions on Industrial Informatics")
                .withField(StandardField.PAGES, "2346--2356")
                .withField(StandardField.PUBLISHER, "IEEE")
                .withField(StandardField.TITLE, "Economic Operation and Quality Control in PV-BES-DG-Based Autonomous System")
                .withField(StandardField.VOLUME, "16")
                .withField(StandardField.KEYWORDS, "Batteries, Generators, Economics, Power quality, State of charge, Harmonic analysis, Control systems, Battery, diesel generator (DG), distributed generation, power quality, photovoltaic (PV), voltage source converter (VSC)");


        filePreferences = mock(FilePreferences.class);
        when(filePreferences.shouldStoreFilesRelativeToBibFile()).thenReturn(false); 
        removeLinks = new RemoveLinksToNotExistentFiles(databaseContext, filePreferences);
    }

    @Test
    void deleteLinkedFile() {

        BibEntry expectedEntry = new BibEntry(StandardEntryType.Article)
        .withField(StandardField.AUTHOR, "Shatakshi Sharma and Bhim Singh and Sukumar Mishra")
        .withField(StandardField.DATE, "April 2020")
        .withField(StandardField.YEAR, "2020")
        .withField(StandardField.DOI, "10.1109/TII.2019.2935531")
        .withField(StandardField.FILE, FileFieldWriter.getStringRepresentation(
            new LinkedFile("", "https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8801912:PDF", "")))
        .withField(StandardField.ISSUE, "4")
        .withField(StandardField.ISSN, "1941-0050")
        .withField(StandardField.JOURNALTITLE, "IEEE Transactions on Industrial Informatics")
        .withField(StandardField.PAGES, "2346--2356")
        .withField(StandardField.PUBLISHER, "IEEE")
        .withField(StandardField.TITLE, "Economic Operation and Quality Control in PV-BES-DG-Based Autonomous System")
        .withField(StandardField.VOLUME, "16")
        .withField(StandardField.KEYWORDS, "Batteries, Generators, Economics, Power quality, State of charge, Harmonic analysis, Control systems, Battery, diesel generator (DG), distributed generation, power quality, photovoltaic (PV), voltage source converter (VSC)");

        assertEquals(2, entry.getFiles().size()); //Entry has 2 linked files

        fileBefore.toFile().delete();
        
        List<FieldChange> changes = removeLinks.cleanup(entry);
        
        assertFalse(changes.isEmpty());
        
        assertEquals(1, entry.getFiles().size());
        assertTrue(entry.getFiles().get(0).isOnlineLink()); //The non-deleted file should be linked online

        assertEquals(expectedEntry, entry); 
    }

    @Test
    void keepLinksToExistingFiles() {

        LinkedFile fileField = new LinkedFile("", fileBefore.toAbsolutePath(), "");
        BibEntry expectedEntry = new BibEntry(StandardEntryType.Article)
                .withField(StandardField.AUTHOR, "Shatakshi Sharma and Bhim Singh and Sukumar Mishra")
                .withField(StandardField.DATE, "April 2020")
                .withField(StandardField.YEAR, "2020")
                .withField(StandardField.DOI, "10.1109/TII.2019.2935531")
                .withField(StandardField.FILE, FileFieldWriter.getStringRepresentation(Arrays.asList(
                    new LinkedFile("", "https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8801912:PDF", ""),
                    fileField)))
                .withField(StandardField.ISSUE, "4")
                .withField(StandardField.ISSN, "1941-0050")
                .withField(StandardField.JOURNALTITLE, "IEEE Transactions on Industrial Informatics")
                .withField(StandardField.PAGES, "2346--2356")
                .withField(StandardField.PUBLISHER, "IEEE")
                .withField(StandardField.TITLE, "Economic Operation and Quality Control in PV-BES-DG-Based Autonomous System")
                .withField(StandardField.VOLUME, "16")
                .withField(StandardField.KEYWORDS, "Batteries, Generators, Economics, Power quality, State of charge, Harmonic analysis, Control systems, Battery, diesel generator (DG), distributed generation, power quality, photovoltaic (PV), voltage source converter (VSC)");

        assertEquals(2, entry.getFiles().size());

        List<FieldChange> changes = removeLinks.cleanup(entry);
        assertTrue(changes.isEmpty());

        assertEquals(2, entry.getFiles().size()); //Since no files deleted the linked files should remain 2

        assertTrue(entry.getFiles().get(0).isOnlineLink());
        assertFalse(entry.getFiles().get(1).isOnlineLink());
        
        assertEquals(expectedEntry, entry); 
    }

    
}
