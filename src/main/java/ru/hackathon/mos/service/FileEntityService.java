package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.dto.document.DocumentCreateRequest;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.exception.FileNotFoundException;
import ru.hackathon.mos.repository.FileEntityRepository;

import java.net.URLConnection;
import java.util.Base64;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FileEntityService {
    private final FileEntityRepository fileEntityRepository;

    public FileEntity saveDocument(Long documentId, DocumentCreateRequest request) {
        byte[] fileBytes = Base64.getDecoder().decode(request.fileContent());
        FileEntity fileEntity = new FileEntity();
        fileEntity.setOwnerType("document");
        fileEntity.setOwnerId(documentId);
        fileEntity.setFilename(request.fileName());
        String mimeType = URLConnection.guessContentTypeFromName(request.fileName());
        fileEntity.setMimeType(mimeType != null ? mimeType : "application/octet-stream");
        fileEntity.setSizeBytes((long) fileBytes.length);
        fileEntity.setFileData(fileBytes);
        fileEntity.setFileRole(request.type());
        fileEntity.setSortOrder(1);
        fileEntityRepository.save(fileEntity);
        log.info("save document success, document id:{}", documentId);
        return fileEntity;
    }

    public FileEntity getFileEntityById(Long fileEntityId) {
        return fileEntityRepository.findById(fileEntityId)
                .orElseThrow(() -> new FileNotFoundException("Файл с ID " + fileEntityId + " не найден"));
    }


    /**
     * Удаление файла.
     *
     * @param fileEntityId ID файла.
     */
    @Transactional
    public void deleteFile(Long fileEntityId) {
        FileEntity file = getFileEntityById(fileEntityId);

        String filename = file.getFilename();
        fileEntityRepository.delete(file);

        log.info("A file with ID={}, filename ='{}' has been deleted.", fileEntityId, filename);
    }
}