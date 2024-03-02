package org.jabref.logic.cleanup;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jabref.logic.externalfiles.LinkedFileHandler;
import org.jabref.model.FieldChange;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.util.OptionalUtil;
import org.jabref.preferences.FilePreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveLinksToNotExistentFiles implements CleanupJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveLinksToNotExistentFiles.class);

    private final BibDatabaseContext databaseContext;
    private final FilePreferences filePreferences;

    public RemoveLinksToNotExistentFiles(BibDatabaseContext databaseContext, FilePreferences filePreferences) {
        this.databaseContext = Objects.requireNonNull(databaseContext);
        this.filePreferences = Objects.requireNonNull(filePreferences);
    }

    @Override
    public List<FieldChange> cleanup(BibEntry entry) {
        List<LinkedFile> files = entry.getFiles();
        List<LinkedFile> files2 = new ArrayList<LinkedFile>();
        boolean changed = false;
        for (LinkedFile file : files) {
            LinkedFileHandler fileHandler = new LinkedFileHandler(file, entry, databaseContext, filePreferences);
            
            if (file.isOnlineLink() == false) {
                Optional<Path> oldFile = file.findIn(databaseContext, filePreferences);
                
                if (oldFile.isEmpty()) {
                    changed = true;
                } else {
                    files2.add(file);
                }
            }
        }

        if (changed) {
            Optional<FieldChange> changes = entry.setFiles(files2);
            return OptionalUtil.toList(changes);
        }

        return Collections.emptyList();
    }
}
