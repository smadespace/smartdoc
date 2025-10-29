package world.bank.smartdoc.controller;

import org.apache.el.lang.ELArithmetic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.bank.smartdoc.api.RefreshApi;
import world.bank.smartdoc.model.ProcessingStatus;
import world.bank.smartdoc.model.RefreshFilesRequest;
import world.bank.smartdoc.service.FileProcessService;
import world.bank.smartdoc.service.FileWatcherService;

import java.io.IOException;
import java.nio.file.Path;


@RestController
@RequestMapping("/api/v1")
public class RefreshApiImpl implements RefreshApi {

    @Autowired
    private FileProcessService fileProcessService;

    @Autowired
    private FileWatcherService fileWatcherService;

    @Override
    public ResponseEntity<ProcessingStatus> refreshFiles(RefreshFilesRequest refreshFilesRequest)  {
        var resBody = fileProcessService.refreshFilesInDatabaseFromReq(refreshFilesRequest.getPath());
        if(refreshFilesRequest.getWatch()){
            fileWatcherService.watchNewFolder(refreshFilesRequest.getPath());
        }else {
            fileWatcherService.unwatchFolder(Path.of(refreshFilesRequest.getPath()));
        }
        return ResponseEntity.status(200).body(resBody);
    }
}
