package world.bank.smartdoc.service;



import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import world.bank.smartdoc.entity.FileMetadataEntity;
import world.bank.smartdoc.repository.FileMetadataRepository;


import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileWatcherService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);

    private final FileMetadataRepository repository;

    private final WatchService watchService;
    private final Map<WatchKey, Path> keyToPath = new HashMap<>();

    public FileWatcherService(FileMetadataRepository repository) throws IOException {
        this.repository = repository;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }


//    public void watchNewFolder(String folderPath) {
//      try {
//          Path path = Paths.get(folderPath);
//          if (Files.exists(path) && Files.isDirectory(path)) {
//              registerAll(path);
//              System.out.println("Now watching folder: " + path);
//          } else {
//              throw new IllegalArgumentException("Folder does not exist or is not a directory: " + folderPath);
//          }
//      }catch (IOException e){
//          logger.error(e.toString());
//      }
//    }


    public void watchNewFolder(String folderPath) {
        try {
            Path path = Paths.get(folderPath).toAbsolutePath().normalize();

            if (!Files.exists(path) || !Files.isDirectory(path)) {
                throw new IllegalArgumentException("Folder does not exist or is not a directory: " + folderPath);
            }

            // Check if already registered
            if (keyToPath.containsValue(path)) {
                logger.warn("Folder already being watched: {}", path);
                return;
            }

            // Register folder and its subdirectories
            registerAll(path);
            logger.info("Now watching folder: {}", path);

        } catch (IOException e) {
            logger.error("Error watching folder: {}", folderPath, e);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Recursively register a directory and all subdirectories
     */
    private void registerAll(Path start) throws IOException {
        Files.walk(start)
                .filter(Files::isDirectory)
                .forEach(this::registerDir);
    }


    private void registerDir(Path dir) {
        try {
            WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );
            keyToPath.put(key, dir);
        } catch (IOException e) {
           logger.error(e.toString());
        }
    }

    public void unwatchFolder(Path folder) {
        keyToPath.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(folder)) {
                WatchKey key = entry.getKey();
                key.cancel(); // stop watching this folder
                System.out.println("Stopped watching folder: " + folder);
                return true; // remove from map
            }
            return false;
        });
    }


    @Override
    public void run() {
        while (true) {
            try {
                WatchKey key = watchService.take();
                Path dir = keyToPath.get(key);

                if (dir == null) {
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fullPath = dir.resolve((Path) event.context());

                    if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("File deleted: " + fullPath);

                        // Update database entity
                        Optional<FileMetadataEntity> fileOpt = repository.findByPath(fullPath.toString());
                        fileOpt.ifPresent(file -> {
                            file.setPresent(false);
                            file.setUpdatedAt(OffsetDateTime.now());
                            repository.save(file);
                        });
                    }

                    // If a new directory is created, register it dynamically
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) {
                        System.out.println("New directory created: " + fullPath);
                        registerAll(fullPath);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    keyToPath.remove(key);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
               logger.error(e.toString());
            }
        }
    }
}
